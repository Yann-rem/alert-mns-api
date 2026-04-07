package com.alertmns.iam.application;

import com.alertmns.iam.domain.exception.UserNotFoundException;
import com.alertmns.iam.domain.model.User;
import com.alertmns.iam.domain.model.UserId;
import com.alertmns.iam.domain.port.incoming.ActivateUserUseCase;
import com.alertmns.iam.domain.port.incoming.command.ActivateUserCommand;
import com.alertmns.iam.domain.port.outgoing.EventPublisher;
import com.alertmns.iam.domain.port.outgoing.UserRepository;

import java.util.Objects;

/**
 * Service applicatif pour l'activation des utilisateurs.
 * Orchestre le processus d'activation : chargement de l’agrégat, logique métier,
 * persistance et publication des événements du domaine.
 */
final public class ActivateUserService implements ActivateUserUseCase {

    private final UserRepository repository;
    private final EventPublisher publisher;

    public ActivateUserService(UserRepository repository, EventPublisher publisher) {
        this.repository = Objects.requireNonNull(
                repository, "repository must not be null"
        );

        this.publisher = Objects.requireNonNull(
                publisher, "publisher must not be null"
        );
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
