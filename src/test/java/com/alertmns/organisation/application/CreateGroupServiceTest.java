package com.alertmns.organisation.application;

import com.alertmns.organisation.domain.event.GroupCreated;
import com.alertmns.organisation.domain.exception.GroupNameAlreadyExistsException;
import com.alertmns.organisation.domain.model.Group;
import com.alertmns.organisation.domain.model.GroupName;
import com.alertmns.organisation.domain.port.incoming.command.CreateGroupCommand;
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

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("CreateGroupService")
@ExtendWith(MockitoExtension.class)
class CreateGroupServiceTest {

    static final OrganisationId ORGANISATION_ID = OrganisationId.generate();
    static final Instant NOW = Instant.parse("2026-05-30T10:00:00Z");

    @Mock
    GroupRepository repository;

    @Mock
    EventPublisher publisher;

    @Mock
    Clock clock;

    @InjectMocks
    CreateGroupService service;

    @BeforeEach
    void stubClock() {
        lenient().when(clock.instant()).thenReturn(NOW);
    }

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create a group and save it with the expected name and organisation")
        void shouldCreateAGroupAndSaveItWithTheExpectedNameAndOrganisation() {
            when(repository.existsByOrganisationIdAndGroupName(any(), any())).thenReturn(false);

            CreateGroupCommand command = new CreateGroupCommand(
                    ORGANISATION_ID.value().toString(), "Développeurs"
            );

            service.create(command);

            ArgumentCaptor<Group> groupCaptor = ArgumentCaptor.forClass(Group.class);
            verify(repository).save(groupCaptor.capture());
            Group saved = groupCaptor.getValue();
            assertEquals(GroupName.of("Développeurs"), saved.name());
            assertEquals(ORGANISATION_ID, saved.organisationId());
        }

        @Test
        @DisplayName("should publish GroupCreated event")
        void shouldPublishGroupCreatedEvent() {
            when(repository.existsByOrganisationIdAndGroupName(any(), any())).thenReturn(false);

            CreateGroupCommand command = new CreateGroupCommand(
                    ORGANISATION_ID.value().toString(), "Développeurs"
            );

            service.create(command);

            ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.captor();
            verify(publisher).publish(eventsCaptor.capture());
            List<DomainEvent> events = eventsCaptor.getValue();
            assertEquals(1, events.size());
            GroupCreated event = assertInstanceOf(GroupCreated.class, events.getFirst());
            assertEquals(GroupName.of("Développeurs"), event.name());
            assertEquals(ORGANISATION_ID, event.organisationId());
            assertEquals(NOW, event.occurredOn());
        }

        @Test
        @DisplayName("should throw GroupNameAlreadyExistsException when name already exists in organisation")
        void shouldThrowGroupNameAlreadyExistsExceptionWhenNameAlreadyExists() {
            when(repository.existsByOrganisationIdAndGroupName(any(), any())).thenReturn(true);

            CreateGroupCommand command = new CreateGroupCommand(
                    ORGANISATION_ID.value().toString(), "Développeurs"
            );

            assertThrows(GroupNameAlreadyExistsException.class, () -> service.create(command));
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when organisationId is not a valid UUID")
        void shouldThrowWhenOrganisationIdIsInvalid() {
            CreateGroupCommand command = new CreateGroupCommand("invalid", "Développeurs");

            assertThrows(IllegalArgumentException.class, () -> service.create(command));
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
                    () -> new CreateGroupService(null, publisher, clock));
        }

        @Test
        @DisplayName("should reject null publisher")
        void shouldRejectNullPublisher() {
            assertThrows(NullPointerException.class,
                    () -> new CreateGroupService(repository, null, clock));
        }

        @Test
        @DisplayName("should reject null clock")
        void shouldRejectNullClock() {
            assertThrows(NullPointerException.class,
                    () -> new CreateGroupService(repository, publisher, null));
        }

        @Test
        @DisplayName("should reject null command organisationId")
        void shouldRejectNullCommandOrganisationId() {
            assertThrows(NullPointerException.class,
                    () -> new CreateGroupCommand(null, "Développeurs"));
        }

        @Test
        @DisplayName("should reject null command name")
        void shouldRejectNullCommandName() {
            assertThrows(NullPointerException.class,
                    () -> new CreateGroupCommand(ORGANISATION_ID.value().toString(), null));
        }
    }
}
