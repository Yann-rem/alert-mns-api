package com.alertmns.messaging.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("MessageContent")
class MessageContentTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create a message content")
        void shouldCreateAMessageContent() {
            MessageContent content = MessageContent.of("Bonjour");
            assertEquals("Bonjour", content.value());
        }

        @Test
        @DisplayName("should normalize message content by stripping surrounding whitespace")
        void shouldNormalizeMessageContent() {
            MessageContent content = MessageContent.of("  Bonjour  ");
            assertEquals("Bonjour", content.value());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null message content")
        void shouldRejectNullMessageContent() {
            String value = null;
            assertThrows(NullPointerException.class, () -> MessageContent.of(value));
        }

        @Test
        @DisplayName("should reject blank message content")
        void shouldRejectBlankMessageContent() {
            assertThrows(IllegalArgumentException.class, () -> MessageContent.of("   "));
        }

        @Test
        @DisplayName("should accept message content with max length")
        void shouldAcceptMessageContentWithMaxLength() {
            String value = "a".repeat(4000);
            MessageContent content = MessageContent.of(value);
            assertEquals(value, content.value());
        }

        @Test
        @DisplayName("should reject message content exceeding max length")
        void shouldRejectMessageContentExceedingMaxLength() {
            String value = "a".repeat(4001);
            assertThrows(IllegalArgumentException.class, () -> MessageContent.of(value));
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("two message contents with same value should be equal")
        void twoMessageContentsWithSameValueShouldBeEqual() {
            assertEquals(MessageContent.of("Bonjour"), MessageContent.of("Bonjour"));
        }

        @Test
        @DisplayName("two message contents with different values should not be equal")
        void twoMessageContentsWithDifferentValuesShouldNotBeEqual() {
            assertNotEquals(MessageContent.of("Bonjour"), MessageContent.of("Salut"));
        }
    }
}
