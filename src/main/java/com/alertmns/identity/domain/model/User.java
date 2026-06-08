package com.alertmns.identity.domain.model;

import com.alertmns.identity.domain.event.AbsenceMessageUpdated;
import com.alertmns.identity.domain.event.ProfileUpdated;
import com.alertmns.identity.domain.event.UserActivated;
import com.alertmns.identity.domain.event.UserReactivated;
import com.alertmns.identity.domain.event.UserRegistered;
import com.alertmns.identity.domain.event.UserSuspended;
import com.alertmns.shared.AggregateRoot;
import com.alertmns.shared.Email;
import com.alertmns.shared.UserId;

import java.time.Instant;
import java.util.Objects;

/**
 * Agrégat racine représentant un utilisateur dans le BC Identity.
 *
 * <p>Représente un utilisateur avec son cycle de vie et ses règles métier. Un utilisateur suit le cycle : PENDING →
 * ACTIVE → SUSPENDED → ACTIVE (réactivation).</p>
 *
 * <p>L'attribut {@code isAnonymized} est orthogonal au statut : il marque l'effacement effectif des données
 * personnelles pour conformité RGPD.</p>
 */
public final class User extends AggregateRoot {

    private final UserId id;
    private final Email email;
    private HashedPassword hashedPassword;
    private Profile profile;
    private UserStatus status;
    private boolean isAnonymized;
    private final Instant createdAt;

    private User(
            UserId id,
            Email email,
            HashedPassword hashedPassword,
            Profile profile,
            UserStatus status,
            boolean isAnonymized,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.email = Objects.requireNonNull(email, "email must not be null");
        this.hashedPassword = Objects.requireNonNull(hashedPassword, "hashedPassword must not be null");
        this.profile = Objects.requireNonNull(profile, "profile must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.isAnonymized = isAnonymized;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    /**
     * Crée un nouvel utilisateur avec le statut {@link UserStatus#PENDING}.
     *
     * <p>Émet {@link UserRegistered}.</p>
     *
     * @param email          l'adresse email de l'utilisateur
     * @param hashedPassword le mot de passe déjà haché
     * @param profile        le profil de l'utilisateur
     * @param now            instant de l'opération
     * @return le nouvel utilisateur créé
     */
    public static User register(
            Email email,
            HashedPassword hashedPassword,
            Profile profile,
            Instant now
    ) {
        User user = new User(
                UserId.generate(),
                email,
                hashedPassword,
                profile,
                UserStatus.PENDING,
                false,
                now
        );

        user.registerEvent(new UserRegistered(user.id, user.email, now));
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
            Email email,
            HashedPassword hashedPassword,
            Profile profile,
            UserStatus status,
            boolean isAnonymized,
            Instant createdAt
    ) {
        return new User(
                id,
                email,
                hashedPassword,
                profile,
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
     * @param avatar    l'URL de l'avatar
     * @param now       instant de l'opération
     * @throws NullPointerException  si firstName ou lastName est null
     * @throws IllegalStateException si le statut n'est pas {@link UserStatus#ACTIVE}
     */
    public void updateProfile(FirstName firstName, LastName lastName, String avatar, Instant now) {
        requireStatus(UserStatus.ACTIVE, "update profile");
        Objects.requireNonNull(firstName, "firstName must not be null");
        Objects.requireNonNull(lastName, "lastName must not be null");
        profile = profile.withIdentity(firstName, lastName, avatar);
        registerEvent(new ProfileUpdated(id, now));
    }

    /**
     * Met à jour le message d'absence de l'utilisateur.
     *
     * <p>Émet {@link AbsenceMessageUpdated}.</p>
     *
     * @param absenceMessage le nouveau message d'absence
     * @param now            instant de l'opération
     * @throws NullPointerException  si absenceMessage est null
     * @throws IllegalStateException si le statut n'est pas {@link UserStatus#ACTIVE}
     */
    public void updateAbsenceMessage(AbsenceMessage absenceMessage, Instant now) {
        requireStatus(UserStatus.ACTIVE, "update absence message");
        Objects.requireNonNull(absenceMessage, "absenceMessage must not be null");
        profile = profile.withAbsenceMessage(absenceMessage);
        registerEvent(new AbsenceMessageUpdated(id, now));
    }

    /**
     * Active un compte en attente en définissant son mot de passe.
     *
     * <p>Émet {@link UserActivated}.</p>
     *
     * @param hashedPassword le nouveau mot de passe haché choisi par l'utilisateur
     * @param now            instant de l'opération
     * @throws NullPointerException  si hashedPassword est null
     * @throws IllegalStateException si le statut n'est pas {@link UserStatus#PENDING}
     */
    public void activateWithPassword(HashedPassword hashedPassword, Instant now) {
        requireStatus(UserStatus.PENDING, "activate with password");
        Objects.requireNonNull(hashedPassword, "hashedPassword must not be null");
        this.hashedPassword = hashedPassword;
        this.status = UserStatus.ACTIVE;
        registerEvent(new UserActivated(id, email, now));
    }

    /**
     * Réactive un compte suspendu (SUSPENDED → ACTIVE).
     *
     * <p>Émet {@link UserReactivated}.</p>
     *
     * @param now instant de l'opération
     * @throws IllegalStateException si le statut n'est pas {@link UserStatus#SUSPENDED}
     */
    public void reactivate(Instant now) {
        requireStatus(UserStatus.SUSPENDED, "reactivate");
        status = UserStatus.ACTIVE;
        registerEvent(new UserReactivated(id, now));
    }

    /**
     * Suspend un compte actif (ACTIVE → SUSPENDED).
     *
     * <p>Émet {@link UserSuspended}.</p>
     *
     * @param now instant de l'opération
     * @throws IllegalStateException si le statut n'est pas {@link UserStatus#ACTIVE}
     */
    public void suspend(Instant now) {
        requireStatus(UserStatus.ACTIVE, "suspend");
        status = UserStatus.SUSPENDED;
        registerEvent(new UserSuspended(id, now));
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

    public Email email() {
        return email;
    }

    public HashedPassword hashedPassword() {
        return hashedPassword;
    }

    public Profile profile() {
        return profile;
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
                ", status=" + status +
                ", isAnonymized=" + isAnonymized +
                '}';
    }
}
