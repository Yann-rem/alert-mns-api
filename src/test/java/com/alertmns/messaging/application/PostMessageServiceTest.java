package com.alertmns.messaging.application;

import com.alertmns.messaging.domain.event.MessagePosted;
import com.alertmns.messaging.domain.exception.ConversationNotFoundException;
import com.alertmns.messaging.domain.exception.InvalidReplyTargetException;
import com.alertmns.messaging.domain.exception.NotAConversationParticipantException;
import com.alertmns.messaging.domain.model.Conversation;
import com.alertmns.messaging.domain.model.ConversationId;
import com.alertmns.messaging.domain.model.ConversationKind;
import com.alertmns.messaging.domain.model.ConversationName;
import com.alertmns.messaging.domain.model.Message;
import com.alertmns.messaging.domain.model.MessageContent;
import com.alertmns.messaging.domain.model.MessageId;
import com.alertmns.messaging.domain.model.ParticipantPair;
import com.alertmns.messaging.domain.port.incoming.command.PostMessageCommand;
import com.alertmns.messaging.domain.port.outgoing.ConversationRepository;
import com.alertmns.messaging.domain.port.outgoing.GroupMembershipChecker;
import com.alertmns.messaging.domain.port.outgoing.MessageRepository;
import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.model.MemberStatus;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("PostMessageService")
@ExtendWith(MockitoExtension.class)
class PostMessageServiceTest {

    static final OrganisationId ORGANISATION_ID = OrganisationId.generate();
    static final Instant NOW = Instant.parse("2026-05-30T10:00:00Z");
    static final String CONTENT = "Bonjour";

    @Mock
    CurrentMemberResolver currentMemberResolver;

    @Mock
    ConversationRepository conversationRepository;

    @Mock
    GroupMembershipChecker groupMembershipChecker;

    @Mock
    MessageRepository messageRepository;

    @Mock
    EventPublisher publisher;

    @Mock
    Clock clock;

    @InjectMocks
    PostMessageService service;

    MemberId authorMemberId;
    Member author;
    ConversationId conversationId;

    @BeforeEach
    void setUp() {
        lenient().when(clock.instant()).thenReturn(NOW);
        authorMemberId = MemberId.generate();
        author = Member.reconstitute(
                authorMemberId, ORGANISATION_ID, UUID.randomUUID(), MemberRole.MEMBER, MemberStatus.ACTIVE, NOW);
        conversationId = ConversationId.generate();
    }

    private void stubAuthenticatedAuthor() {
        when(currentMemberResolver.resolveCurrentMember()).thenReturn(author);
    }

    private PostMessageCommand command() {
        return new PostMessageCommand(conversationId.value().toString(), CONTENT, null);
    }

    private PostMessageCommand replyCommand(MessageId replyTo) {
        return new PostMessageCommand(conversationId.value().toString(), CONTENT, replyTo.value().toString());
    }

    private Conversation directConversationWithAuthor() {
        ParticipantPair pair = ParticipantPair.of(authorMemberId.value(), UUID.randomUUID());
        return Conversation.reconstitute(
                conversationId, ORGANISATION_ID, null, null, ConversationKind.DIRECT, pair, NOW);
    }

    private Conversation groupConversation(UUID groupId) {
        return Conversation.reconstitute(
                conversationId, ORGANISATION_ID, groupId, ConversationName.of("Général"), ConversationKind.GROUP,
                null, NOW);
    }

    @Nested
    @DisplayName("Posting")
    class Posting {

        @Test
        @DisplayName("should post and save a root message in a DIRECT conversation the author belongs to")
        void shouldPostRootMessage() {
            stubAuthenticatedAuthor();
            when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(directConversationWithAuthor()));

            MessageId result = service.post(command());

            ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
            verify(messageRepository).save(messageCaptor.capture());
            Message saved = messageCaptor.getValue();
            assertEquals(conversationId, saved.conversationId());
            assertEquals(authorMemberId.value(), saved.authorId());
            assertEquals(MessageContent.of(CONTENT), saved.content());
            assertNull(saved.replyTo());
            assertEquals(NOW, saved.sentAt());
            assertEquals(saved.id(), result);
        }

        @Test
        @DisplayName("should publish MessagePosted with the author and occurredOn = now")
        void shouldPublishMessagePosted() {
            stubAuthenticatedAuthor();
            when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(directConversationWithAuthor()));

            service.post(command());

            ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.captor();
            verify(publisher).publish(eventsCaptor.capture());
            MessagePosted event = assertInstanceOf(MessagePosted.class, eventsCaptor.getValue().getFirst());
            assertEquals(conversationId, event.conversationId());
            assertEquals(authorMemberId.value(), event.authorId());
            assertEquals(NOW, event.occurredOn());
        }

        @Test
        @DisplayName("should post in a GROUP conversation when the author is a member of the group")
        void shouldPostInGroupWhenMember() {
            stubAuthenticatedAuthor();
            UUID groupId = UUID.randomUUID();
            when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(groupConversation(groupId)));
            when(groupMembershipChecker.isMember(groupId, authorMemberId.value())).thenReturn(true);

            service.post(command());

            verify(messageRepository).save(any());
        }
    }

    @Nested
    @DisplayName("Access")
    class Access {

        @Test
        @DisplayName("should throw ConversationNotFoundException when the conversation does not exist")
        void shouldThrowWhenConversationAbsent() {
            stubAuthenticatedAuthor();
            when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());

            assertThrows(ConversationNotFoundException.class, () -> service.post(command()));
            verify(messageRepository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }

        @Test
        @DisplayName("DIRECT: should throw NotAConversationParticipantException when the author is outside the pair")
        void shouldThrowWhenNotInPair() {
            stubAuthenticatedAuthor();
            ParticipantPair otherPair = ParticipantPair.of(UUID.randomUUID(), UUID.randomUUID());
            Conversation conversation = Conversation.reconstitute(
                    conversationId, ORGANISATION_ID, null, null, ConversationKind.DIRECT, otherPair, NOW);
            when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));

            assertThrows(NotAConversationParticipantException.class, () -> service.post(command()));
            verify(messageRepository, never()).save(any());
        }

        @Test
        @DisplayName("GROUP: should throw NotAConversationParticipantException when the author is not in the group")
        void shouldThrowWhenNotGroupMember() {
            stubAuthenticatedAuthor();
            UUID groupId = UUID.randomUUID();
            when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(groupConversation(groupId)));
            when(groupMembershipChecker.isMember(groupId, authorMemberId.value())).thenReturn(false);

            assertThrows(NotAConversationParticipantException.class, () -> service.post(command()));
            verify(messageRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Reply")
    class Reply {

        @Test
        @DisplayName("should post a reply referencing a message of the same conversation")
        void shouldPostReply() {
            stubAuthenticatedAuthor();
            when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(directConversationWithAuthor()));
            MessageId replyTo = MessageId.generate();
            Message target = Message.reconstitute(
                    replyTo, conversationId, UUID.randomUUID(), MessageContent.of("Originel"), null, NOW);
            when(messageRepository.findById(replyTo)).thenReturn(Optional.of(target));

            service.post(replyCommand(replyTo));

            ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
            verify(messageRepository).save(messageCaptor.capture());
            assertEquals(replyTo, messageCaptor.getValue().replyTo());
        }

        @Test
        @DisplayName("should throw InvalidReplyTargetException when the reply target does not exist")
        void shouldThrowWhenReplyTargetMissing() {
            stubAuthenticatedAuthor();
            when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(directConversationWithAuthor()));
            MessageId replyTo = MessageId.generate();
            when(messageRepository.findById(replyTo)).thenReturn(Optional.empty());

            assertThrows(InvalidReplyTargetException.class, () -> service.post(replyCommand(replyTo)));
            verify(messageRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw InvalidReplyTargetException when the reply target is in another conversation")
        void shouldThrowWhenReplyTargetInAnotherConversation() {
            stubAuthenticatedAuthor();
            when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(directConversationWithAuthor()));
            MessageId replyTo = MessageId.generate();
            Message target = Message.reconstitute(
                    replyTo, ConversationId.generate(), UUID.randomUUID(), MessageContent.of("Ailleurs"), null, NOW);
            when(messageRepository.findById(replyTo)).thenReturn(Optional.of(target));

            assertThrows(InvalidReplyTargetException.class, () -> service.post(replyCommand(replyTo)));
            verify(messageRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null currentMemberResolver")
        void shouldRejectNullCurrentMemberResolver() {
            assertThrows(NullPointerException.class, () -> new PostMessageService(
                    null, conversationRepository, groupMembershipChecker, messageRepository, publisher, clock));
        }

        @Test
        @DisplayName("should reject null conversationRepository")
        void shouldRejectNullConversationRepository() {
            assertThrows(NullPointerException.class, () -> new PostMessageService(
                    currentMemberResolver, null, groupMembershipChecker, messageRepository, publisher, clock));
        }

        @Test
        @DisplayName("should reject null groupMembershipChecker")
        void shouldRejectNullGroupMembershipChecker() {
            assertThrows(NullPointerException.class, () -> new PostMessageService(
                    currentMemberResolver, conversationRepository, null, messageRepository, publisher, clock));
        }

        @Test
        @DisplayName("should reject null messageRepository")
        void shouldRejectNullMessageRepository() {
            assertThrows(NullPointerException.class, () -> new PostMessageService(
                    currentMemberResolver, conversationRepository, groupMembershipChecker, null, publisher, clock));
        }

        @Test
        @DisplayName("should reject null publisher")
        void shouldRejectNullPublisher() {
            assertThrows(NullPointerException.class, () -> new PostMessageService(
                    currentMemberResolver, conversationRepository, groupMembershipChecker, messageRepository, null, clock));
        }

        @Test
        @DisplayName("should reject null clock")
        void shouldRejectNullClock() {
            assertThrows(NullPointerException.class, () -> new PostMessageService(
                    currentMemberResolver, conversationRepository, groupMembershipChecker, messageRepository, publisher, null));
        }

        @Test
        @DisplayName("should reject null command conversationId")
        void shouldRejectNullCommandConversationId() {
            assertThrows(NullPointerException.class, () -> new PostMessageCommand(null, CONTENT, null));
        }

        @Test
        @DisplayName("should reject null command content")
        void shouldRejectNullCommandContent() {
            assertThrows(NullPointerException.class,
                    () -> new PostMessageCommand(conversationId.value().toString(), null, null));
        }
    }
}
