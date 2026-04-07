package com.alertmns.iam.domain.model;

import com.alertmns.iam.domain.event.AbsenceMessageUpdated;
import com.alertmns.iam.domain.event.ProfileUpdated;
import com.alertmns.iam.domain.event.UserActivated;
import com.alertmns.iam.domain.event.UserDisabled;
import com.alertmns.iam.domain.event.UserRegistered;
import com.alertmns.shared.AggregateRoot;

import java.time.Instant;
import java.util.Objects;

/**
 * Agrégat racine du BC IAM.
 *
 * <p>Représente un utilisateur avec son cycle de vie et ses règles métier.
 * Un utilisateur suit le cycle : PENDING → ACTIVE → DISABLED → ACTIVE (réactivation).</p>
 */
public final class User extends AggregateRoot {

    private final UserId id;
    private final Email email;
    private final HashedPassword hashedPassword;
    private Profile profile;
    private final UserRole role;
    private UserStatus status;
    private final Instant createdAt;

    private User(
            UserId id,
            Email email,
            HashedPassword hashedPassword,
            Profile profile,
            UserRole role,
            UserStatus status,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id, "L'identifiant ne peut pas être null");
        this.email = Objects.requireNonNull(email, "L'email ne peut pas être null");
        this.hashedPassword = Objects.requireNonNull(hashedPassword, "Le mot de passe haché ne peut pas être null");
        this.profile = Objects.requireNonNull(profile, "Le profile ne peut pas être null");
        this.role = Objects.requireNonNull(role, "Le role ne peut pas être null");
        this.status = Objects.requireNonNull(status, "Le statut ne peut pas être null");
        this.createdAt = Objects.requireNonNull(createdAt, "La date de création ne peut pas être null");
    }

    /**
     * Crée un nouvel utilisateur avec le statut {@link UserStatus#PENDING}.
     *
     * <p>L'identifiant et la date de création sont générés automatiquement.
     * Le rôle par défaut est {@link UserRole#USER}.</p>
     *
     * <p>Émet {@link UserRegistered}.</p>
     *
     * @param email          l'adresse email de l'utilisateur
     * @param hashedPassword le mot de passe déjà haché
     * @param profile        le profil de l'utilisateur
     * @return le nouvel utilisateur créé
     */
    public static User register(
            Email email,
            HashedPassword hashedPassword,
            Profile profile
    ) {
        User user = new User(
                UserId.generate(),
                email,
                hashedPassword,
                profile,
                UserRole.USER,
                UserStatus.PENDING,
                Instant.now()
        );

        user.registerEvent(new UserRegistered(user.id, user.email, user.role));
        return user;
    }

    /**
     * Reconstruit un utilisateur existant depuis la persistence.
     *
     * <p>Aucun événement de domaine n'est émis.</p>
     *
     * @return l'utilisateur reconstitué
     */
    public static User reconstitute(
            UserId id,
            Email email,
            HashedPassword hashedPassword,
            Profile profile,
            UserRole role,
            UserStatus status,
            Instant createdAt
    ) {
        return new User(
                id,
                email,
                hashedPassword,
                profile,
                role,
                status,
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
     */
    public void updateProfile(FirstName firstName, LastName lastName, String avatar) {
        profile = Profile.of(firstName, lastName).withAvatar(avatar);
        registerEvent(new ProfileUpdated(id));
    }

    /**
     * Met à jour le message d'absence de l'utilisateur.
     *
     * <p>Émet {@link AbsenceMessageUpdated}.</p>
     *
     * @param absenceMessage le nouveau message d'absence
     * @throws NullPointerException si absenceMessage est null
     */
    public void updateAbsenceMessage(AbsenceMessage absenceMessage) {
        Objects.requireNonNull(absenceMessage, "Le message d'absence ne peut pas être null");
        profile = profile.withAbsenceMessage(absenceMessage);
        registerEvent(new AbsenceMessageUpdated(id));
    }

    // TODO : ajouter la personne responsable de l'activation.

    /**
     * Active un compte en attente (PENDING → ACTIVE).
     *
     * <p>Émet {@link UserActivated}.</p>
     *
     * @throws IllegalStateException si le statut n'est pas {@link UserStatus#PENDING}
     */
    public void activate() {
        if (status != UserStatus.PENDING) {
            throw new IllegalStateException(
                    "Impossible d'activer un compte qui n'est pas en attente. Statut actuel : " + status
            );
        }
        status = UserStatus.ACTIVE;
        registerEvent(new UserActivated(id));
    }

    /**
     * Réactive un compte désactivé (DISABLED → ACTIVE).
     *
     * <p>Émet {@link UserActivated}.</p>
     *
     * @throws IllegalStateException si le statut n'est pas {@link UserStatus#DISABLED}
     */
    public void reactivate() {
        if (status != UserStatus.DISABLED) {
            throw new IllegalStateException(
                    "Impossible de réactiver un compte qui n'est pas désactivé. Statut actuel : " + status
            );
        }
        status = UserStatus.ACTIVE;
        registerEvent(new UserActivated(id));
    }

    /**
     * Désactive un compte actif (ACTIVE → DISABLED).
     *
     * <p>Émet {@link UserDisabled}.</p>
     *
     * @throws IllegalStateException si le statut n'est pas {@link UserStatus#ACTIVE}
     */
    public void disable() {
        if (status != UserStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Impossible de désactiver un compte qui n'est pas activé. Statut actuel : " + status
            );
        }
        status = UserStatus.DISABLED;
        registerEvent(new UserDisabled(id));
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

    public UserRole role() {
        return role;
    }

    public UserStatus status() {
        return status;
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
                '}';
    }
}
