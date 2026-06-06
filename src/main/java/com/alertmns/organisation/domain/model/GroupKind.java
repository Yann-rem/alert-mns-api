package com.alertmns.organisation.domain.model;

/**
 * Nature d'un {@link Group} au sein d'une organisation.
 *
 * <p>Le {@code kind} est fixé à la création et immuable. Il distingue le canal général des groupes standards créés à
 * la demande.</p>
 */
public enum GroupKind {
    GENERAL,
    STANDARD
}
