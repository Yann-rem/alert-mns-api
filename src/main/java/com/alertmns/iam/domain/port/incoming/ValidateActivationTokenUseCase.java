package com.alertmns.iam.domain.port.incoming;

import com.alertmns.iam.domain.port.incoming.query.ValidateActivationTokenQuery;
import com.alertmns.iam.domain.port.incoming.result.ActivationTokenContext;

/**
 * Port entrant représentant le cas d'utilisation de validation d'un token d'activation.
 *
 * <p>Lecture pure (CQRS query) : vérifie que le token existe, n'est pas expiré, et retourne le contexte utilisateur
 * nécessaire à l'affichage de la page d'activation. Aucune mutation d'état.</p>
 *
 * <p>Implémenté par {@link com.alertmns.iam.application.ValidateActivationTokenService}.</p>
 */
public interface ValidateActivationTokenUseCase {

    /**
     * Valide un token d'activation et retourne le contexte utilisateur associé.
     *
     * @param query la query portant le raw token à valider
     * @return le contexte (email + identité) à afficher sur la page d'activation
     */
    ActivationTokenContext validate(ValidateActivationTokenQuery query);
}
