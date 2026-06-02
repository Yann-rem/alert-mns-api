package com.alertmns.organisation.application;

import com.alertmns.organisation.domain.exception.GroupNotFoundException;
import com.alertmns.organisation.domain.exception.MemberNotFoundException;
import com.alertmns.organisation.domain.exception.OrganisationMismatchException;
import com.alertmns.organisation.domain.model.Group;
import com.alertmns.organisation.domain.model.GroupId;
import com.alertmns.organisation.domain.model.GroupMembership;
import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.port.incoming.AddMemberToGroupUseCase;
import com.alertmns.organisation.domain.port.incoming.command.AddMemberToGroupCommand;
import com.alertmns.organisation.domain.port.outgoing.GroupMembershipRepository;
import com.alertmns.organisation.domain.port.outgoing.GroupRepository;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import com.alertmns.shared.EventPublisher;
import com.alertmns.shared.OrganisationId;

import java.time.Clock;
import java.util.Objects;

/**
 * Service applicatif représentant l'orchestration de l'ajout d'un membre à un groupe.
 *
 * <p>Parse (VOs) → load (group + member) → check (tenant) → idempotence (no-op si l'adhésion existe)
 * → act (GroupMembership.add) → save → publish.</p>
 *
 * <p>L'opération est idempotente : si l'adhésion {@code (groupId, memberId)} existe déjà, le service
 * retourne sans publier d'événement ni écrire en base.</p>
 */
public final class AddMemberToGroupService implements AddMemberToGroupUseCase {

    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;
    private final GroupMembershipRepository groupMembershipRepository;
    private final EventPublisher publisher;
    private final Clock clock;

    public AddMemberToGroupService(
            GroupRepository groupRepository,
            MemberRepository memberRepository,
            GroupMembershipRepository groupMembershipRepository,
            EventPublisher publisher,
            Clock clock
    ) {
        this.groupRepository = Objects.requireNonNull(groupRepository, "groupRepository must not be null");
        this.memberRepository = Objects.requireNonNull(memberRepository, "memberRepository must not be null");
        this.groupMembershipRepository = Objects.requireNonNull(
                groupMembershipRepository,
                "groupMembershipRepository must not be null"
        );
        this.publisher = Objects.requireNonNull(publisher, "publisher must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public void add(AddMemberToGroupCommand command) {
        OrganisationId organisationId = OrganisationId.from(command.organisationId());
        GroupId groupId = GroupId.from(command.groupId());
        MemberId memberId = MemberId.from(command.memberId());

        Group group = groupRepository.findById(groupId).orElseThrow(() -> new GroupNotFoundException(groupId));
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new MemberNotFoundException(memberId));

        if (!organisationId.equals(group.organisationId())) {
            throw new OrganisationMismatchException(group.organisationId(), organisationId);
        }
        if (!member.organisationId().equals(group.organisationId())) {
            throw new OrganisationMismatchException(group.organisationId(), member.organisationId());
        }
        if (groupMembershipRepository.findByGroupIdAndMemberId(groupId, memberId).isPresent()) {
            return;
        }

        GroupMembership groupMembership = GroupMembership.add(organisationId, groupId, memberId, clock.instant());
        groupMembershipRepository.save(groupMembership);
        publisher.publish(groupMembership.pullDomainEvents());
    }
}
