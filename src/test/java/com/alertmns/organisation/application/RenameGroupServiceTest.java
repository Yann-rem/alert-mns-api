package com.alertmns.organisation.application;

import com.alertmns.organisation.domain.event.GroupRenamed;
import com.alertmns.organisation.domain.exception.GroupNameAlreadyExistsException;
import com.alertmns.organisation.domain.exception.GroupNotFoundException;
import com.alertmns.organisation.domain.model.Group;
import com.alertmns.organisation.domain.model.GroupId;
import com.alertmns.organisation.domain.model.GroupName;
import com.alertmns.organisation.domain.port.incoming.command.RenameGroupCommand;
import com.alertmns.organisation.domain.port.outgoing.GroupRepository;
import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.EventPublisher;
import com.alertmns.shared.OrganisationId;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("RenameGroupService")
@ExtendWith(MockitoExtension.class)
class RenameGroupServiceTest {

    static final OrganisationId ORGANISATION_ID = OrganisationId.generate();

    @Mock
    GroupRepository repository;

    @Mock
    EventPublisher publisher;

    @InjectMocks
    RenameGroupService service;

    @Nested
    @DisplayName("Renaming")
    class Renaming {

        GroupId id;
        Group group;

        @BeforeEach
        void setUp() {
            id = GroupId.generate();

            group = Group.reconstitute(
                    id,
                    ORGANISATION_ID,
                    GroupName.of("Développeurs"),
                    Instant.now()
            );
        }

        @Test
        @DisplayName("should rename a group and save it with the new name")
        void shouldRenameAGroupAndSaveItWithTheNewName() {
            when(repository.findById(any())).thenReturn(Optional.of(group));
            when(repository.existsByOrganisationIdAndGroupName(any(), any())).thenReturn(false);

            RenameGroupCommand command = new RenameGroupCommand(id.value().toString(), "Designers");

            service.rename(command);

            ArgumentCaptor<Group> groupCaptor = ArgumentCaptor.forClass(Group.class);
            verify(repository).save(groupCaptor.capture());
            assertEquals(GroupName.of("Designers"), groupCaptor.getValue().name());
        }

        @Test
        @DisplayName("should publish GroupRenamed event")
        void shouldPublishGroupRenamedEvent() {
            when(repository.findById(any())).thenReturn(Optional.of(group));
            when(repository.existsByOrganisationIdAndGroupName(any(), any())).thenReturn(false);

            RenameGroupCommand command = new RenameGroupCommand(id.value().toString(), "Designers");

            service.rename(command);

            ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.captor();
            verify(publisher).publish(eventsCaptor.capture());
            List<DomainEvent> events = eventsCaptor.getValue();
            assertEquals(1, events.size());
            GroupRenamed event = assertInstanceOf(GroupRenamed.class, events.getFirst());
            assertEquals(id, event.groupId());
            assertEquals(GroupName.of("Designers"), event.name());
        }

        @Test
        @DisplayName("should do nothing when new name equals current name")
        void shouldDoNothingWhenNewNameEqualsCurrentName() {
            when(repository.findById(any())).thenReturn(Optional.of(group));

            RenameGroupCommand command = new RenameGroupCommand(id.value().toString(), "Développeurs");

            service.rename(command);

            verify(repository, never()).existsByOrganisationIdAndGroupName(any(), any());
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should throw GroupNameAlreadyExistsException when new name already exists in organisation")
        void shouldThrowGroupNameAlreadyExistsExceptionWhenNewNameAlreadyExists() {
            when(repository.findById(any())).thenReturn(Optional.of(group));
            when(repository.existsByOrganisationIdAndGroupName(any(), any())).thenReturn(true);

            RenameGroupCommand command = new RenameGroupCommand(id.value().toString(), "Designers");

            assertThrows(GroupNameAlreadyExistsException.class, () -> service.rename(command));
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should throw GroupNotFoundException when group not found")
        void shouldThrowGroupNotFoundExceptionWhenGroupNotFound() {
            when(repository.findById(any())).thenReturn(Optional.empty());

            RenameGroupCommand command = new RenameGroupCommand(id.value().toString(), "Designers");

            assertThrows(GroupNotFoundException.class, () -> service.rename(command));
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when groupId is not a valid UUID")
        void shouldThrowWhenGroupIdIsInvalid() {
            RenameGroupCommand command = new RenameGroupCommand("invalid", "Designers");

            assertThrows(IllegalArgumentException.class, () -> service.rename(command));
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null repository")
        void shouldRejectNullRepository() {
            assertThrows(NullPointerException.class,
                    () -> new RenameGroupService(null, publisher));
        }

        @Test
        @DisplayName("should reject null publisher")
        void shouldRejectNullPublisher() {
            assertThrows(NullPointerException.class,
                    () -> new RenameGroupService(repository, null));
        }

        @Test
        @DisplayName("should reject null command groupId")
        void shouldRejectNullCommandGroupId() {
            assertThrows(NullPointerException.class,
                    () -> new RenameGroupCommand(null, "Designers"));
        }

        @Test
        @DisplayName("should reject null command name")
        void shouldRejectNullCommandName() {
            assertThrows(NullPointerException.class,
                    () -> new RenameGroupCommand(GroupId.generate().value().toString(), null));
        }
    }
}
