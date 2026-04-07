package com.alertmns.iam.application;

import com.alertmns.iam.domain.exception.UserNotFoundException;
import com.alertmns.iam.domain.model.User;
import com.alertmns.iam.domain.model.UserId;
import com.alertmns.iam.domain.port.incoming.DisableUserUseCase;
import com.alertmns.iam.domain.port.incoming.command.DisableUserCommand;
import com.alertmns.iam.domain.port.outgoing.EventPublisher;
import com.alertmns.iam.domain.port.outgoing.UserRepository;

import java.util.Objects;

/**
 * Service applicatif pour la désactivation des utilisateurs.
 * Orchestre le processus de désactivation : chargement de l’agrégat, logique métier,
 * persistance et publication des événements du domaine.
 */
final public class DisableUserService implements DisableUserUseCase {

    private final UserRepository repository;
    private final EventPublisher publisher;

    public DisableUserService(UserRepository repository, EventPublisher publisher) {
        this.repository = Objects.requireNonNull(
                repository, "UserRepository ne peut pas être null"
        );

        this.publisher = Objects.requireNonNull(
                publisher, "EventPublisher ne peut pas être null"
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
