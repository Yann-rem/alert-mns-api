package com.alertmns.messaging.domain.model;

import com.alertmns.messaging.domain.event.ConversationCreated;
import com.alertmns.messaging.domain.event.ConversationRenamed;
import com.alertmns.shared.AggregateRoot;
import com.alertmns.shared.OrganisationId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Agrégat racine représentant une conversation dans le BC Messaging.
 *
 * <p>Une conversation est rattachée à une seule organisation. Son {@link ConversationKind} distingue les conversations
 * {@link ConversationKind#GROUP}, adossées à un {@code Group} du BC Organisation (relation 1:1), des futures
 * conversations {@link ConversationKind#DIRECT} (messages directs). Le {@code kind} est fixé à la création.</p>
 *
 * <p>Événements : {@link ConversationCreated}.</p>
 */
public final class Conversation extends AggregateRoot {

    private final ConversationId id;
    private final OrganisationId organisationId;
    private final UUID groupId;
    private ConversationName name;
    private final ConversationKind kind;
    private final Instant createdAt;

    private Conversation(
            ConversationId id,
            OrganisationId organisationId,
            UUID groupId,
            ConversationName name,
            ConversationKind kind,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.organisationId = Objects.requireNonNull(organisationId, "organisationId must not be null");
        this.groupId = Objects.requireNonNull(groupId, "groupId must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.kind = Objects.requireNonNull(kind, "kind must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    /**
     * Crée la conversation adossée à un groupe
     *
     * <p>Émet {@link ConversationCreated}.</p>
     *
     * @param groupId l'identifiant du groupe source
     * @param name    le nom de la conversation, hérité du groupe
     * @param now     instant de l'opération
     * @return la nouvelle conversation créée, de genre GROUP
     */
    public static Conversation createForGroup(
            OrganisationId organisationId,
            UUID groupId,
            ConversationName name,
            Instant now
    ) {
        Conversation conversation = new Conversation(
                ConversationId.generate(),
                organisationId,
                groupId,
                name,
                ConversationKind.GROUP,
                now
        );

        conversation.registerEvent(new ConversationCreated(
                conversation.id,
                organisationId,
                groupId,
                conversation.kind,
                now
        ));
        return conversation;
    }

    /**
     * Reconstruit une conversation existante depuis la persistence.
     *
     * <p>Aucun événement de domaine n'est émis.</p>
     *
     * @return la conversation reconstituée
     */
    public static Conversation reconstitute(
            ConversationId id,
            OrganisationId organisationId,
            UUID groupId,
            ConversationName name,
            ConversationKind kind,
            Instant createdAt
    ) {
        return new Conversation(id, organisationId, groupId, name, kind, createdAt);
    }

    /**
     * Renomme la conversation.
     *
     * <p>Émet {@link ConversationRenamed}. Idempotent : no-op si le nom est inchangé.</p>
     *
     * @param now instant de l'opération
     */
    public void rename(ConversationName name, Instant now) {
        Objects.requireNonNull(name, "name must not be null");
        if (this.name.equals(name)) {
            return;
        }
        this.name = name;
        registerEvent(new ConversationRenamed(id, organisationId, name, now));
    }

    public ConversationId id() {
        return id;
    }

    public OrganisationId organisationId() {
        return organisationId;
    }

    public UUID groupId() {
        return groupId;
    }

    public ConversationName name() {
        return name;
    }

    public ConversationKind kind() {
        return kind;
    }

    public Instant createdAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Conversation that = (Conversation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Conversation{" +
                "id=" + id +
                ", organisationId=" + organisationId +
                ", groupId=" + groupId +
                ", name=" + name +
                ", kind=" + kind +
                '}';
    }
}
