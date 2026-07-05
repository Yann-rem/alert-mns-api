package com.alertmns.alerting.application;

import com.alertmns.alerting.domain.model.Alert;
import com.alertmns.alerting.domain.model.AlertAudienceKind;
import com.alertmns.alerting.domain.model.AlertId;
import com.alertmns.alerting.domain.model.AlertLevel;
import com.alertmns.alerting.domain.port.incoming.command.BroadcastAlertCommand;
import com.alertmns.alerting.domain.port.outgoing.AlertRepository;
import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.model.MemberStatus;
import com.alertmns.shared.EventPublisher;
import com.alertmns.shared.OrganisationId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("BroadcastAlertService")
@ExtendWith(MockitoExtension.class)
class BroadcastAlertServiceTest {

    static final OrganisationId ORGANISATION_ID = OrganisationId.generate();
    static final Instant NOW = Instant.parse("2026-06-15T08:00:00Z");
    static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    @Mock
    CurrentMemberResolver currentMemberResolver;

    @Mock
    AlertRepository alertRepository;

    @Mock
    EventPublisher publisher;

    BroadcastAlertService service;

    MemberId memberId;
    Member member;

    @BeforeEach
    void setUp() {
        service = new BroadcastAlertService(currentMemberResolver, alertRepository, publisher, CLOCK);
        memberId = MemberId.generate();
        member = Member.reconstitute(
                memberId, ORGANISATION_ID, UUID.randomUUID(), MemberRole.MANAGER, MemberStatus.ACTIVE, NOW);
    }

    private void stubCurrentMember() {
        when(currentMemberResolver.resolveCurrentMember()).thenReturn(member);
    }

    private Alert captureSavedAlert() {
        ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepository).save(captor.capture());
        return captor.getValue();
    }

    @Nested
    @DisplayName("Broadcast")
    class Broadcast {

        @Test
        @DisplayName("should broadcast an organisation-wide alert and publish the event")
        void broadcastsOrganisationWide() {
            stubCurrentMember();
            BroadcastAlertCommand command =
                    new BroadcastAlertCommand("Fermeture demain", "URGENT", "ORGANISATION", null);

            AlertId id = service.broadcast(command);

            Alert saved = captureSavedAlert();
            assertEquals(id, saved.id());
            assertEquals(ORGANISATION_ID, saved.organisationId());
            assertEquals(memberId.value(), saved.issuerId());
            assertEquals("Fermeture demain", saved.content().value());
            assertEquals(AlertAudienceKind.ORGANISATION, saved.audience().kind());
            assertNull(saved.audience().groupId());
            assertEquals(AlertLevel.URGENT, saved.level());
            assertEquals(NOW, saved.issuedAt());
            verify(publisher).publish(anyList());
        }

        @Test
        @DisplayName("should broadcast a group-targeted alert")
        void broadcastsGroup() {
            stubCurrentMember();
            UUID groupId = UUID.randomUUID();
            BroadcastAlertCommand command =
                    new BroadcastAlertCommand("Réunion de classe", "INFO", "GROUP", groupId.toString());

            service.broadcast(command);

            Alert saved = captureSavedAlert();
            assertEquals(AlertAudienceKind.GROUP, saved.audience().kind());
            assertEquals(groupId, saved.audience().groupId());
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("should reject an unknown level")
        void rejectsUnknownLevel() {
            stubCurrentMember();
            BroadcastAlertCommand command = new BroadcastAlertCommand("x", "PANIC", "ORGANISATION", null);

            assertThrows(IllegalArgumentException.class, () -> service.broadcast(command));
        }

        @Test
        @DisplayName("should reject an unknown audience kind")
        void rejectsUnknownAudienceKind() {
            stubCurrentMember();
            BroadcastAlertCommand command = new BroadcastAlertCommand("x", "INFO", "EVERYONE", null);

            assertThrows(IllegalArgumentException.class, () -> service.broadcast(command));
        }

        @Test
        @DisplayName("should reject a GROUP audience without a groupId")
        void rejectsGroupWithoutGroupId() {
            stubCurrentMember();
            BroadcastAlertCommand command = new BroadcastAlertCommand("x", "INFO", "GROUP", null);

            assertThrows(IllegalArgumentException.class, () -> service.broadcast(command));
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject a null currentMemberResolver")
        void rejectsNullResolver() {
            assertThrows(NullPointerException.class,
                    () -> new BroadcastAlertService(null, alertRepository, publisher, CLOCK));
        }

        @Test
        @DisplayName("should reject a null alertRepository")
        void rejectsNullRepository() {
            assertThrows(NullPointerException.class,
                    () -> new BroadcastAlertService(currentMemberResolver, null, publisher, CLOCK));
        }

        @Test
        @DisplayName("should reject a null publisher")
        void rejectsNullPublisher() {
            assertThrows(NullPointerException.class,
                    () -> new BroadcastAlertService(currentMemberResolver, alertRepository, null, CLOCK));
        }

        @Test
        @DisplayName("should reject a null clock")
        void rejectsNullClock() {
            assertThrows(NullPointerException.class,
                    () -> new BroadcastAlertService(currentMemberResolver, alertRepository, publisher, null));
        }
    }
}
