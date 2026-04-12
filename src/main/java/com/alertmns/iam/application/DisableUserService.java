package com.alertmns.iam.application;

import com.alertmns.iam.domain.exception.UserNotFoundException;
import com.alertmns.iam.domain.model.User;
import com.alertmns.iam.domain.model.UserId;
import com.alertmns.iam.domain.port.incoming.DisableUserUseCase;
import com.alertmns.iam.domain.port.incoming.command.DisableUserCommand;
import com.alertmns.shared.EventPublisher;
import com.alertmns.iam.domain.port.outgoing.UserRepository;

import java.util.Objects;

/**
 * Service applicatif représentant l’orchestration de la désactivation des utilisateurs.
 *
 * <p>Charge l’agrégat → désactive → persiste → publie les événements.</p>
 */
public final class DisableUserService implements DisableUserUseCase {

    private final UserRepository repository;
    private final EventPublisher publisher;

    public DisableUserService(UserRepository repository, EventPublisher publisher) {
        this.repository = Objects.requireNonNull(
                repository, "repository must not be null"
        );

        this.publisher = Objects.requireNonNull(
                publisher, "publisher must not be null"
        );
    }

    @Override
    public void disable(DisableUserCommand command) {
        UserId id = UserId.from(command.userId());
        User user = repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        user.disable();
        repository.save(user);
        publisher.publish(user.pullDomainEvents());
    }
}
