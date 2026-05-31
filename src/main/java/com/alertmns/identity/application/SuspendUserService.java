package com.alertmns.identity.application;

import com.alertmns.identity.domain.exception.UserNotFoundException;
import com.alertmns.identity.domain.model.User;
import com.alertmns.identity.domain.port.incoming.SuspendUserUseCase;
import com.alertmns.identity.domain.port.incoming.command.SuspendUserCommand;
import com.alertmns.identity.domain.port.outgoing.UserRepository;
import com.alertmns.shared.EventPublisher;
import com.alertmns.shared.UserId;

import java.time.Clock;
import java.util.Objects;

/**
 * Service applicatif représentant l'orchestration de la suspension des utilisateurs.
 *
 * <p>Parse (VO id) → load (agrégat) → act (suspend) → save → publish.</p>
 */
public final class SuspendUserService implements SuspendUserUseCase {

    private final UserRepository repository;
    private final EventPublisher publisher;
    private final Clock clock;

    public SuspendUserService(UserRepository repository, EventPublisher publisher, Clock clock) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.publisher = Objects.requireNonNull(publisher, "publisher must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public void suspend(SuspendUserCommand command) {
        UserId id = UserId.from(command.userId());
        User user = repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        user.suspend(clock.instant());
        repository.save(user);
        publisher.publish(user.pullDomainEvents());
    }
}
