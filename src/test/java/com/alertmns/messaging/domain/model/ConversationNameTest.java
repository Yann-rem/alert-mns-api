package com.alertmns.messaging.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ConversationName")
class ConversationNameTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create a conversation name")
        void shouldCreateAConversationName() {
            String value = "Général";
            ConversationName name = ConversationName.of(value);
            assertEquals("Général", name.value());
        }

        @Test
        @DisplayName("should normalize conversation name by trimming spaces")
        void shouldNormalizeConversationName() {
            String value = " Général ";
            ConversationName name = ConversationName.of(value);
            assertEquals("Général", name.value());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null conversation name")
        void shouldRejectNullConversationName() {
            String value = null;
            assertThrows(NullPointerException.class, () -> ConversationName.of(value));
        }

        @Test
        @DisplayName("should reject blank conversation name")
        void shouldRejectBlankConversationName() {
            String value = " ";
            assertThrows(IllegalArgumentException.class, () -> ConversationName.of(value));
        }

        @Test
        @DisplayName("should accept conversation name with max length")
        void shouldAcceptConversationNameWithMaxLength() {
            String value = "a".repeat(150);
            ConversationName name = ConversationName.of(value);
            assertEquals(value, name.value());
        }

        @Test
        @DisplayName("should reject conversation name exceeding max length")
        void shouldRejectConversationNameExceedingMaxLength() {
            String value = "a".repeat(151);
            assertThrows(IllegalArgumentException.class, () -> ConversationName.of(value));
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("two conversation names with same value should be equal")
        void twoConversationNamesWithSameValueShouldBeEqual() {
            ConversationName name1 = ConversationName.of("Général");
            ConversationName name2 = ConversationName.of("Général");
            assertEquals(name1, name2);
        }

        @Test
        @DisplayName("two conversation names with different values should not be equal")
        void twoConversationNamesWithDifferentValuesShouldNotBeEqual() {
            ConversationName name1 = ConversationName.of("Général");
            ConversationName name2 = ConversationName.of("Projet X");
            assertNotEquals(name1, name2);
        }
    }
}
