package com.alertmns.alerting.domain.model;

import com.alertmns.alerting.domain.event.AlertBroadcast;
import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.OrganisationId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Alert")
class AlertTest {

    static final OrganisationId ORGANISATION_ID = OrganisationId.generate();
    static final UUID ISSUER_ID = UUID.randomUUID();
    static final AlertContent CONTENT = AlertContent.of("Fermeture exceptionnelle demain");
    static final AlertAudience AUDIENCE = AlertAudience.organisation();
    static final Instant NOW = Instant.parse("2026-06-15T08:00:00Z");

    @Nested
    @DisplayName("Broadcast")
    class Broadcast {

        @Test
        @DisplayName("should carry the broadcast data")
        void carriesData() {
            Alert alert = Alert.broadcast(ORGANISATION_ID, ISSUER_ID, CONTENT, AUDIENCE, AlertLevel.URGENT, NOW);

            assertEquals(ORGANISATION_ID, alert.organisationId());
            assertEquals(ISSUER_ID, alert.issuerId());
            assertEquals(CONTENT, alert.content());
            assertEquals(AUDIENCE, alert.audience());
            assertEquals(AlertLevel.URGENT, alert.level());
            assertEquals(NOW, alert.issuedAt());
        }

        @Test
        @DisplayName("should generate an identifier")
        void generatesId() {
            Alert alert = Alert.broadcast(ORGANISATION_ID, ISSUER_ID, CONTENT, AUDIENCE, AlertLevel.INFO, NOW);

            assertNotNull(alert.id());
        }

        @Test
        @DisplayName("should emit a self-contained AlertBroadcast event")
        void emitsAlertBroadcast() {
            Alert alert = Alert.broadcast(ORGANISATION_ID, ISSUER_ID, CONTENT, AUDIENCE, AlertLevel.URGENT, NOW);

            List<DomainEvent> events = alert.pullDomainEvents();

            assertEquals(1, events.size());
            AlertBroadcast event = assertInstanceOf(AlertBroadcast.class, events.get(0));
            assertEquals(alert.id(), event.alertId());
            assertEquals(ORGANISATION_ID, event.organisationId());
            assertEquals(ISSUER_ID, event.issuerId());
            assertEquals(CONTENT, event.content());
            assertEquals(AUDIENCE, event.audience());
            assertEquals(AlertLevel.URGENT, event.level());
            assertEquals(NOW, event.occurredOn());
        }
    }

    @Nested
    @DisplayName("Reconstitute")
    class Reconstitute {

        @Test
        @DisplayName("should not emit any domain event")
        void emitsNoEvent() {
            Alert alert = Alert.reconstitute(
                    AlertId.generate(), ORGANISATION_ID, ISSUER_ID, CONTENT, AUDIENCE, AlertLevel.INFO, NOW);

            assertTrue(alert.pullDomainEvents().isEmpty());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject a null identifier")
        void rejectsNullId() {
            assertThrows(NullPointerException.class, () -> Alert.reconstitute(
                    null, ORGANISATION_ID, ISSUER_ID, CONTENT, AUDIENCE, AlertLevel.INFO, NOW));
        }

        @Test
        @DisplayName("should reject a null issuer")
        void rejectsNullIssuer() {
            assertThrows(NullPointerException.class, () -> Alert.broadcast(
                    ORGANISATION_ID, null, CONTENT, AUDIENCE, AlertLevel.INFO, NOW));
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("should be equal for the same identifier")
        void equalForSameId() {
            AlertId id = AlertId.generate();
            Alert a = Alert.reconstitute(id, ORGANISATION_ID, ISSUER_ID, CONTENT, AUDIENCE, AlertLevel.INFO, NOW);
            Alert b = Alert.reconstitute(id, ORGANISATION_ID, ISSUER_ID, CONTENT, AUDIENCE, AlertLevel.URGENT, NOW);

            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("should not be equal for different identifiers")
        void notEqualForDifferentId() {
            Alert a = Alert.reconstitute(
                    AlertId.generate(), ORGANISATION_ID, ISSUER_ID, CONTENT, AUDIENCE, AlertLevel.INFO, NOW);
            Alert b = Alert.reconstitute(
                    AlertId.generate(), ORGANISATION_ID, ISSUER_ID, CONTENT, AUDIENCE, AlertLevel.INFO, NOW);

            assertNotEquals(a, b);
        }
    }
}
