package com.alertmns.alerting.infrastructure.adapter.outgoing.acl;

import com.alertmns.alerting.domain.port.outgoing.GroupDirectoryPort;
import com.alertmns.organisation.domain.model.Group;
import com.alertmns.organisation.domain.model.GroupId;
import com.alertmns.organisation.domain.model.GroupName;
import com.alertmns.organisation.domain.port.outgoing.GroupRepository;

import java.util.UUID;

/**
 * Adapter d'anti-corruption traduisant « comment s'appelle ce groupe ? » vers le BC Organisation.
 *
 * <p>Doublon d'ACL assumé par BC (ISP), à l'image de {@code GroupMembershipPortAdapter}.</p>
 */
public final class GroupDirectoryPortAdapter implements GroupDirectoryPort {

    private final GroupRepository groupRepository;

    public GroupDirectoryPortAdapter(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @Override
    public String nameOf(UUID groupId) {
        if (groupId == null) {
            return DELETED_GROUP_DISPLAY_NAME;
        }
        return groupRepository.findById(GroupId.from(groupId))
                .map(Group::name)
                .map(GroupName::value)
                .orElse(DELETED_GROUP_DISPLAY_NAME);
    }
}
