package com.alertmns.organisation.application;

import com.alertmns.organisation.domain.model.Group;
import com.alertmns.organisation.domain.port.incoming.ListGroupsUseCase;
import com.alertmns.organisation.domain.port.incoming.command.ListGroupsQuery;
import com.alertmns.organisation.domain.port.outgoing.GroupRepository;
import com.alertmns.shared.OrganisationId;

import java.util.List;
import java.util.Objects;

/** Liste paginée et filtrable des groupes d'une organisation. */
public class ListGroupsService implements ListGroupsUseCase {

    private final GroupRepository groupRepository;

    public ListGroupsService(GroupRepository groupRepository) {
        this.groupRepository = Objects.requireNonNull(groupRepository, "groupRepository must not be null");
    }

    @Override
    public GroupsPage list(ListGroupsQuery query) {
        Objects.requireNonNull(query, "query must not be null");

        OrganisationId organisationId = OrganisationId.from(query.organisationId());
        String search = (query.search() == null || query.search().isBlank())
                ? null
                : query.search().trim();

        List<Group> groups = groupRepository.findByOrganisationId(
                organisationId, search, query.page(), query.size());
        return new GroupsPage(groups, groupRepository.countByOrganisationId(organisationId, search));
    }
}
