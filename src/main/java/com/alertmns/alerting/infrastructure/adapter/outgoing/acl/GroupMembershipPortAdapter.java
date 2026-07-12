package com.alertmns.alerting.infrastructure.adapter.outgoing.acl;

import com.alertmns.alerting.domain.port.outgoing.GroupMembershipPort;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.port.outgoing.GroupMembershipRepository;

import java.util.List;
import java.util.UUID;

/**
 * Adapter d'anti-corruption traduisant le besoin du BC Alerting (« quels sont les groupes de ce membre ? ») vers le
 * port du BC Organisation.
 *
 * <p>Doublon fonctionnel assumé de l'adapter homonyme du BC Messaging : chaque BC déclare son propre port ACL (ISP).
 * Contrairement à {@code CurrentMemberResolver} (extrait vers Organisation), on ne mutualise pas cet adaptateur d'une
 * ligne — ce serait coupler les BC entre eux.</p>
 */
public final class GroupMembershipPortAdapter implements GroupMembershipPort {

    private final GroupMembershipRepository groupMembershipRepository;

    public GroupMembershipPortAdapter(GroupMembershipRepository groupMembershipRepository) {
        this.groupMembershipRepository = groupMembershipRepository;
    }

    @Override
    public List<UUID> groupIdsOf(UUID memberId) {
        return groupMembershipRepository.findByMemberId(MemberId.from(memberId))
                .stream()
                .map(membership -> membership.groupId().value())
                .toList();
    }
}
