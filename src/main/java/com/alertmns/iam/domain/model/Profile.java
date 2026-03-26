package com.alertmns.iam.domain.model;

import java.util.Objects;
import java.util.Optional;

/**
 * Profil d'un utilisateur.
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

    public static Profile of(FirstName firstName, LastName lastName) {
        Objects.requireNonNull(firstName, "Le prénom ne peut pas être null");
        Objects.requireNonNull(lastName, "le nom ne peut pas être null");
        return new Profile(firstName, lastName, null, Optional.empty());
    }

    public Profile withAvatar(String avatar) {
        return new Profile(firstName, lastName, avatar, absenceMessage);
    }

    public Profile withAbsenceMessage(AbsenceMessage absenceMessage) {
        Objects.requireNonNull(absenceMessage, "Le message d'absence ne peut pas être null");
        return new Profile(firstName, lastName, avatar, Optional.of(absenceMessage));
    }

    public Profile activateAbsenceMessage() {
        return absenceMessage
                .map(message -> new Profile(
                        firstName, lastName, avatar, Optional.of(message.activate()))
                )

                // TODO : décision métier — lever une exception ou ne rien faire en cas de message d'absence non configuré ?
                .orElse(this);
    }

    public Profile deactivateAbsenceMessage() {
        return absenceMessage
                .map(message -> new Profile(
                        firstName, lastName, avatar, Optional.of(message.deactivate()))
                )
                // TODO : décision métier — lever une exception ou ne rien faire en cas de message d'absence non configuré ?
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
        return firstName + " " + lastName;
    }
}
