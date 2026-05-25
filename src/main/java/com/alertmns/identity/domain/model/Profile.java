package com.alertmns.identity.domain.model;

import java.util.Objects;
import java.util.Optional;

/**
 * Value Object représentant le profil d'un utilisateur.
 */
public record Profile(
        FirstName firstName,
        LastName lastName,
        Optional<String> avatar,
        Optional<AbsenceMessage> absenceMessage
) {

    public Profile {
        Objects.requireNonNull(firstName, "firstName must not be null");
        Objects.requireNonNull(lastName, "lastName must not be null");
        Objects.requireNonNull(avatar, "avatar must not be null");
        Objects.requireNonNull(absenceMessage, "absenceMessage must not be null");
    }

    /**
     * Crée un profil avec un prénom et un nom.
     *
     * @param firstName le prénom
     * @param lastName  le nom
     * @return le profil créé
     */
    public static Profile of(FirstName firstName, LastName lastName) {
        return of(firstName, lastName, null);
    }

    /**
     * Crée un profil avec un prénom, un nom et un avatar.
     *
     * @param firstName le prénom
     * @param lastName  le nom
     * @param avatar    l'URL de l'avatar (nullable)
     * @return le profil créé
     */
    public static Profile of(FirstName firstName, LastName lastName, String avatar) {
        return new Profile(firstName, lastName, Optional.ofNullable(avatar), Optional.empty());
    }

    /**
     * Retourne une copie du profil avec les informations d'identité mises à jour.
     *
     * <p>Le message d'absence est préservé.</p>
     *
     * @param firstName le nouveau prénom
     * @param lastName  le nouveau nom
     * @param avatar    l'URL de l'avatar (nullable)
     * @return le profil mis à jour
     */
    public Profile withIdentity(FirstName firstName, LastName lastName, String avatar) {
        return new Profile(firstName, lastName, Optional.ofNullable(avatar), absenceMessage);
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
                .map(message -> new Profile(firstName, lastName, avatar, Optional.of(message.activate())))
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
                .map(message -> new Profile(firstName, lastName, avatar, Optional.of(message.deactivate())))
                .orElse(this);
    }
}
