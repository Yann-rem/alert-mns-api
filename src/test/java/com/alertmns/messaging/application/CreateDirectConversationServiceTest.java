package com.alertmns.messaging.application;

import com.alertmns.messaging.domain.event.DirectConversationCreated;
import com.alertmns.messaging.domain.model.Conversation;
import com.alertmns.messaging.domain.model.ConversationId;
import com.alertmns.messaging.domain.model.ConversationKind;
import com.alertmns.messaging.domain.model.ParticipantPair;
import com.alertmns.messaging.domain.port.incoming.command.CreateDirectConversationCommand;
import com.alertmns.messaging.domain.port.outgoing.ConversationRepository;
import com.alertmns.organisation.domain.exception.MemberNotFoundException;
import com.alertmns.organisation.domain.exception.OrganisationMismatchException;
import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.model.MemberStatus;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("CreateDirectConversationService")
@ExtendWith(MockitoExtension.class)
class CreateDirectConversationServiceTest {

    static final OrganisationId ORGANISATION_ID = OrganisationId.generate();
    static final OrganisationId OTHER_ORGANISATION_ID = OrganisationId.generate();
    static final Instant NOW = Instant.parse("2026-05-30T10:00:00Z");

    @Mock
    CurrentMemberResolver currentMemberResolver;

    @Mock
    MemberRepository memberRepository;

    @Mock
    ConversationRepository conversationRepository;

    @Mock
    EventPublisher publisher;

    @Mock
    Clock clock;

    @InjectMocks
    CreateDirectConversationService service;

    MemberId initiatorId;
    MemberId targetId;
    Member initiator;
    Member target;

    @BeforeEach
    void setUp() {
        lenient().when(clock.instant()).thenReturn(NOW);
        initiatorId = MemberId.generate();
        targetId = MemberId.generate();
        initiator = Member.reconstitute(
                initiatorId, ORGANISATION_ID, UUID.randomUUID(), MemberRole.MEMBER, MemberStatus.ACTIVE, NOW);
        target = Member.reconstitute(
                targetId, ORGANISATION_ID, UUID.randomUUID(), MemberRole.MEMBER, MemberStatus.ACTIVE, NOW);
    }

    private CreateDirectConversationCommand command() {
        return new CreateDirectConversationCommand(targetId.value().toString());
    }

    private void stubAuthenticatedInitiator() {
        when(currentMemberResolver.resolveCurrentMember()).thenReturn(initiator);
    }

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create and save a DIRECT conversation between the two members when none exists")
        void shouldCreateWhenAbsent() {
            stubAuthenticatedInitiator();
            when(memberRepository.findById(targetId)).thenReturn(Optional.of(target));
            when(conversationRepository.findByParticipants(any())).thenReturn(Optional.empty());

            ConversationId result = service.create(command());

            ArgumentCaptor<Conversation> conversationCaptor = ArgumentCaptor.forClass(Conversation.class);
            verify(conversationRepository).save(conversationCaptor.capture());
            Conversation saved = conversationCaptor.getValue();
            assertEquals(ConversationKind.DIRECT, saved.kind());
            assertEquals(ORGANISATION_ID, saved.organisationId());
            assertEquals(ParticipantPair.of(initiatorId.value(), targetId.value()), saved.participantPair());
            assertEquals(saved.id(), result);
        }

        @Test
        @DisplayName("should publish DirectConversationCreated with the participant pair")
        void shouldPublishDirectConversationCreated() {
            stubAuthenticatedInitiator();
            when(memberRepository.findById(targetId)).thenReturn(Optional.of(target));
            when(conversationRepository.findByParticipants(any())).thenReturn(Optional.empty());

            service.create(command());

            ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.captor();
            verify(publisher).publish(eventsCaptor.capture());
            DirectConversationCreated event =
                    assertInstanceOf(DirectConversationCreated.class, eventsCaptor.getValue().getFirst());
            assertEquals(ORGANISATION_ID, event.organisationId());
            assertEquals(ParticipantPair.of(initiatorId.value(), targetId.value()), event.participantPair());
            assertEquals(NOW, event.occurredOn());
        }
    }

    @Nested
    @DisplayName("Idempotence")
    class Idempotence {

        @Test
        @DisplayName("should return the existing conversation id and not create a duplicate")
        void shouldReturnExistingWhenPairAlreadyExists() {
            stubAuthenticatedInitiator();
            when(memberRepository.findById(targetId)).thenReturn(Optional.of(target));
            ParticipantPair pair = ParticipantPair.of(initiatorId.value(), targetId.value());
            Conversation existing = Conversation.reconstitute(
                    ConversationId.generate(), ORGANISATION_ID, null, null, ConversationKind.DIRECT, pair, NOW);
            when(conversationRepository.findByParticipants(any())).thenReturn(Optional.of(existing));

            ConversationId result = service.create(command());

            assertEquals(existing.id(), result);
            verify(conversationRepository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("should throw MemberNotFoundException when the target does not exist")
        void shouldThrowWhenTargetMissing() {
            stubAuthenticatedInitiator();
            when(memberRepository.findById(targetId)).thenReturn(Optional.empty());

            assertThrows(MemberNotFoundException.class, () -> service.create(command()));
            verify(conversationRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw OrganisationMismatchException when the target is in another organisation")
        void shouldThrowWhenTargetInAnotherOrganisation() {
            Member otherOrgTarget = Member.reconstitute(
                    targetId, OTHER_ORGANISATION_ID, UUID.randomUUID(), MemberRole.MEMBER, MemberStatus.ACTIVE, NOW);
            stubAuthenticatedInitiator();
            when(memberRepository.findById(targetId)).thenReturn(Optional.of(otherOrgTarget));

            assertThrows(OrganisationMismatchException.class, () -> service.create(command()));
            verify(conversationRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null currentMemberResolver")
        void shouldRejectNullCurrentMemberResolver() {
            assertThrows(NullPointerException.class,
                    () -> new CreateDirectConversationService(null, memberRepository, conversationRepository, publisher, clock));
        }

        @Test
        @DisplayName("should reject null memberRepository")
        void shouldRejectNullMemberRepository() {
            assertThrows(NullPointerException.class,
                    () -> new CreateDirectConversationService(currentMemberResolver, null, conversationRepository, publisher, clock));
        }

        @Test
        @DisplayName("should reject null conversationRepository")
        void shouldRejectNullConversationRepository() {
            assertThrows(NullPointerException.class,
                    () -> new CreateDirectConversationService(currentMemberResolver, memberRepository, null, publisher, clock));
        }

        @Test
        @DisplayName("should reject null publisher")
        void shouldRejectNullPublisher() {
            assertThrows(NullPointerException.class,
                    () -> new CreateDirectConversationService(currentMemberResolver, memberRepository, conversationRepository, null, clock));
        }

        @Test
        @DisplayName("should reject null clock")
        void shouldRejectNullClock() {
            assertThrows(NullPointerException.class,
                    () -> new CreateDirectConversationService(currentMemberResolver, memberRepository, conversationRepository, publisher, null));
        }

        @Test
        @DisplayName("should reject null command targetMemberId")
        void shouldRejectNullCommandTargetMemberId() {
            assertThrows(NullPointerException.class, () -> new CreateDirectConversationCommand(null));
        }
    }
}
