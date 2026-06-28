package com.alertmns.messaging.infrastructure.adapter.outgoing.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
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
        name = "messages",
        indexes = @Index(name = "idx_message_conversation", columnList = "conversation_id")
)
public class MessageJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID conversationId;

    @Column(nullable = false)
    private UUID authorId;

    @Column(nullable = false, length = 4000)
    private String content;

    @Column(nullable = true)
    private UUID replyTo;

    @Column(nullable = false)
    private Instant sentAt;
}
