package com.alertmns.identity.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("AbsenceMessage")
class AbsenceMessageTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create an active absence message")
        void shouldCreateAnActiveAbsenceMessage() {
            String content = "Je ne suis pas disponible pour le moment";
            AbsenceMessage message = AbsenceMessage.of(content, true);
            assertEquals(content, message.content());
            assertTrue(message.active());
        }

        @Test
        @DisplayName("should create an inactive absence message")
        void shouldCreateAnInactiveAbsenceMessage() {
            String content = "Je ne suis pas disponible pour le moment";
            AbsenceMessage message = AbsenceMessage.of(content, false);
            assertEquals(content, message.content());
            assertFalse(message.active());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null content")
        void shouldRejectNullContent() {
            assertThrows(NullPointerException.class, () -> AbsenceMessage.of(null, true));
        }

        @Test
        @DisplayName("should reject blank content")
        void shouldRejectBlankContent() {
            assertThrows(IllegalArgumentException.class, () -> AbsenceMessage.of(" ", true));
        }
    }

    @Nested
    @DisplayName("Behaviour")
    class Behaviour {

        @Test
        @DisplayName("activate should return a new active message")
        void activateShouldReturnANewActiveMessage() {
            AbsenceMessage original = AbsenceMessage.of("Je ne suis pas disponible pour le moment", false);
            AbsenceMessage activated = original.activate();
            assertNotSame(original, activated);
            assertTrue(activated.active());
        }

        @Test
        @DisplayName("deactivate should return a new inactive message")
        void deactivateShouldReturnANewInactiveMessage() {
            AbsenceMessage original = AbsenceMessage.of("Je ne suis pas disponible pour le moment", true);
            AbsenceMessage deactivated = original.deactivate();
            assertNotSame(original, deactivated);
            assertFalse(deactivated.active());
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("two identical messages should be equal")
        void twoIdenticalMessagesShouldBeEqual() {
            AbsenceMessage message1 = AbsenceMessage.of("Je ne suis pas disponible pour le moment", true);
            AbsenceMessage message2 = AbsenceMessage.of("Je ne suis pas disponible pour le moment", true);
            assertEquals(message1, message2);
        }

        @Test
        @DisplayName("two messages with different content should not be equal")
        void twoMessagesWithDifferentContentShouldNotBeEqual() {
            AbsenceMessage message1 = AbsenceMessage.of("Je ne suis pas disponible pour le moment", true);
            AbsenceMessage message2 = AbsenceMessage.of("Je suis indisponible pour le moment", true);
            assertNotEquals(message1, message2);
        }

        @Test
        @DisplayName("two messages with same content but different status should not be equal")
        void twoMessagesWithSameContentButDifferentStatusShouldNotBeEqual() {
            AbsenceMessage message1 = AbsenceMessage.of("Je ne suis pas disponible pour le moment", true);
            AbsenceMessage message2 = AbsenceMessage.of("Je ne suis pas disponible pour le moment", false);
            assertNotEquals(message1, message2);
        }
    }
}
