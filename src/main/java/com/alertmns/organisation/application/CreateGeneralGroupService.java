package com.alertmns.organisation.application;

import com.alertmns.organisation.domain.model.Group;
import com.alertmns.organisation.domain.model.GroupName;
import com.alertmns.organisation.domain.port.incoming.CreateGeneralGroupUseCase;
import com.alertmns.organisation.domain.port.incoming.command.CreateGeneralGroupCommand;
import com.alertmns.organisation.domain.port.outgoing.GroupRepository;
import com.alertmns.shared.EventPublisher;
import com.alertmns.shared.OrganisationId;

import java.time.Clock;
import java.util.Objects;

/**
 * Service applicatif orchestrant la création (idempotente) du groupe GENERAL d'une organisation.
 *
 * <p>Parse (VO) → check (existence du GENERAL) → act (Group.createGeneral) → save → publish.
 * Idempotent : no-op si un groupe GENERAL existe déjà pour l'organisation.</p>
 */
public final class CreateGeneralGroupService implements CreateGeneralGroupUseCase {

    private final GroupRepository repository;
    private final EventPublisher publisher;
    private final Clock clock;

    public CreateGeneralGroupService(GroupRepository repository, EventPublisher publisher, Clock clock) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.publisher = Objects.requireNonNull(publisher, "publisher must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public void create(CreateGeneralGroupCommand command) {
        OrganisationId organisationId = OrganisationId.from(command.organisationId());

        if (repository.existsGeneralByOrganisationId(organisationId)) {
            return;
        }

        GroupName name = GroupName.of(command.name());
        Group group = Group.createGeneral(organisationId, name, clock.instant());
        repository.save(group);
        publisher.publish(group.pullDomainEvents());
    }
}
