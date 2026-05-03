package com.alertmns.iam.application;

import com.alertmns.iam.domain.exception.UserNotFoundException;
import com.alertmns.iam.domain.model.User;
import com.alertmns.shared.UserId;
import com.alertmns.iam.domain.port.incoming.ReactivateUserUseCase;
import com.alertmns.iam.domain.port.incoming.command.ReactivateUserCommand;
import com.alertmns.iam.domain.port.outgoing.UserRepository;
import com.alertmns.shared.EventPublisher;

import java.util.Objects;

/**
 * Service applicatif représentant l'orchestration de la réactivation des utilisateurs.
 *
 * <p>Parse (VO id) → load (agrégat) → act (reactivate) → save → publish.</p>
 */
public final class ReactivateUserService implements ReactivateUserUseCase {

    private final UserRepository repository;
    private final EventPublisher publisher;

    public ReactivateUserService(UserRepository repository, EventPublisher publisher) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.publisher = Objects.requireNonNull(publisher, "publisher must not be null");
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
