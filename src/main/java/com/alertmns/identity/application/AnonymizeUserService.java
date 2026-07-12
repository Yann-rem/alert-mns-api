package com.alertmns.identity.application;

import com.alertmns.identity.domain.exception.UserNotFoundException;
import com.alertmns.identity.domain.model.User;
import com.alertmns.identity.domain.port.incoming.AnonymizeUserUseCase;
import com.alertmns.identity.domain.port.incoming.command.AnonymizeUserCommand;
import com.alertmns.identity.domain.port.outgoing.UserRepository;
import com.alertmns.shared.EventPublisher;
import com.alertmns.shared.UserId;

import java.time.Clock;
import java.util.Objects;

/**
 * Service applicatif orchestrant l'anonymisation d'un utilisateur (droit à l'effacement RGPD — doctrine ADR-0017).
 *
 * <p>Parse (VO id) → load (agrégat) → act (anonymize) → save → publish. Aucune logique métier : l'effacement des PII et
 * l'idempotence sont portés par l'agrégat {@link User#anonymize}.</p>
 */
public final class AnonymizeUserService implements AnonymizeUserUseCase {

    private final UserRepository repository;
    private final EventPublisher publisher;
    private final Clock clock;

    public AnonymizeUserService(UserRepository repository, EventPublisher publisher, Clock clock) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.publisher = Objects.requireNonNull(publisher, "publisher must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public void anonymize(AnonymizeUserCommand command) {
        UserId id = UserId.from(command.userId());
        User user = repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        user.anonymize(clock.instant());
        repository.save(user);
        publisher.publish(user.pullDomainEvents());
    }
}
