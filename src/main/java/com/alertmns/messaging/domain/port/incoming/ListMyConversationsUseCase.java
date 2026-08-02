package com.alertmns.messaging.domain.port.incoming;

import java.util.List;

/**
 * Port entrant représentant le listage des conversations de l'utilisateur courant.
 *
 * <p>Implémenté par {@link com.alertmns.messaging.application.ListMyConversationsService}.</p>
 *
 * <p>Renvoie des {@link ConversationSummary} et non des agrégats : le titre d'un DM et l'aperçu du
 * dernier message dépendent du lecteur, ils ne peuvent donc pas vivre dans l'agrégat.</p>
 */
public interface ListMyConversationsUseCase {

    List<ConversationSummary> list();
}
