package com.alertmns.messaging.infrastructure.adapter.incoming.ws;

import java.util.UUID;

/**
 * Signal éphémère « un utilisateur est en train d'écrire » relayé aux autres participants d'une conversation.
 *
 * <p>N'est jamais persisté et ne donne lieu à aucun agrégat.</p>
 *
 * <p><b>Pourquoi le nom est porté par la charge utile.</b> Ce signal ne transportait initialement que le
 * {@code userId}, en supposant que le client résolve le nom localement. Cette hypothèse est fausse : le client ne
 * manipule que des {@code memberId} (auteurs des messages, participants d'une conversation) et n'a aucun moyen
 * d'associer un {@code userId} à un nom. Le résoudre ici est un unique lookup indexé, largement absorbé par la
 * limitation de fréquence côté client.</p>
 */
public record TypingNotification(UUID conversationId, UUID userId, String userName) {}
