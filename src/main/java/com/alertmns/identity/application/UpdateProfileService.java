package com.alertmns.identity.application;

import com.alertmns.identity.domain.exception.UserNotFoundException;
import com.alertmns.identity.domain.model.FirstName;
import com.alertmns.identity.domain.model.LastName;
import com.alertmns.identity.domain.model.User;
import com.alertmns.identity.domain.port.incoming.UpdateProfileUseCase;
import com.alertmns.identity.domain.port.incoming.command.UpdateProfileCommand;
import com.alertmns.identity.domain.port.outgoing.UserRepository;
import com.alertmns.shared.EventPublisher;
import com.alertmns.shared.UserId;

import java.time.Clock;
import java.util.Objects;

/**
 * Service applicatif représentant l'orchestration de la mise à jour du profil d'un utilisateur.
 *
 * <p>Parse (VOs) → load (agrégat) → act (updateProfile) → save → publish.</p>
 */
public final class UpdateProfileService implements UpdateProfileUseCase {

    private final UserRepository repository;
    private final EventPublisher publisher;
    private final Clock clock;

    public UpdateProfileService(UserRepository repository, EventPublisher publisher, Clock clock) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.publisher = Objects.requireNonNull(publisher, "publisher must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public void update(UpdateProfileCommand command) {
        UserId id = UserId.from(command.userId());
        FirstName firstName = FirstName.of(command.firstName());
        LastName lastName = LastName.of(command.lastName());
        User user = repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        user.updateProfile(firstName, lastName, command.avatar(), clock.instant());
        repository.save(user);
        publisher.publish(user.pullDomainEvents());
    }
}
