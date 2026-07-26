package com.alertmns.identity.infrastructure.transaction;

import com.alertmns.identity.domain.port.incoming.RedeemActivationTokenUseCase;
import com.alertmns.identity.domain.port.incoming.command.RedeemActivationTokenCommand;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Décorateur transactionnel du cas d'usage {@link RedeemActivationTokenUseCase}.
 *
 * <p><b>Pourquoi un décorateur plutôt qu'un {@code @Transactional} sur le service ?</b> La couche
 * {@code application} est volontairement sans dépendance au framework (voir ADR-0022). La gestion des
 * transactions étant un détail d'infrastructure, elle est déportée ici : le service applicatif reste
 * un objet Java pur, testable sans Spring.</p>
 *
 * <p><b>Pourquoi cette opération doit être atomique</b> : {@code redeem} enchaîne trois écritures
 * (mot de passe de l'utilisateur, suppression du token consommé, puis acceptation de l'invitation
 * d'adhésion via un listener synchrone du BC <i>Organisation</i>). Sans transaction englobante, un
 * échec tardif — typiquement une invitation expirée — laissait un état incohérent : compte activé
 * mais rattaché à aucune organisation, et token déjà consommé donc impossible à rejouer.</p>
 */
// Volontairement non {@code final} : Spring crée un proxy CGLIB (sous-classe) pour appliquer @Transactional.
public class TransactionalRedeemActivationTokenUseCase implements RedeemActivationTokenUseCase {

    private final RedeemActivationTokenUseCase delegate;

    public TransactionalRedeemActivationTokenUseCase(RedeemActivationTokenUseCase delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
    }

    /**
     * Exécute l'activation dans une transaction unique : toute exception non vérifiée levée par le
     * service ou par un listener d'événement annule l'intégralité des écritures.
     */
    @Override
    @Transactional
    public void redeem(RedeemActivationTokenCommand command) {
        delegate.redeem(command);
    }
}
