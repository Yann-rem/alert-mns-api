package com.alertmns.organisation.application;

import com.alertmns.organisation.domain.exception.GroupMembershipAlreadyExistsException;
import com.alertmns.organisation.domain.exception.GroupNotFoundException;
import com.alertmns.organisation.domain.exception.MemberNotFoundException;
import com.alertmns.organisation.domain.exception.OrganisationMismatchException;
import com.alertmns.organisation.domain.model.Group;
import com.alertmns.organisation.domain.model.GroupId;
import com.alertmns.organisation.domain.model.GroupMembership;
import com.alertmns.organisation.domain.model.GroupMembershipId;
import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.port.incoming.AddMemberToGroupUseCase;
import com.alertmns.organisation.domain.port.incoming.command.AddMemberToGroupCommand;
import com.alertmns.organisation.domain.port.outgoing.GroupMembershipRepository;
import com.alertmns.organisation.domain.port.outgoing.GroupRepository;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import com.alertmns.shared.EventPublisher;

import java.util.Objects;

/**
 * Service applicatif représentant l'orchestration de l'ajout d'un membre à un groupe.
 *
 * <p>Parse (VOs) → load (group + member) → check (organisation match + unicité adhésion)
 * → act (GroupMembership.add) → save → publish.</p>
 */
public final class AddMemberToGroupService implements AddMemberToGroupUseCase {

    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;
    private final GroupMembershipRepository groupMembershipRepository;
    private final EventPublisher publisher;

    public AddMemberToGroupService(
            GroupRepository groupRepository,
            MemberRepository memberRepository,
            GroupMembershipRepository groupMembershipRepository,
            EventPublisher publisher
    ) {
        this.groupRepository = Objects.requireNonNull(groupRepository, "groupRepository must not be null");
        this.memberRepository = Objects.requireNonNull(memberRepository, "memberRepository must not be null");
        this.groupMembershipRepository = Objects.requireNonNull(
                groupMembershipRepository,
                "groupMembershipRepository must not be null"
        );
        this.publisher = Objects.requireNonNull(publisher, "publisher must not be null");
    }

    @Override
    public GroupMembershipId add(AddMemberToGroupCommand command) {
        GroupId groupId = GroupId.from(command.groupId());
        MemberId memberId = MemberId.from(command.memberId());

        Group group = groupRepository.findById(groupId).orElseThrow(() -> new GroupNotFoundException(groupId));
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new MemberNotFoundException(memberId));

        if (!member.organisationId().equals(group.organisationId())) {
            throw new OrganisationMismatchException(group.organisationId(), member.organisationId());
        }
        if (groupMembershipRepository.existsByGroupIdAndMemberId(groupId, memberId)) {
            throw new GroupMembershipAlreadyExistsException(groupId, memberId);
        }

        GroupMembership groupMembership = GroupMembership.add(groupId, memberId);
        groupMembershipRepository.save(groupMembership);
        publisher.publish(groupMembership.pullDomainEvents());
        return groupMembership.id();
    }
}
