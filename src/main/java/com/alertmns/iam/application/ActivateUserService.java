package com.alertmns.iam.application;

import com.alertmns.iam.domain.exception.UserNotFoundException;
import com.alertmns.iam.domain.model.User;
import com.alertmns.shared.UserId;
import com.alertmns.iam.domain.port.incoming.ActivateUserUseCase;
import com.alertmns.iam.domain.port.incoming.command.ActivateUserCommand;
import com.alertmns.iam.domain.port.outgoing.UserRepository;
import com.alertmns.shared.EventPublisher;

import java.util.Objects;

/**
 * Service applicatif représentant l'orchestration de l'activation des utilisateurs.
 *
 * <p>Parse (VO id) → load (agrégat) → act (activate) → save → publish.</p>
 */
public final class ActivateUserService implements ActivateUserUseCase {

    private final UserRepository repository;
    private final EventPublisher publisher;

    public ActivateUserService(UserRepository repository, EventPublisher publisher) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.publisher = Objects.requireNonNull(publisher, "publisher must not be null");
    }

    @Override
    public void activate(ActivateUserCommand command) {
        UserId id = UserId.from(command.userId());
        User user = repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        user.activate();
        repository.save(user);
        publisher.publish(user.pullDomainEvents());
    }
}
