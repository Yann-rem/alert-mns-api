package com.alertmns.iam.domain.port.outgoing;

import com.alertmns.iam.domain.model.ActivationToken;
import com.alertmns.iam.domain.model.TokenHash;
import com.alertmns.shared.UserId;

import java.util.Optional;

/**
 * Port sortant pour la persistance des tokens d'activation.
 */
public interface ActivationTokenRepository {

    /**
     * Sauvegarde un token d'activation (création ou mise à jour).
     *
     * @param token le token à sauvegarder
     */
    void save(ActivationToken token);

    /**
     * Recherche un token par son hash.
     *
     * @param hash le hash recherché
     * @return le token trouvé, ou vide
     */
    Optional<ActivationToken> findByTokenHash(TokenHash hash);

    /**
     * Supprime tous les tokens associés à un utilisateur.
     *
     * <p>Utilisé à la ré-émission (remplacement du token précédent) et à la consommation (le token est supprimé après
     * usage).</p>
     *
     * @param userId l'utilisateur dont les tokens doivent être supprimés
     */
    void deleteByUserId(UserId userId);
}
