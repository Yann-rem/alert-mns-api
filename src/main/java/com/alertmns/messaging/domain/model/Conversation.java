package com.alertmns.messaging.domain.model;

import com.alertmns.messaging.domain.event.ConversationCreated;
import com.alertmns.messaging.domain.event.ConversationRenamed;
import com.alertmns.messaging.domain.event.DirectConversationCreated;
import com.alertmns.shared.AggregateRoot;
import com.alertmns.shared.OrganisationId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Agrégat racine représentant une conversation dans le BC Messaging.
 *
 * <p>Une conversation est rattachée à une seule organisation. Son {@link ConversationKind} distingue les conversations
 * {@link ConversationKind#GROUP}, adossées à un {@code Group} du BC Organisation (relation 1:1), des conversations
 * {@link ConversationKind#DIRECT} (messages directs entre deux membres). Le {@code kind} est fixé à la création.</p>
 *
 * <p>Événements : {@link ConversationCreated}, {@link ConversationRenamed}, {@link DirectConversationCreated}.</p>
 */
public final class Conversation extends AggregateRoot {

    private final ConversationId id;
    private final OrganisationId organisationId;
    private final UUID groupId;
    private ConversationName name;
    private final ConversationKind kind;
    private final UUID participantLow;
    private final UUID participantHigh;
    private final Instant createdAt;

    private Conversation(
            ConversationId id,
            OrganisationId organisationId,
            UUID groupId,
            ConversationName name,
            ConversationKind kind,
            UUID participantLow,
            UUID participantHigh,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.organisationId = Objects.requireNonNull(organisationId, "organisationId must not be null");
        this.groupId = groupId;
        this.name = name;
        this.kind = Objects.requireNonNull(kind, "kind must not be null");
        this.participantLow = participantLow;
        this.participantHigh = participantHigh;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    /**
     * Crée la conversation adossée à un groupe
     *
     * <p>Émet {@link ConversationCreated}.</p>
     *
     * @param organisationId l'identifiant de l'organisation de rattachement
     * @param groupId        l'identifiant du groupe source
     * @param name           le nom de la conversation, hérité du groupe
     * @param now            instant de l'opération
     * @return la nouvelle conversation créée, de genre GROUP
     */
    public static Conversation createForGroup(
            OrganisationId organisationId,
            UUID groupId,
            ConversationName name,
            Instant now
    ) {
        Objects.requireNonNull(groupId, "groupId must not be null");
        Objects.requireNonNull(name, "name must not be null");

        Conversation conversation = new Conversation(
                ConversationId.generate(),
                organisationId,
                groupId,
                name,
                ConversationKind.GROUP,
                null,
                null,
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
     * Crée une conversation directe entre deux membres.
     *
     * <p>Les deux participants sont stockés dans un ordre canonique (plus petit identifiant en premier), ce qui
     * garantit l'unicité de la paire quel que soit l'initiateur : {@code DM(A,B)} et {@code DM(B,A)} désignent la
     * même conversation.</p>
     *
     * <p>Émet {@link DirectConversationCreated}</p>
     *
     * @param organisationId l'identifiant de l'organisation de rattachement
     * @param firstMemberId  l'identifiant d'un des deux membres (l'ordre des deux n'importe pas)
     * @param secondMemberId l'identifiant de l'autre membre
     * @param now            instant de l'opération
     * @return la nouvelle conversation directe, de genre DIRECT
     * @throws IllegalArgumentException si les deux membres sont identiques
     */
    public static Conversation createDirect(
            OrganisationId organisationId,
            UUID firstMemberId,
            UUID secondMemberId,
            Instant now
    ) {
        Objects.requireNonNull(firstMemberId, "firstMemberId must not be null");
        Objects.requireNonNull(secondMemberId, "secondMemberId must not be null");
        if (firstMemberId.equals(secondMemberId)) {
            throw new IllegalArgumentException("A direct conversation requires two distinct members");
        }

        UUID participantLow = firstMemberId.compareTo(secondMemberId) < 0 ? firstMemberId : secondMemberId;
        UUID participantHigh = participantLow == firstMemberId ? secondMemberId : firstMemberId;

        Conversation conversation = new Conversation(
                ConversationId.generate(),
                organisationId,
                null,
                null,
                ConversationKind.DIRECT,
                participantLow,
                participantHigh,
                now
        );

        conversation.registerEvent(new DirectConversationCreated(
                conversation.id,
                organisationId,
                participantLow,
                participantHigh,
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
            UUID participantLow,
            UUID participantHigh,
            Instant createdAt
    ) {
        return new Conversation(id, organisationId, groupId, name, kind, participantLow, participantHigh, createdAt);
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

    public UUID participantLow() {
        return participantLow;
    }

    public UUID participantHigh() {
        return participantHigh;
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
                ", kind=" + kind +
                ", groupId=" + groupId +
                ", name=" + name +
                ", participantLow=" + participantLow +
                ", participantHigh=" + participantHigh +
                '}';
    }
}
