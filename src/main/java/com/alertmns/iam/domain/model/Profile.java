package com.alertmns.iam.domain.model;

import java.util.Objects;
import java.util.Optional;

/**
 * Value Object représentant le profil d'un utilisateur.
 */
public final class Profile {

    private final FirstName firstName;
    private final LastName lastName;
    private final String avatar;
    private final Optional<AbsenceMessage> absenceMessage;

    private Profile(
            FirstName firstName,
            LastName lastName,
            String avatar,
            Optional<AbsenceMessage> absenceMessage
    ) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.avatar = avatar;
        this.absenceMessage = absenceMessage;
    }

    /**
     * Crée un profil avec un prénom et un nom.
     *
     * @param firstName le prénom
     * @param lastName  le nom
     * @return le profil créé
     */
    public static Profile of(FirstName firstName, LastName lastName) {
        Objects.requireNonNull(firstName, "firstName must not be null");
        Objects.requireNonNull(lastName, "lastName must not be null");
        return new Profile(firstName, lastName, null, Optional.empty());
    }

    /**
     * Retourne une copie du profil avec l'avatar spécifié.
     *
     * @param avatar l'URL de l'avatar (nullable)
     * @return le profil avec l'avatar
     */
    public Profile withAvatar(String avatar) {
        return new Profile(firstName, lastName, avatar, absenceMessage);
    }

    /**
     * Retourne une copie du profil avec le message d'absence spécifié.
     *
     * @param absenceMessage le message d'absence
     * @return le profil avec le message d'absence
     */
    public Profile withAbsenceMessage(AbsenceMessage absenceMessage) {
        Objects.requireNonNull(absenceMessage, "absenceMessage must not be null");
        return new Profile(firstName, lastName, avatar, Optional.of(absenceMessage));
    }

    /**
     * Retourne une copie du profil avec le message d'absence activé.
     *
     * <p>Si aucun message d'absence n'est configuré, retourne le profil inchangé.</p>
     *
     * @return le profil avec le message d'absence activé
     */
    public Profile activateAbsenceMessage() {
        return absenceMessage
                .map(message -> new Profile(
                        firstName, lastName, avatar, Optional.of(message.activate()))
                )
                .orElse(this);
    }

    /**
     * Retourne une copie du profil avec le message d'absence désactivé.
     *
     * <p>Si aucun message d'absence n'est configuré, retourne le profil inchangé.</p>
     *
     * @return le profil avec le message d'absence désactivé
     */
    public Profile deactivateAbsenceMessage() {
        return absenceMessage
                .map(message -> new Profile(
                        firstName, lastName, avatar, Optional.of(message.deactivate()))
                )
                .orElse(this);
    }

    public FirstName firstName() {
        return firstName;
    }

    public LastName lastName() {
        return lastName;
    }

    public Optional<String> avatar() {
        return Optional.ofNullable(avatar);
    }

    public Optional<AbsenceMessage> absenceMessage() {
        return absenceMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Profile that = (Profile) o;
        return Objects.equals(firstName, that.firstName) &&
                Objects.equals(lastName, that.lastName) &&
                Objects.equals(avatar, that.avatar) &&
                Objects.equals(absenceMessage, that.absenceMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, avatar, absenceMessage);
    }

    @Override
    public String toString() {
        return "Profile{" +
                "firstName=" + firstName +
                ", lastName=" + lastName +
                '}';
    }
}
