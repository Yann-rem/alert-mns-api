package com.alertmns.identity.domain.port.incoming;

import com.alertmns.identity.domain.port.incoming.command.RedeemActivationTokenCommand;

/**
 * Port entrant représentant le cas d'utilisation de consommation d'un token d'activation.
 *
 * <p>Consomme un token d'activation à usage unique et finalise l'activation du compte utilisateur cible : transition
 * {@code PENDING → ACTIVE} avec définition du mot de passe choisi.</p>
 *
 * <p>Implémenté par {@link com.alertmns.identity.application.RedeemActivationTokenService}.</p>
 */
public interface RedeemActivationTokenUseCase {

    /**
     * Consomme un token d'activation et active le compte utilisateur.
     *
     * @param command la commande portant le raw token et le mot de passe choisi par l'utilisateur
     */
    void redeem(RedeemActivationTokenCommand command);
}
