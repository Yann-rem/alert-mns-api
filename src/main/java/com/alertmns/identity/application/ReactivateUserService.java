package com.alertmns.identity.application;

import com.alertmns.identity.domain.exception.UserNotFoundException;
import com.alertmns.identity.domain.model.User;
import com.alertmns.identity.domain.port.incoming.ReactivateUserUseCase;
import com.alertmns.identity.domain.port.incoming.command.ReactivateUserCommand;
import com.alertmns.identity.domain.port.outgoing.UserRepository;
import com.alertmns.shared.EventPublisher;
import com.alertmns.shared.UserId;

import java.time.Clock;
import java.util.Objects;

/**
 * Service applicatif représentant l'orchestration de la réactivation des utilisateurs.
 *
 * <p>Parse (VO id) → load (agrégat) → act (reactivate) → save → publish.</p>
 */
public final class ReactivateUserService implements ReactivateUserUseCase {

    private final UserRepository repository;
    private final EventPublisher publisher;
    private final Clock clock;

    public ReactivateUserService(UserRepository repository, EventPublisher publisher, Clock clock) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.publisher = Objects.requireNonNull(publisher, "publisher must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public void reactivate(ReactivateUserCommand command) {
        UserId id = UserId.from(command.userId());
        User user = repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        user.reactivate(clock.instant());
        repository.save(user);
        publisher.publish(user.pullDomainEvents());
    }
}
