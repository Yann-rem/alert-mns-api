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
 * {@link ConversationKind#GROUP}, adossées à un groupe du BC Organisation (relation 1:1), des conversations
 * {@link ConversationKind#DIRECT} entre deux membres. Le genre d'une conversation est fixé à la création.</p>
 *
 * <p>Événements : {@link ConversationCreated}, {@link ConversationRenamed}, {@link DirectConversationCreated}.</p>
 */
public final class Conversation extends AggregateRoot {

    private final ConversationId id;
    private final OrganisationId organisationId;
    private final UUID groupId;
    private ConversationName name;
    private final ConversationKind kind;
    private final ParticipantPair participantPair;
    private final Instant createdAt;

    private Conversation(
            ConversationId id,
            OrganisationId organisationId,
            UUID groupId,
            ConversationName name,
            ConversationKind kind,
            ParticipantPair participantPair,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.organisationId = Objects.requireNonNull(organisationId, "organisationId must not be null");
        this.groupId = groupId;
        this.name = name;
        this.kind = Objects.requireNonNull(kind, "kind must not be null");
        this.participantPair = participantPair;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    /**
     * Crée la conversation d'un groupe.
     *
     * <p>Émet {@link ConversationCreated}.</p>
     *
     * @param organisationId l'identifiant de l'organisation de rattachement
     * @param groupId        l'identifiant du groupe
     * @param name           le nom de la conversation, hérité du groupe
     * @param now            instant de l'opération
     * @return la nouvelle conversation de genre GROUP
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
     * <p>Émet {@link DirectConversationCreated}. La paire est déjà distincte et rangée dans un ordre canonique : c'est
     * l'invariant porté par {@link ParticipantPair}.</p>
     *
     * @param organisationId  l'identifiant de l'organisation de rattachement
     * @param participantPair la paire ordonnée des deux participants
     * @param now             instant de l'opération
     * @return la nouvelle conversation de genre DIRECT
     */
    public static Conversation createDirect(
            OrganisationId organisationId,
            ParticipantPair participantPair,
            Instant now
    ) {
        Objects.requireNonNull(participantPair, "participantPair must not be null");

        Conversation conversation = new Conversation(
                ConversationId.generate(),
                organisationId,
                null,
                null,
                ConversationKind.DIRECT,
                participantPair,
                now
        );

        conversation.registerEvent(new DirectConversationCreated(
                conversation.id,
                organisationId,
                participantPair,
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
            ParticipantPair participantPair,
            Instant createdAt
    ) {
        return new Conversation(id, organisationId, groupId, name, kind, participantPair, createdAt);
    }

    /**
     * Renomme la conversation d'un groupe.
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

    public ParticipantPair participantPair() {
        return participantPair;
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
                ", participantPair=" + participantPair +
                '}';
    }
}
