package com.alertmns.alerting.domain.port.incoming.command;

/**
 * Commande de diffusion d'une alerte.
 *
 * <p>Données brutes issues de l'adapter entrant ; le service applicatif les traduit en Value Objects. Le membre
 * émetteur et l'organisation ne figurent pas ici : ils sont dérivés de l'utilisateur authentifié courant.</p>
 *
 * @param content      contenu textuel de l'alerte
 * @param level        niveau de gravité ({@code INFO}, {@code IMPORTANT} ou {@code URGENT})
 * @param audienceKind genre d'audience ({@code ORGANISATION} ou {@code GROUP})
 * @param groupId      identifiant du groupe ciblé — requis si {@code audienceKind == GROUP}, sinon {@code null}
 */
public record BroadcastAlertCommand(String content, String level, String audienceKind, String groupId) {}
