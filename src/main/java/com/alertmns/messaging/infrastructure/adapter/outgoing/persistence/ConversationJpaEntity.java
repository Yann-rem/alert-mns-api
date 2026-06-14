package com.alertmns.messaging.infrastructure.adapter.outgoing.persistence;

import com.alertmns.messaging.domain.model.ConversationKind;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Entity
@Table(
        name = "conversations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_conversation_group",
                columnNames = "group_id"
        )
)
public class ConversationJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID organisationId;

    @Column(nullable = true)
    private UUID groupId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConversationKind kind;

    @Column(length = 150)
    private String name;

    @Column(nullable = false)
    private Instant createdAt;
}
