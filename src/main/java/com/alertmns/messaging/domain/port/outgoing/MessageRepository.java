package com.alertmns.messaging.domain.port.outgoing;

import com.alertmns.messaging.domain.model.ConversationId;
import com.alertmns.messaging.domain.model.Message;
import com.alertmns.messaging.domain.model.MessageId;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Port sortant pour la persistance des messages.
 */
public interface MessageRepository {

    void save(Message message);

    Optional<Message> findById(MessageId messageId);

    /**
     * Retourne une page de messages d'une conversation, les plus récents d'abord.
     *
     * @param page index de page (commence à 0)
     * @param size taille de page
     */
    List<Message> findByConversationId(ConversationId conversationId, int page, int size);

    /**
     * Dernier message de chacune des conversations données.
     *
     * <p>Requête unique et non paginée : la liste des conversations d'un membre est bornée par son
     * organisation, et interroger chaque conversation séparément produirait un N+1 sur l'écran le
     * plus fréquenté de l'application.</p>
     *
     * @param conversationIds les conversations à sonder
     * @return le dernier message par conversation ; une conversation sans message est absente
     */
    Map<ConversationId, Message> findLastMessagePerConversation(Collection<ConversationId> conversationIds);
}
