package com.alertmns.messaging.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ParticipantPair")
class ParticipantPairTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("of should order the two members canonically (low < high)")
        void ofShouldOrderCanonically() {
            UUID memberA = UUID.randomUUID();
            UUID memberB = UUID.randomUUID();

            ParticipantPair pair = ParticipantPair.of(memberA, memberB);

            UUID expectedLow = memberA.compareTo(memberB) < 0 ? memberA : memberB;
            UUID expectedHigh = expectedLow == memberA ? memberB : memberA;
            assertEquals(expectedLow, pair.low());
            assertEquals(expectedHigh, pair.high());
            assertTrue(pair.low().compareTo(pair.high()) < 0);
        }

        @Test
        @DisplayName("of should yield the same pair regardless of argument order")
        void ofShouldBeOrderIndependent() {
            UUID memberA = UUID.randomUUID();
            UUID memberB = UUID.randomUUID();

            assertEquals(ParticipantPair.of(memberA, memberB), ParticipantPair.of(memberB, memberA));
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("of should reject two identical members")
        void ofShouldRejectIdenticalMembers() {
            UUID member = UUID.randomUUID();
            assertThrows(IllegalArgumentException.class, () -> ParticipantPair.of(member, member));
        }

        @Test
        @DisplayName("of should reject null members")
        void ofShouldRejectNullMembers() {
            UUID member = UUID.randomUUID();
            assertThrows(NullPointerException.class, () -> ParticipantPair.of(null, member));
            assertThrows(NullPointerException.class, () -> ParticipantPair.of(member, null));
        }

        @Test
        @DisplayName("the compact constructor should reject null components")
        void compactConstructorShouldRejectNulls() {
            UUID member = UUID.randomUUID();
            assertThrows(NullPointerException.class, () -> new ParticipantPair(null, member));
            assertThrows(NullPointerException.class, () -> new ParticipantPair(member, null));
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("two pairs with the same low and high should be equal")
        void equalWhenSameComponents() {
            UUID low = UUID.fromString("11111111-1111-1111-1111-111111111111");
            UUID high = UUID.fromString("22222222-2222-2222-2222-222222222222");

            assertEquals(new ParticipantPair(low, high), new ParticipantPair(low, high));
        }
    }
}
