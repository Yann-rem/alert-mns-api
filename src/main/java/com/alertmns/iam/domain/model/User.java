package com.alertmns.iam.domain.model;

import com.alertmns.iam.domain.event.AbsenceMessageUpdated;
import com.alertmns.iam.domain.event.ProfileUpdated;
import com.alertmns.iam.domain.event.UserActivated;
import com.alertmns.iam.domain.event.UserReactivated;
import com.alertmns.iam.domain.event.UserRegistered;
import com.alertmns.iam.domain.event.UserSuspended;
import com.alertmns.iam.domain.exception.BannedUserCannotBeReactivatedException;
import com.alertmns.shared.AggregateRoot;
import com.alertmns.shared.OrganisationId;
import com.alertmns.shared.UserId;

import java.time.Instant;
import java.util.Objects;

/**
 * Agrégat racine représentant un utilisateur dans le BC IAM.
 *
 * <p>Représente un utilisateur avec son cycle de vie et ses règles métier.
 * Un utilisateur suit le cycle : PENDING → ACTIVE → SUSPENDED → ACTIVE (réactivation).
 * BANNED est terminal.</p>
 *
 * <p>L'attribut {@code isAnonymized} est orthogonal au statut : il marque l'effacement effectif des PII pour
 * conformité RGPD.</p>
 */
public final class User extends AggregateRoot {

    private final UserId id;
    private final Email email;
    private final HashedPassword hashedPassword;
    private Profile profile;
    private final UserRole role;
    private UserStatus status;
    private final OrganisationId organisationId;
    private boolean isAnonymized;
    private final Instant createdAt;

    private User(
            UserId id,
            OrganisationId organisationId,
            Email email,
            HashedPassword hashedPassword,
            Profile profile,
            UserRole role,
            UserStatus status,
            boolean isAnonymized,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.organisationId = Objects.requireNonNull(organisationId, "organisationId must not be null");
        this.email = Objects.requireNonNull(email, "email must not be null");
        this.hashedPassword = Objects.requireNonNull(hashedPassword, "hashedPassword must not be null");
        this.profile = Objects.requireNonNull(profile, "profile must not be null");
        this.role = Objects.requireNonNull(role, "role must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.isAnonymized = isAnonymized;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    /**
     * Crée un nouvel utilisateur avec le statut {@link UserStatus#PENDING}.
     *
     * <p>L'identifiant et la date de création sont générés automatiquement.
     * Le rôle par défaut est {@link UserRole#USER}.
     * Le drapeau {@code isAnonymized} est initialisé à {@code false}.</p>
     *
     * <p>Émet {@link UserRegistered}.</p>
     *
     * @param organisationId l'identifiant de l'organisation de rattachement
     * @param email          l'adresse email de l'utilisateur
     * @param hashedPassword le mot de passe déjà haché
     * @param profile        le profil de l'utilisateur
     * @return le nouvel utilisateur créé
     */
    public static User register(
            OrganisationId organisationId,
            Email email,
            HashedPassword hashedPassword,
            Profile profile
    ) {
        User user = new User(
                UserId.generate(),
                organisationId,
                email,
                hashedPassword,
                profile,
                UserRole.USER,
                UserStatus.PENDING,
                false,
                Instant.now()
        );

        user.registerEvent(new UserRegistered(user.organisationId, user.id, user.email, user.role));
        return user;
    }

    /**
     * Reconstruit un utilisateur existant depuis la persistence.
     *
     * <p>Aucun événement de domaine n'est émis.</p>
     *
     * @param isAnonymized indique si les PII de l'utilisateur ont été effacées (RGPD)
     * @return l'utilisateur reconstitué
     */
    public static User reconstitute(
            UserId id,
            OrganisationId organisationId,
            Email email,
            HashedPassword hashedPassword,
            Profile profile,
            UserRole role,
            UserStatus status,
            boolean isAnonymized,
            Instant createdAt
    ) {
        return new User(
                id,
                organisationId,
                email,
                hashedPassword,
                profile,
                role,
                status,
                isAnonymized,
                createdAt
        );
    }

    /**
     * Met à jour le profil de l'utilisateur.
     *
     * <p>Émet {@link ProfileUpdated}.</p>
     *
     * @param firstName le nouveau prénom
     * @param lastName  le nouveau nom
     * @param avatar    l'URL de l'avatar (nullable)
     * @throws NullPointerException  si firstName ou lastName est null
     * @throws IllegalStateException si le statut n'est pas {@link UserStatus#ACTIVE}
     */
    public void updateProfile(FirstName firstName, LastName lastName, String avatar) {
        requireStatus(UserStatus.ACTIVE, "update profile");
        Objects.requireNonNull(firstName, "firstName must not be null");
        Objects.requireNonNull(lastName, "lastName must not be null");
        profile = profile.withIdentity(firstName, lastName, avatar);
        registerEvent(new ProfileUpdated(id));
    }

    /**
     * Met à jour le message d'absence de l'utilisateur.
     *
     * <p>Émet {@link AbsenceMessageUpdated}.</p>
     *
     * @param absenceMessage le nouveau message d'absence
     * @throws NullPointerException  si absenceMessage est null
     * @throws IllegalStateException si le statut n'est pas {@link UserStatus#ACTIVE}
     */
    public void updateAbsenceMessage(AbsenceMessage absenceMessage) {
        requireStatus(UserStatus.ACTIVE, "update absence message");
        Objects.requireNonNull(absenceMessage, "absenceMessage must not be null");
        profile = profile.withAbsenceMessage(absenceMessage);
        registerEvent(new AbsenceMessageUpdated(id));
    }

    /**
     * Active un compte en attente (PENDING → ACTIVE).
     *
     * <p>Émet {@link UserActivated}.</p>
     *
     * @throws IllegalStateException si le statut n'est pas {@link UserStatus#PENDING}
     */
    public void activate() {
        requireStatus(UserStatus.PENDING, "activate");
        status = UserStatus.ACTIVE;
        registerEvent(new UserActivated(id));
    }

    /**
     * Réactive un compte suspendu (SUSPENDED → ACTIVE).
     *
     * <p>Le statut {@link UserStatus#BANNED} est terminal : la réactivation y est refusée via une exception métier nommée.</p>
     *
     * <p>Émet {@link UserReactivated}.</p>
     *
     * @throws BannedUserCannotBeReactivatedException si le statut est {@link UserStatus#BANNED}
     * @throws IllegalStateException                  si le statut n'est ni {@link UserStatus#SUSPENDED} ni {@link UserStatus#BANNED}
     */
    public void reactivate() {
        if (status == UserStatus.BANNED) {
            throw new BannedUserCannotBeReactivatedException(id);
        }
        requireStatus(UserStatus.SUSPENDED, "reactivate");
        status = UserStatus.ACTIVE;
        registerEvent(new UserReactivated(id));
    }

    /**
     * Suspend un compte actif (ACTIVE → SUSPENDED).
     *
     * <p>Émet {@link UserSuspended}.</p>
     *
     * @throws IllegalStateException si le statut n'est pas {@link UserStatus#ACTIVE}
     */
    public void suspend() {
        requireStatus(UserStatus.ACTIVE, "suspend");
        status = UserStatus.SUSPENDED;
        registerEvent(new UserSuspended(id));
    }

    private void requireStatus(UserStatus expected, String action) {
        if (status != expected) {
            throw new IllegalStateException(
                    "Cannot " + action + ": user is not " + expected + ". Current status: " + status
            );
        }
    }

    public UserId id() {
        return id;
    }

    public OrganisationId organisationId() {
        return organisationId;
    }

    public Email email() {
        return email;
    }

    public HashedPassword hashedPassword() {
        return hashedPassword;
    }

    public Profile profile() {
        return profile;
    }

    public UserRole role() {
        return role;
    }

    public UserStatus status() {
        return status;
    }

    public boolean isAnonymized() {
        return isAnonymized;
    }

    public Instant createdAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email=" + email +
                ", role=" + role +
                ", status=" + status +
                ", isAnonymized=" + isAnonymized +
                '}';
    }
}
