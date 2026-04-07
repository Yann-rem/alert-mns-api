package com.alertmns.iam.application;

import com.alertmns.iam.domain.exception.UserNotFoundException;
import com.alertmns.iam.domain.model.FirstName;
import com.alertmns.iam.domain.model.LastName;
import com.alertmns.iam.domain.model.User;
import com.alertmns.iam.domain.model.UserId;
import com.alertmns.iam.domain.port.incoming.UpdateProfileUseCase;
import com.alertmns.iam.domain.port.incoming.command.UpdateProfileCommand;
import com.alertmns.iam.domain.port.outgoing.EventPublisher;
import com.alertmns.iam.domain.port.outgoing.UserRepository;

import java.util.Objects;

/**
 * Service applicatif pour la mise à jour du profil d’un utilisateur.
 * Orchestre le processus de mise à jour : chargement de l’agrégat, logique métier,
 * persistance et publication des événements du domaine.
 */
public class UpdateProfileService implements UpdateProfileUseCase {

    private final UserRepository repository;
    private final EventPublisher publisher;

    public UpdateProfileService(UserRepository repository, EventPublisher publisher) {
        this.repository = Objects.requireNonNull(
                repository, "UserRepository ne peut pas être null"
        );

        this.publisher = Objects.requireNonNull(
                publisher, "EventPublisher ne peut pas être null"
        );
    }

    @Override
    public void update(UpdateProfileCommand command) {
        UserId id = UserId.from(command.userId());
        User user = repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        FirstName firstName = FirstName.of(command.firstName());
        LastName lastName = LastName.of(command.lastName());
        user.updateProfile(firstName, lastName, command.avatar());
        repository.save(user);
        publisher.publish(user.pullDomainEvents());
    }
}
