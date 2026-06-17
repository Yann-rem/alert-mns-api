package com.alertmns.messaging.application;

import com.alertmns.messaging.domain.model.Conversation;
import com.alertmns.messaging.domain.model.ConversationId;
import com.alertmns.messaging.domain.model.ParticipantPair;
import com.alertmns.messaging.domain.port.incoming.CreateDirectConversationUseCase;
import com.alertmns.messaging.domain.port.incoming.command.CreateDirectConversationCommand;
import com.alertmns.messaging.domain.port.outgoing.ConversationRepository;
import com.alertmns.organisation.domain.exception.MemberNotFoundException;
import com.alertmns.organisation.domain.exception.OrganisationMismatchException;
import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import com.alertmns.shared.CurrentUserPort;
import com.alertmns.shared.EventPublisher;
import com.alertmns.shared.UserId;

import java.time.Clock;
import java.util.Objects;

/**
 * Service applicatif orchestrant la création d'une conversation directe entre deux membres.
 *
 * <p>Parse (cible) → resolve (utilisateur courant → Member) → load + check (cible, même organisation) → act
 * (Conversation.createDirect) → save → publish. Idempotent : retourne la conversation existante pour la paire
 * canonique de participants si celle-ci a déjà été créée.</p>
 */
public final class CreateDirectConversationService implements CreateDirectConversationUseCase {

    private final CurrentUserPort currentUserPort;
    private final MemberRepository memberRepository;
    private final ConversationRepository conversationRepository;
    private final EventPublisher publisher;
    private final Clock clock;

    public CreateDirectConversationService(
            CurrentUserPort currentUserPort,
            MemberRepository memberRepository,
            ConversationRepository conversationRepository,
            EventPublisher publisher,
            Clock clock
    ) {
        this.currentUserPort = Objects.requireNonNull(currentUserPort, "currentUserPort must not be null");
        this.memberRepository = Objects.requireNonNull(memberRepository, "memberRepository must not be null");
        this.conversationRepository = Objects.requireNonNull(
                conversationRepository, "conversationRepository must not be null");
        this.publisher = Objects.requireNonNull(publisher, "publisher must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public ConversationId create(CreateDirectConversationCommand command) {
        UserId currentUserId = currentUserPort.currentUser()
                .orElseThrow(() -> new IllegalStateException("No authenticated user in context"))
                .userId();

        Member initiator = memberRepository.findByUserId(currentUserId.value())
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user has no member: " + currentUserId.value()));

        MemberId targetMemberId = MemberId.from(command.targetMemberId());
        Member target = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> new MemberNotFoundException(targetMemberId));

        if (!initiator.organisationId().equals(target.organisationId())) {
            throw new OrganisationMismatchException(initiator.organisationId(), target.organisationId());
        }

        ParticipantPair pair = ParticipantPair.of(initiator.id().value(), target.id().value());

        return conversationRepository.findByParticipants(pair)
                .map(Conversation::id)
                .orElseGet(() -> {
                    Conversation conversation = Conversation.createDirect(
                            initiator.organisationId(), pair, clock.instant());
                    conversationRepository.save(conversation);
                    publisher.publish(conversation.pullDomainEvents());
                    return conversation.id();
                });
    }
}
