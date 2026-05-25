package com.alertmns.identity.application;

import com.alertmns.identity.domain.exception.EmailAlreadyExistsException;
import com.alertmns.identity.domain.model.FirstName;
import com.alertmns.identity.domain.model.HashedPassword;
import com.alertmns.identity.domain.model.LastName;
import com.alertmns.identity.domain.model.Profile;
import com.alertmns.identity.domain.model.User;
import com.alertmns.identity.domain.port.incoming.RegisterPendingUserUseCase;
import com.alertmns.identity.domain.port.incoming.command.RegisterPendingUserCommand;
import com.alertmns.identity.domain.port.outgoing.UserRepository;
import com.alertmns.shared.Email;
import com.alertmns.shared.EventPublisher;
import com.alertmns.shared.UserId;

import java.util.Objects;

/**
 * Service applicatif représentant l'orchestration de l'inscription d'un utilisateur en attente d'activation (PENDING).
 *
 * <p>Parse (VOs) → check (unicité email) → act (agrégat avec HashedPassword sentinelle) → save → publish.</p>
 */
public final class RegisterPendingUserService implements RegisterPendingUserUseCase {

    private final UserRepository repository;
    private final EventPublisher publisher;

    public RegisterPendingUserService(UserRepository repository, EventPublisher publisher) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.publisher = Objects.requireNonNull(publisher, "publisher must not be null");
    }

    @Override
    public UserId register(RegisterPendingUserCommand command) {
        Email email = Email.of(command.email());
        Profile profile = Profile.of(FirstName.of(command.firstName()), LastName.of(command.lastName()));

        if (repository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        User user = User.register(email, HashedPassword.unset(), profile);
        repository.save(user);
        publisher.publish(user.pullDomainEvents());
        return user.id();
    }
}
