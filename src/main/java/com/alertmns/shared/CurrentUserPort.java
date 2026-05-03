package com.alertmns.shared;

import java.util.Optional;

/**
 * Port transverse permettant aux services applicatifs de connaître l'utilisateur courant sans dépendre de Spring
 * Security (ni d'aucun framework d'authentification).
 *
 * <p>Implémenté côté infrastructure par un adapter qui lit le contexte de sécurité pour Spring Security.</p>
 *
 * <p>Retourne un {@link Optional} pour les contextes non authentifiés (endpoints publics, jobs asynchrones, tests).
 * Un appel sans utilisateur courant ne lève pas d'exception : c'est aux services métier d'exiger une authentification
 * s'ils en ont besoin.</p>
 */
public interface CurrentUserPort {

    /**
     * @return l'utilisateur authentifié si présent dans le contexte, {@link Optional#empty()} sinon
     */
    Optional<AuthenticatedUser> currentUser();
}
