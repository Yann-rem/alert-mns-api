package com.alertmns.organisation.domain.port.incoming.command;

/**
 * Critères de listage des membres d'une organisation.
 *
 * @param organisationId identifiant de l'organisation, obligatoire
 * @param status         statut du membre (ACTIVE, SUSPENDED…), ou {@code null} pour ne pas filtrer
 * @param role           rôle du membre (ADMIN, MEMBER), ou {@code null} pour ne pas filtrer
 * @param search         recherche textuelle sur nom, prénom ou e-mail, ou {@code null}/vide
 * @param groupId        restreint aux membres du groupe désigné, ou {@code null} pour ne pas filtrer
 * @param page           index de page, à partir de 0
 * @param size           taille de page
 */
public record ListMembersQuery(
        String organisationId,
        String status,
        String role,
        String search,
        String groupId,
        int page,
        int size
) {}
