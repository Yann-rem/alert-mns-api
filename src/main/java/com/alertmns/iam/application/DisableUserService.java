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
public class DisableUserService implements DisableUserUseCase {

    private final UserRepository userRepository;
    private final EventPublisher eventPublisher;

    public DisableUserService(UserRepository userRepository, EventPublisher eventPublisher) {
        this.userRepository = Objects.requireNonNull(
                userRepository, "UserRepository ne peut pas être null"
        );

        this.eventPublisher = Objects.requireNonNull(
                eventPublisher, "EventPublisher ne peut pas être null"
        );
    }

    @Override
    public void disable(DisableUserCommand command) {
        UserId id = UserId.from(command.userId());

        User foundUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        foundUser.disable();
        userRepository.save(foundUser);
        eventPublisher.publish(foundUser.pullDomainEvents());
    }
}
