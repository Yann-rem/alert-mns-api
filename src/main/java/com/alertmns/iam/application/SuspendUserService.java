package com.alertmns.iam.application;

import com.alertmns.iam.domain.exception.UserNotFoundException;
import com.alertmns.iam.domain.model.User;
import com.alertmns.shared.UserId;
import com.alertmns.iam.domain.port.incoming.SuspendUserUseCase;
import com.alertmns.iam.domain.port.incoming.command.SuspendUserCommand;
import com.alertmns.iam.domain.port.outgoing.UserRepository;
import com.alertmns.shared.EventPublisher;

import java.util.Objects;

/**
 * Service applicatif représentant l'orchestration de la suspension des utilisateurs.
 *
 * <p>Parse (VO id) → load (agrégat) → act (suspend) → save → publish.</p>
 */
public final class SuspendUserService implements SuspendUserUseCase {

    private final UserRepository repository;
    private final EventPublisher publisher;

    public SuspendUserService(UserRepository repository, EventPublisher publisher) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.publisher = Objects.requireNonNull(publisher, "publisher must not be null");
    }

    @Override
    public void suspend(SuspendUserCommand command) {
        UserId id = UserId.from(command.userId());
        User user = repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        user.suspend();
        repository.save(user);
        publisher.publish(user.pullDomainEvents());
    }
}
