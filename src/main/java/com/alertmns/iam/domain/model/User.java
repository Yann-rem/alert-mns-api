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
 * Représente un utilisateur avec son cycle de vie et ses règles métiers.
 */
public final class User extends AggregateRoot {

    private final UserId id;
    private final Email email;
    private final HashedPassword hashedPassword;
    private Profile profile;
    private final Role role;
    private UserStatus status;
    private final Instant createdAt;

    private User(
            UserId id,
            Email email,
            HashedPassword hashedPassword,
            Profile profile,
            Role role,
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
                Role.USER,
                UserStatus.PENDING,
                Instant.now()
        );

        user.registerEvent(new UserRegistered(user.id, user.email, user.role));
        return user;
    }

    public static User reconstitute(
            UserId id,
            Email email,
            HashedPassword hashedPassword,
            Profile profile,
            Role role,
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

    public void updateProfile(FirstName firstName, LastName lastName, String avatar) {
        profile = Profile.of(firstName, lastName).withAvatar(avatar);
        registerEvent(new ProfileUpdated(id));
    }

    public void updateAbsenceMessage(AbsenceMessage absenceMessage) {
        Objects.requireNonNull(absenceMessage, "Le message d'absence ne peut pas être null");
        profile = profile.withAbsenceMessage(absenceMessage);
        registerEvent(new AbsenceMessageUpdated(id));
    }

    // TODO : ajouter la personne responsable de l'activation.
    public void activate() {
        if (status != UserStatus.PENDING) {
            throw new IllegalStateException(
                    "Impossible d'activer un compte qui n'est pas en attente. Statut actuel : " + status
            );
        }
        status = UserStatus.ACTIVE;
        registerEvent(new UserActivated(id));
    }

    public void reactivate() {
        if (status != UserStatus.DISABLED) {
            throw new IllegalStateException(
                    "Impossible de réactiver un compte qui n'est pas désactivé. Statut actuel : " + status
            );
        }
        status = UserStatus.ACTIVE;
        registerEvent(new UserActivated(id));
    }

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

    public Role role() {
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
