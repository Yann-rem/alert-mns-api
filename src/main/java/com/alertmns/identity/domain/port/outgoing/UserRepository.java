package com.alertmns.identity.domain.port.outgoing;

import com.alertmns.identity.domain.model.User;
import com.alertmns.shared.Email;
import com.alertmns.shared.UserId;

import java.util.Optional;

/**
 * Port sortant pour la persistance des utilisateurs.
 */
public interface UserRepository {

    /**
     * Sauvegarde un utilisateur (création ou mise à jour).
     *
     * @param user l'utilisateur à sauvegarder
     */
    void save(User user);

    /**
     * Recherche un utilisateur par son identifiant.
     *
     * @param id l'identifiant de l'utilisateur
     * @return l'utilisateur trouvé, ou vide
     */
    Optional<User> findById(UserId id);

    /**
     * Recherche un utilisateur par son email.
     *
     * @param email l'email recherché
     * @return l'utilisateur trouvé, ou vide
     */
    Optional<User> findByEmail(Email email);

    /**
     * Vérifie si un email est déjà utilisé.
     *
     * @param email l'email à vérifier
     * @return {@code true} si l'email existe déjà
     */
    boolean existsByEmail(Email email);
}
