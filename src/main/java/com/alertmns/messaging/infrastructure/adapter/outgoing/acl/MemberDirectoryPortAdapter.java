package com.alertmns.messaging.infrastructure.adapter.outgoing.acl;

import com.alertmns.messaging.domain.port.outgoing.MemberDirectoryPort;
import com.alertmns.organisation.domain.model.GroupId;
import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter d'anti-corruption traduisant les besoins de résolution de membres du BC Messaging vers le BC Organisation.
 *
 * <p>Doublon d'ACL assumé par BC (ISP), à l'image de {@code GroupMembershipPortAdapter}.</p>
 */
public final class MemberDirectoryPortAdapter implements MemberDirectoryPort {

    private final MemberRepository memberRepository;

    public MemberDirectoryPortAdapter(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public List<UUID> directRecipients(UUID memberIdA, UUID memberIdB) {
        return memberRepository.findUserIdsByIdIn(List.of(MemberId.from(memberIdA), MemberId.from(memberIdB)));
    }

    @Override
    public List<UUID> groupRecipients(UUID groupId) {
        return memberRepository.findUserIdsByGroupId(GroupId.from(groupId));
    }

    @Override
    public Optional<UUID> userIdOf(UUID memberId) {
        return memberRepository.findById(MemberId.from(memberId)).map(Member::userId);
    }
}
