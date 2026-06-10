package com.alertmns.organisation.infrastructure.adapter.incoming.event;

import com.alertmns.organisation.domain.event.MemberJoined;
import com.alertmns.organisation.domain.model.Group;
import com.alertmns.organisation.domain.model.GroupId;
import com.alertmns.organisation.domain.model.GroupKind;
import com.alertmns.organisation.domain.model.GroupName;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.port.incoming.AddMemberToGroupUseCase;
import com.alertmns.organisation.domain.port.incoming.command.AddMemberToGroupCommand;
import com.alertmns.organisation.domain.port.outgoing.GroupRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("AddMemberToGeneralGroupOnMemberJoinedListener")
@ExtendWith(MockitoExtension.class)
class AddMemberToGeneralGroupOnMemberJoinedListenerTest {

    static final OrganisationId ORGANISATION_ID = OrganisationId.generate();
    static final GroupId GENERAL_GROUP_ID = GroupId.generate();
    static final MemberId MEMBER_ID = MemberId.generate();
    static final Instant NOW = Instant.parse("2026-05-30T10:00:00Z");

    @Mock
    GroupRepository groupRepository;

    @Mock
    AddMemberToGroupUseCase addMemberToGroupUseCase;

    @InjectMocks
    AddMemberToGeneralGroupOnMemberJoinedListener listener;

    private MemberJoined memberJoined() {
        return new MemberJoined(ORGANISATION_ID, MEMBER_ID, UUID.randomUUID(), MemberRole.MEMBER, NOW);
    }

    private Group generalGroup() {
        return Group.reconstitute(
                GENERAL_GROUP_ID,
                ORGANISATION_ID,
                GroupName.of("Général"),
                GroupKind.GENERAL,
                NOW
        );
    }

    @Nested
    @DisplayName("Cascade")
    class Cascade {

        @Test
        @DisplayName("should add the new member to the GENERAL group of its organisation")
        void shouldAddMemberToGeneralGroup() {
            when(groupRepository.findGeneralByOrganisationId(ORGANISATION_ID))
                    .thenReturn(Optional.of(generalGroup()));

            listener.onMemberJoinedEvent(memberJoined());

            ArgumentCaptor<AddMemberToGroupCommand> commandCaptor =
                    ArgumentCaptor.forClass(AddMemberToGroupCommand.class);
            verify(addMemberToGroupUseCase).add(commandCaptor.capture());
            AddMemberToGroupCommand command = commandCaptor.getValue();
            assertThat(command.organisationId()).isEqualTo(ORGANISATION_ID.value().toString());
            assertThat(command.groupId()).isEqualTo(GENERAL_GROUP_ID.value().toString());
            assertThat(command.memberId()).isEqualTo(MEMBER_ID.value().toString());
        }

        @Test
        @DisplayName("should resolve the GENERAL group by the organisationId carried in the event")
        void shouldResolveGeneralGroupByEventOrganisationId() {
            when(groupRepository.findGeneralByOrganisationId(ORGANISATION_ID))
                    .thenReturn(Optional.of(generalGroup()));

            listener.onMemberJoinedEvent(memberJoined());

            verify(groupRepository).findGeneralByOrganisationId(ORGANISATION_ID);
        }

        @Test
        @DisplayName("should throw IllegalStateException when the organisation has no GENERAL group")
        void shouldThrowWhenNoGeneralGroup() {
            when(groupRepository.findGeneralByOrganisationId(ORGANISATION_ID))
                    .thenReturn(Optional.empty());

            assertThrows(IllegalStateException.class,
                    () -> listener.onMemberJoinedEvent(memberJoined()));

            verify(addMemberToGroupUseCase, never()).add(any());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null GroupRepository")
        void shouldRejectNullGroupRepository() {
            assertThrows(NullPointerException.class,
                    () -> new AddMemberToGeneralGroupOnMemberJoinedListener(null, addMemberToGroupUseCase));
        }

        @Test
        @DisplayName("should reject null AddMemberToGroupUseCase")
        void shouldRejectNullAddMemberToGroupUseCase() {
            assertThrows(NullPointerException.class,
                    () -> new AddMemberToGeneralGroupOnMemberJoinedListener(groupRepository, null));
        }
    }
}
