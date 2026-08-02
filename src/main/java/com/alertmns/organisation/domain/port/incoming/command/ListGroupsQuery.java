package com.alertmns.organisation.domain.port.incoming.command;

/**
 * Critères de listage des groupes d'une organisation.
 *
 * @param organisationId identifiant de l'organisation, obligatoire
 * @param search         filtre sur le nom du groupe, ou {@code null}/vide
 * @param page           index de page, à partir de 0
 * @param size           taille de page
 */
public record ListGroupsQuery(String organisationId, String search, int page, int size) {}
