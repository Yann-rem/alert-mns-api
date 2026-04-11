package com.alertmns.iam.application;

import com.alertmns.iam.domain.exception.UserNotFoundException;
import com.alertmns.iam.domain.model.User;
import com.alertmns.iam.domain.model.UserId;
import com.alertmns.iam.domain.port.incoming.ReactivateUserUseCase;
import com.alertmns.iam.domain.port.incoming.command.ReactivateUserCommand;
import com.alertmns.shared.EventPublisher;
import com.alertmns.iam.domain.port.outgoing.UserRepository;

import java.util.Objects;

/**
 * Service applicatif représentant l'orchestration de la réactivation des utilisateurs.
 *
 * <p>Charge l'agrégat → réactive → persiste → publie les événements.</p>
 */
public final class ReactivateUserService implements ReactivateUserUseCase {

    private final UserRepository repository;
    private final EventPublisher publisher;

    public ReactivateUserService(UserRepository repository, EventPublisher publisher) {
        this.repository = Objects.requireNonNull(
                repository, "repository must not be null"
        );

        this.publisher = Objects.requireNonNull(
                publisher, "publisher must not be null"
        );
    }

    @Override
    public void reactivate(ReactivateUserCommand command) {
        UserId id = UserId.from(command.userId());
        User user = repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        user.reactivate();
        repository.save(user);
        publisher.publish(user.pullDomainEvents());
    }
}
