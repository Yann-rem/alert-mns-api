package com.alertmns.organisation.application;

import com.alertmns.organisation.domain.exception.GroupNameAlreadyExistsException;
import com.alertmns.organisation.domain.model.Group;
import com.alertmns.organisation.domain.model.GroupId;
import com.alertmns.organisation.domain.model.GroupName;
import com.alertmns.organisation.domain.port.incoming.CreateGroupUseCase;
import com.alertmns.organisation.domain.port.incoming.command.CreateGroupCommand;
import com.alertmns.organisation.domain.port.outgoing.GroupRepository;
import com.alertmns.shared.EventPublisher;
import com.alertmns.shared.OrganisationId;

import java.util.Objects;

/**
 * Service applicatif représentant l'orchestration de la création des groupes.
 *
 * <p>Valide → vérifie l'unicité du nom dans l'organisation → crée l'agrégat →
 * persiste → publie les événements.</p>
 */
public final class CreateGroupService implements CreateGroupUseCase {

    private final GroupRepository repository;
    private final EventPublisher publisher;

    public CreateGroupService(GroupRepository repository, EventPublisher publisher) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.publisher = Objects.requireNonNull(publisher, "publisher must not be null");
    }

    @Override
    public GroupId create(CreateGroupCommand command) {
        OrganisationId organisationId = OrganisationId.from(command.organisationId());
        GroupName name = GroupName.of(command.name());

        if (repository.existsByOrganisationIdAndGroupName(organisationId, name)) {
            throw new GroupNameAlreadyExistsException(organisationId, name);
        }

        Group group = Group.create(organisationId, name);
        repository.save(group);
        publisher.publish(group.pullDomainEvents());
        return group.id();
    }
}
