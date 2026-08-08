package com.alertmns.alerting.application;

import com.alertmns.alerting.domain.model.Alert;
import com.alertmns.alerting.domain.model.AlertAudience;
import com.alertmns.alerting.domain.model.AlertContent;
import com.alertmns.alerting.domain.model.AlertId;
import com.alertmns.alerting.domain.model.AlertLevel;
import com.alertmns.alerting.domain.port.incoming.AlertView;
import com.alertmns.alerting.domain.port.outgoing.AlertRepository;
import com.alertmns.alerting.domain.port.outgoing.GroupDirectoryPort;
import com.alertmns.alerting.domain.port.outgoing.GroupMembershipPort;
import com.alertmns.alerting.domain.port.outgoing.IssuerDirectoryPort;
import com.alertmns.organisation.application.CurrentMemberResolver;
import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.model.MemberStatus;
import com.alertmns.shared.OrganisationId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("ListMyAlertsService")
@ExtendWith(MockitoExtension.class)
class ListMyAlertsServiceTest {

    static final OrganisationId ORGANISATION_ID = OrganisationId.generate();
    static final UUID ISSUER_ID = UUID.randomUUID();
    static final Instant NOW = Instant.parse("2026-06-25T10:00:00Z");

    @Mock
    CurrentMemberResolver currentMemberResolver;

    @Mock
    AlertRepository alertRepository;

    @Mock
    GroupMembershipPort groupMembershipPort;

    @Mock
    IssuerDirectoryPort issuerDirectory;

    @Mock
    GroupDirectoryPort groupDirectory;

    @InjectMocks
    ListMyAlertsService service;

    MemberId memberId;
    Member member;

    @BeforeEach
    void setUp() {
        memberId = MemberId.generate();
        member = Member.reconstitute(
                memberId, ORGANISATION_ID, UUID.randomUUID(), MemberRole.MEMBER, MemberStatus.ACTIVE, NOW);
    }

    private void stubCurrentMember() {
        when(currentMemberResolver.resolveCurrentMember()).thenReturn(member);
    }

    private void stubIssuerName() {
        when(issuerDirectory.displayNameOf(ISSUER_ID)).thenReturn("Marie Dupont");
    }

    private Alert organisationAlert(Instant issuedAt) {
        return Alert.reconstitute(
                AlertId.generate(), ORGANISATION_ID, ISSUER_ID,
                AlertContent.of("Org"), AlertAudience.organisation(), AlertLevel.INFO, issuedAt);
    }

    private Alert groupAlert(UUID groupId, Instant issuedAt) {
        return Alert.reconstitute(
                AlertId.generate(), ORGANISATION_ID, ISSUER_ID,
                AlertContent.of("Group"), AlertAudience.group(groupId), AlertLevel.URGENT, issuedAt);
    }

    private static List<Alert> alertsOf(List<AlertView> views) {
        return views.stream().map(AlertView::alert).toList();
    }

    @Nested
    @DisplayName("Listing")
    class Listing {

        @Test
        @DisplayName("should return organisation-wide and group alerts sorted from most recent to oldest")
        void sortsMostRecentFirst() {
            stubCurrentMember();
            stubIssuerName();
            Alert oldestOrg = organisationAlert(NOW);
            Alert middleOrg = organisationAlert(NOW.plusSeconds(60));
            UUID groupId = UUID.randomUUID();
            Alert newestGroup = groupAlert(groupId, NOW.plusSeconds(120));
            when(alertRepository.findOrganisationWide(ORGANISATION_ID)).thenReturn(List.of(oldestOrg, middleOrg));
            when(groupMembershipPort.groupIdsOf(memberId.value())).thenReturn(List.of(groupId));
            when(alertRepository.findByGroupIdIn(List.of(groupId))).thenReturn(List.of(newestGroup));
            when(groupDirectory.nameOf(groupId)).thenReturn("Promo CDA 2026");

            List<AlertView> result = service.list();

            assertEquals(List.of(newestGroup, middleOrg, oldestOrg), alertsOf(result));
        }

        @Test
        @DisplayName("should not query group alerts when the member has no group")
        void skipsGroupQueryWhenNoGroup() {
            stubCurrentMember();
            stubIssuerName();
            Alert org = organisationAlert(NOW);
            when(alertRepository.findOrganisationWide(ORGANISATION_ID)).thenReturn(List.of(org));
            when(groupMembershipPort.groupIdsOf(memberId.value())).thenReturn(List.of());

            List<AlertView> result = service.list();

            assertEquals(List.of(org), alertsOf(result));
            verify(alertRepository, never()).findByGroupIdIn(any());
        }
    }

    @Nested
    @DisplayName("Naming")
    class Naming {

        @Test
        @DisplayName("should name the issuer, and the target group only for a GROUP audience")
        void namesIssuerAndGroup() {
            stubCurrentMember();
            stubIssuerName();
            UUID groupId = UUID.randomUUID();
            when(alertRepository.findOrganisationWide(ORGANISATION_ID))
                    .thenReturn(List.of(organisationAlert(NOW)));
            when(groupMembershipPort.groupIdsOf(memberId.value())).thenReturn(List.of(groupId));
            when(alertRepository.findByGroupIdIn(List.of(groupId)))
                    .thenReturn(List.of(groupAlert(groupId, NOW.plusSeconds(60))));
            when(groupDirectory.nameOf(groupId)).thenReturn("Promo CDA 2026");

            List<AlertView> result = service.list();

            AlertView groupView = result.get(0);
            assertEquals("Marie Dupont", groupView.issuerName());
            assertEquals("Promo CDA 2026", groupView.groupName());

            AlertView organisationView = result.get(1);
            assertEquals("Marie Dupont", organisationView.issuerName());
            assertNull(organisationView.groupName(), "an organisation-wide alert targets no group");
        }

        @Test
        @DisplayName("should resolve a repeated issuer only once")
        void resolvesEachIssuerOnce() {
            stubCurrentMember();
            stubIssuerName();
            when(alertRepository.findOrganisationWide(ORGANISATION_ID))
                    .thenReturn(List.of(organisationAlert(NOW), organisationAlert(NOW.plusSeconds(60))));
            when(groupMembershipPort.groupIdsOf(memberId.value())).thenReturn(List.of());

            service.list();

            verify(issuerDirectory, times(1)).displayNameOf(ISSUER_ID);
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject a null currentMemberResolver")
        void rejectsNullResolver() {
            assertThrows(NullPointerException.class, () -> new ListMyAlertsService(
                    null, alertRepository, groupMembershipPort, issuerDirectory, groupDirectory));
        }

        @Test
        @DisplayName("should reject a null alertRepository")
        void rejectsNullRepository() {
            assertThrows(NullPointerException.class, () -> new ListMyAlertsService(
                    currentMemberResolver, null, groupMembershipPort, issuerDirectory, groupDirectory));
        }

        @Test
        @DisplayName("should reject a null groupMembershipPort")
        void rejectsNullPort() {
            assertThrows(NullPointerException.class, () -> new ListMyAlertsService(
                    currentMemberResolver, alertRepository, null, issuerDirectory, groupDirectory));
        }

        @Test
        @DisplayName("should reject a null issuerDirectory")
        void rejectsNullIssuerDirectory() {
            assertThrows(NullPointerException.class, () -> new ListMyAlertsService(
                    currentMemberResolver, alertRepository, groupMembershipPort, null, groupDirectory));
        }

        @Test
        @DisplayName("should reject a null groupDirectory")
        void rejectsNullGroupDirectory() {
            assertThrows(NullPointerException.class, () -> new ListMyAlertsService(
                    currentMemberResolver, alertRepository, groupMembershipPort, issuerDirectory, null));
        }
    }
}
