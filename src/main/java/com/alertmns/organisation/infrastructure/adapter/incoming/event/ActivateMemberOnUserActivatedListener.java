package com.alertmns.organisation.infrastructure.adapter.incoming.event;

import com.alertmns.identity.domain.event.UserActivated;
import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.port.incoming.ActivateMemberUseCase;
import com.alertmns.organisation.domain.port.incoming.command.ActivateMemberCommand;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import org.springframework.context.event.EventListener;

import java.util.Objects;
import java.util.UUID;

/**
 * Listener qui active automatiquement le {@link Member} d'un utilisateur lorsque celui-ci active son compte Identity.
 *
 * <p>Cascade prescrite par ADR-0015 : la transition {@code User.PENDING → ACTIVE} (via redeem du magic-link)
 * déclenche la transition correspondante {@code Member.PENDING → ACTIVE} pour l'unique Member rattaché à ce user.</p>
 */
public final class ActivateMemberOnUserActivatedListener {

    private final MemberRepository memberRepository;
    private final ActivateMemberUseCase activateMemberUseCase;

    public ActivateMemberOnUserActivatedListener(
            MemberRepository memberRepository,
            ActivateMemberUseCase activateMemberUseCase
    ) {
        this.memberRepository = Objects.requireNonNull(memberRepository, "memberRepository must not be null");
        this.activateMemberUseCase = Objects.requireNonNull(
                activateMemberUseCase, "activateMemberUseCase must not be null");
    }

    @EventListener
    public void onUserActivated(UserActivated event) {
        UUID userId = event.userId().value();

        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("No Member found for activated user: " + userId));

        activateMemberUseCase.activate(new ActivateMemberCommand(
                member.organisationId().value().toString(),
                member.id().value().toString()
        ));
    }
}
