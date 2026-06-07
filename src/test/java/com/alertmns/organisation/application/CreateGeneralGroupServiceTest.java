package com.alertmns.organisation.application;

import com.alertmns.organisation.domain.event.GroupCreated;
import com.alertmns.organisation.domain.model.Group;
import com.alertmns.organisation.domain.model.GroupKind;
import com.alertmns.organisation.domain.model.GroupName;
import com.alertmns.organisation.domain.port.incoming.command.CreateGeneralGroupCommand;
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

@DisplayName("CreateGeneralGroupService")
@ExtendWith(MockitoExtension.class)
class CreateGeneralGroupServiceTest {

    static final OrganisationId ORGANISATION_ID = OrganisationId.generate();
    static final String GENERAL_NAME = "Général";
    static final Instant NOW = Instant.parse("2026-05-30T10:00:00Z");

    @Mock
    GroupRepository repository;

    @Mock
    EventPublisher publisher;

    @Mock
    Clock clock;

    @InjectMocks
    CreateGeneralGroupService service;

    @BeforeEach
    void stubClock() {
        lenient().when(clock.instant()).thenReturn(NOW);
    }

    private CreateGeneralGroupCommand command() {
        return new CreateGeneralGroupCommand(ORGANISATION_ID.value().toString(), GENERAL_NAME);
    }

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create and save a GENERAL group when none exists")
        void shouldCreateGeneralGroupWhenAbsent() {
            when(repository.existsGeneralByOrganisationId(ORGANISATION_ID)).thenReturn(false);

            service.create(command());

            ArgumentCaptor<Group> groupCaptor = ArgumentCaptor.forClass(Group.class);
            verify(repository).save(groupCaptor.capture());
            Group saved = groupCaptor.getValue();
            assertEquals(GroupName.of(GENERAL_NAME), saved.name());
            assertEquals(ORGANISATION_ID, saved.organisationId());
            assertEquals(GroupKind.GENERAL, saved.kind());
        }

        @Test
        @DisplayName("should publish GroupCreated with kind GENERAL and occurredOn = now")
        void shouldPublishGroupCreated() {
            when(repository.existsGeneralByOrganisationId(ORGANISATION_ID)).thenReturn(false);

            service.create(command());

            ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.captor();
            verify(publisher).publish(eventsCaptor.capture());
            List<DomainEvent> events = eventsCaptor.getValue();
            assertEquals(1, events.size());
            GroupCreated event = assertInstanceOf(GroupCreated.class, events.getFirst());
            assertEquals(GroupName.of(GENERAL_NAME), event.name());
            assertEquals(ORGANISATION_ID, event.organisationId());
            assertEquals(GroupKind.GENERAL, event.kind());
            assertEquals(NOW, event.occurredOn());
        }
    }

    @Nested
    @DisplayName("Idempotence")
    class Idempotence {

        @Test
        @DisplayName("should be a no-op when a GENERAL group already exists")
        void shouldBeNoOpWhenGeneralGroupExists() {
            when(repository.existsGeneralByOrganisationId(ORGANISATION_ID)).thenReturn(true);

            service.create(command());

            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should throw IllegalArgumentException when organisationId is not a valid UUID")
        void shouldThrowWhenOrganisationIdInvalid() {
            CreateGeneralGroupCommand command = new CreateGeneralGroupCommand("invalid", GENERAL_NAME);

            assertThrows(IllegalArgumentException.class, () -> service.create(command));
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("should reject null repository")
        void shouldRejectNullRepository() {
            assertThrows(NullPointerException.class,
                    () -> new CreateGeneralGroupService(null, publisher, clock));
        }

        @Test
        @DisplayName("should reject null publisher")
        void shouldRejectNullPublisher() {
            assertThrows(NullPointerException.class,
                    () -> new CreateGeneralGroupService(repository, null, clock));
        }

        @Test
        @DisplayName("should reject null clock")
        void shouldRejectNullClock() {
            assertThrows(NullPointerException.class,
                    () -> new CreateGeneralGroupService(repository, publisher, null));
        }

        @Test
        @DisplayName("should reject null command organisationId")
        void shouldRejectNullCommandOrganisationId() {
            assertThrows(NullPointerException.class,
                    () -> new CreateGeneralGroupCommand(null, GENERAL_NAME));
        }

        @Test
        @DisplayName("should reject null command name")
        void shouldRejectNullCommandName() {
            assertThrows(NullPointerException.class,
                    () -> new CreateGeneralGroupCommand(ORGANISATION_ID.value().toString(), null));
        }
    }
}
