package com.alertmns.alerting.application;

import com.alertmns.alerting.domain.event.AlertBroadcast;
import com.alertmns.alerting.domain.model.AlertAudience;
import com.alertmns.alerting.domain.model.AlertContent;
import com.alertmns.alerting.domain.model.AlertId;
import com.alertmns.alerting.domain.model.AlertLevel;
import com.alertmns.alerting.domain.port.outgoing.AlertNotification;
import com.alertmns.alerting.domain.port.outgoing.AlertRealtimePort;
import com.alertmns.alerting.domain.port.outgoing.AlertRecipientPort;
import com.alertmns.alerting.domain.port.outgoing.GroupDirectoryPort;
import com.alertmns.alerting.domain.port.outgoing.IssuerDirectoryPort;
import com.alertmns.shared.OrganisationId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("DispatchAlertService")
@ExtendWith(MockitoExtension.class)
class DispatchAlertServiceTest {

    static final OrganisationId ORGANISATION_ID = OrganisationId.generate();
    static final UUID ISSUER_ID = UUID.randomUUID();
    static final Instant NOW = Instant.parse("2026-05-30T10:00:00Z");

    @Mock
    AlertRecipientPort recipientPort;

    @Mock
    AlertRealtimePort realtimePort;

    @Mock
    IssuerDirectoryPort issuerDirectory;

    @Mock
    GroupDirectoryPort groupDirectory;

    @InjectMocks
    DispatchAlertService service;

    private AlertBroadcast organisationAlert(String content, AlertLevel level) {
        return new AlertBroadcast(
                AlertId.generate(), ORGANISATION_ID, ISSUER_ID,
                AlertContent.of(content), AlertAudience.organisation(), level, NOW);
    }

    private AlertBroadcast groupAlert(UUID groupId, String content, AlertLevel level) {
        return new AlertBroadcast(
                AlertId.generate(), ORGANISATION_ID, ISSUER_ID,
                AlertContent.of(content), AlertAudience.group(groupId), level, NOW);
    }

    @Nested
    @DisplayName("Dispatching")
    class Dispatching {

        @Test
        @DisplayName("ORGANISATION audience: pushes to the organisation recipients with a self-contained notification")
        void shouldPushToOrganisationRecipients() {
            UUID u1 = UUID.randomUUID();
            UUID u2 = UUID.randomUUID();
            AlertBroadcast event = organisationAlert("Évacuation générale", AlertLevel.URGENT);
            when(recipientPort.organisationRecipients(ORGANISATION_ID)).thenReturn(List.of(u1, u2));
            when(issuerDirectory.displayNameOf(ISSUER_ID)).thenReturn("Marie Dupont");

            service.dispatch(event);

            ArgumentCaptor<AlertNotification> captor = ArgumentCaptor.forClass(AlertNotification.class);
            verify(realtimePort).push(eq(List.of(u1, u2)), captor.capture());
            AlertNotification notification = captor.getValue();
            assertEquals(event.alertId().value(), notification.alertId());
            assertEquals(ORGANISATION_ID.value(), notification.organisationId());
            assertEquals(ISSUER_ID, notification.issuerId());
            assertEquals("Marie Dupont", notification.issuerName());
            assertEquals("Évacuation générale", notification.content());
            assertEquals("URGENT", notification.level());
            assertEquals("ORGANISATION", notification.audienceKind());
            assertNull(notification.groupId());
            assertNull(notification.groupName(), "an organisation-wide alert targets no group");
            assertEquals(NOW, notification.issuedAt());
            // Rien à nommer côté groupe : l'annuaire ne doit même pas être sollicité.
            verify(groupDirectory, never()).nameOf(any());
        }

        @Test
        @DisplayName("GROUP audience: pushes to the group recipients and names the target group")
        void shouldPushToGroupRecipients() {
            UUID groupId = UUID.randomUUID();
            UUID u1 = UUID.randomUUID();
            AlertBroadcast event = groupAlert(groupId, "Cours annulé", AlertLevel.INFO);
            when(recipientPort.groupRecipients(groupId)).thenReturn(List.of(u1));
            when(issuerDirectory.displayNameOf(ISSUER_ID)).thenReturn("Marie Dupont");
            when(groupDirectory.nameOf(groupId)).thenReturn("Promo CDA 2026");

            service.dispatch(event);

            ArgumentCaptor<AlertNotification> captor = ArgumentCaptor.forClass(AlertNotification.class);
            verify(realtimePort).push(eq(List.of(u1)), captor.capture());
            assertEquals("GROUP", captor.getValue().audienceKind());
            assertEquals(groupId, captor.getValue().groupId());
            assertEquals("Promo CDA 2026", captor.getValue().groupName());
            verify(recipientPort, never()).organisationRecipients(any());
        }

        @Test
        @DisplayName("should not push, nor resolve any name, when the audience resolves to no recipient")
        void shouldNotPushWhenNoRecipient() {
            AlertBroadcast event = organisationAlert("Personne connectée", AlertLevel.INFO);
            when(recipientPort.organisationRecipients(ORGANISATION_ID)).thenReturn(List.of());

            service.dispatch(event);

            verify(realtimePort, never()).push(any(), any());
            verify(issuerDirectory, never()).displayNameOf(any());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null recipientPort")
        void shouldRejectNullRecipientPort() {
            assertThrows(NullPointerException.class,
                    () -> new DispatchAlertService(null, realtimePort, issuerDirectory, groupDirectory));
        }

        @Test
        @DisplayName("should reject null realtimePort")
        void shouldRejectNullRealtimePort() {
            assertThrows(NullPointerException.class,
                    () -> new DispatchAlertService(recipientPort, null, issuerDirectory, groupDirectory));
        }

        @Test
        @DisplayName("should reject null issuerDirectory")
        void shouldRejectNullIssuerDirectory() {
            assertThrows(NullPointerException.class,
                    () -> new DispatchAlertService(recipientPort, realtimePort, null, groupDirectory));
        }

        @Test
        @DisplayName("should reject null groupDirectory")
        void shouldRejectNullGroupDirectory() {
            assertThrows(NullPointerException.class,
                    () -> new DispatchAlertService(recipientPort, realtimePort, issuerDirectory, null));
        }
    }
}
