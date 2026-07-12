package com.alertmns.alerting.application;

import com.alertmns.alerting.domain.model.Alert;
import com.alertmns.alerting.domain.model.AlertAudience;
import com.alertmns.alerting.domain.model.AlertContent;
import com.alertmns.alerting.domain.model.AlertId;
import com.alertmns.alerting.domain.model.AlertLevel;
import com.alertmns.alerting.domain.port.outgoing.AlertRepository;
import com.alertmns.alerting.domain.port.outgoing.GroupMembershipPort;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("ListMyAlertsService")
@ExtendWith(MockitoExtension.class)
class ListMyAlertsServiceTest {

    static final OrganisationId ORGANISATION_ID = OrganisationId.generate();
    static final Instant NOW = Instant.parse("2026-06-25T10:00:00Z");

    @Mock
    CurrentMemberResolver currentMemberResolver;

    @Mock
    AlertRepository alertRepository;

    @Mock
    GroupMembershipPort groupMembershipPort;

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

    private Alert organisationAlert(Instant issuedAt) {
        return Alert.reconstitute(
                AlertId.generate(), ORGANISATION_ID, UUID.randomUUID(),
                AlertContent.of("Org"), AlertAudience.organisation(), AlertLevel.INFO, issuedAt);
    }

    private Alert groupAlert(UUID groupId, Instant issuedAt) {
        return Alert.reconstitute(
                AlertId.generate(), ORGANISATION_ID, UUID.randomUUID(),
                AlertContent.of("Group"), AlertAudience.group(groupId), AlertLevel.URGENT, issuedAt);
    }

    @Nested
    @DisplayName("Listing")
    class Listing {

        @Test
        @DisplayName("should return organisation-wide and group alerts sorted from most recent to oldest")
        void sortsMostRecentFirst() {
            stubCurrentMember();
            Alert oldestOrg = organisationAlert(NOW);
            Alert middleOrg = organisationAlert(NOW.plusSeconds(60));
            UUID groupId = UUID.randomUUID();
            Alert newestGroup = groupAlert(groupId, NOW.plusSeconds(120));
            when(alertRepository.findOrganisationWide(ORGANISATION_ID)).thenReturn(List.of(oldestOrg, middleOrg));
            when(groupMembershipPort.groupIdsOf(memberId.value())).thenReturn(List.of(groupId));
            when(alertRepository.findByGroupIdIn(List.of(groupId))).thenReturn(List.of(newestGroup));

            List<Alert> result = service.list();

            assertEquals(List.of(newestGroup, middleOrg, oldestOrg), result);
        }

        @Test
        @DisplayName("should not query group alerts when the member has no group")
        void skipsGroupQueryWhenNoGroup() {
            stubCurrentMember();
            Alert org = organisationAlert(NOW);
            when(alertRepository.findOrganisationWide(ORGANISATION_ID)).thenReturn(List.of(org));
            when(groupMembershipPort.groupIdsOf(memberId.value())).thenReturn(List.of());

            List<Alert> result = service.list();

            assertEquals(List.of(org), result);
            verify(alertRepository, never()).findByGroupIdIn(any());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject a null currentMemberResolver")
        void rejectsNullResolver() {
            assertThrows(NullPointerException.class,
                    () -> new ListMyAlertsService(null, alertRepository, groupMembershipPort));
        }

        @Test
        @DisplayName("should reject a null alertRepository")
        void rejectsNullRepository() {
            assertThrows(NullPointerException.class,
                    () -> new ListMyAlertsService(currentMemberResolver, null, groupMembershipPort));
        }

        @Test
        @DisplayName("should reject a null groupMembershipPort")
        void rejectsNullPort() {
            assertThrows(NullPointerException.class,
                    () -> new ListMyAlertsService(currentMemberResolver, alertRepository, null));
        }
    }
}
