package com.alertmns.organisation.application;

import com.alertmns.organisation.domain.exception.GroupNameAlreadyExistsException;
import com.alertmns.organisation.domain.exception.GroupNotFoundException;
import com.alertmns.organisation.domain.exception.OrganisationMismatchException;
import com.alertmns.organisation.domain.model.Group;
import com.alertmns.organisation.domain.model.GroupId;
import com.alertmns.organisation.domain.model.GroupName;
import com.alertmns.organisation.domain.port.incoming.RenameGroupUseCase;
import com.alertmns.organisation.domain.port.incoming.command.RenameGroupCommand;
import com.alertmns.organisation.domain.port.outgoing.GroupRepository;
import com.alertmns.shared.EventPublisher;
import com.alertmns.shared.OrganisationId;

import java.util.Objects;

/**
 * Service applicatif représentant l'orchestration du renommage des groupes.
 *
 * <p>Parse (VOs) → load (agrégat) → check (idempotence + unicité du nouveau nom) → act (rename) → save → publish.</p>
 */
public final class RenameGroupService implements RenameGroupUseCase {

    private final GroupRepository repository;
    private final EventPublisher publisher;

    public RenameGroupService(GroupRepository repository, EventPublisher publisher) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.publisher = Objects.requireNonNull(publisher, "publisher must not be null");
    }

    @Override
    public void rename(RenameGroupCommand command) {
        OrganisationId organisationId = OrganisationId.from(command.organisationId());
        GroupId groupId = GroupId.from(command.groupId());
        GroupName name = GroupName.of(command.name());
        Group group = repository.findById(groupId).orElseThrow(() -> new GroupNotFoundException(groupId));

        if (!group.organisationId().equals(organisationId)) {
            throw new OrganisationMismatchException(group.organisationId(), organisationId);
        }
        if (group.name().equals(name)) {
            return;
        }
        if (repository.existsByOrganisationIdAndGroupName(group.organisationId(), name)) {
            throw new GroupNameAlreadyExistsException(group.organisationId(), name);
        }

        group.rename(name);
        repository.save(group);
        publisher.publish(group.pullDomainEvents());
    }
}
