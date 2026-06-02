package com.alertmns.organisation.application;

import com.alertmns.organisation.domain.exception.OrganisationNameAlreadyExistsException;
import com.alertmns.organisation.domain.model.Organisation;
import com.alertmns.organisation.domain.model.OrganisationName;
import com.alertmns.organisation.domain.port.incoming.CreateOrganisationUseCase;
import com.alertmns.organisation.domain.port.incoming.command.CreateOrganisationCommand;
import com.alertmns.organisation.domain.port.outgoing.OrganisationRepository;
import com.alertmns.shared.EventPublisher;
import com.alertmns.shared.OrganisationId;

import java.time.Clock;
import java.util.Objects;

/**
 * Service applicatif représentant l'orchestration de la création des organisations.
 *
 * <p>Parse (VO name) → check (unicité du nom) → act (Organisation.create) → save → publish.</p>
 */
public final class CreateOrganisationService implements CreateOrganisationUseCase {

    private final OrganisationRepository repository;
    private final EventPublisher publisher;
    private final Clock clock;

    public CreateOrganisationService(OrganisationRepository repository, EventPublisher publisher, Clock clock) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.publisher = Objects.requireNonNull(publisher, "publisher must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public OrganisationId create(CreateOrganisationCommand command) {
        OrganisationName name = OrganisationName.of(command.name());

        if (repository.existsByName(name)) {
            throw new OrganisationNameAlreadyExistsException(name);
        }

        Organisation organisation = Organisation.create(name, clock.instant());
        repository.save(organisation);
        publisher.publish(organisation.pullDomainEvents());
        return organisation.id();
    }
}
