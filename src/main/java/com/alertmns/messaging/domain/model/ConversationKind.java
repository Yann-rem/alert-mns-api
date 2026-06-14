package com.alertmns.messaging.domain.model;

/**
 * Type d'une conversation : adossée à un groupe ({@link #GROUP}, relation 1:1) ou message direct ({@link #DIRECT}).
 */
public enum ConversationKind {
    GROUP,
    DIRECT
}
