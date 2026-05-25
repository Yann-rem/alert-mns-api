package com.alertmns.identity.application;

import com.alertmns.identity.domain.exception.EmailAlreadyExistsException;
import com.alertmns.identity.domain.model.Email;
import com.alertmns.identity.domain.model.FirstName;
import com.alertmns.identity.domain.model.HashedPassword;
import com.alertmns.identity.domain.model.LastName;
import com.alertmns.identity.domain.model.Profile;
import com.alertmns.identity.domain.model.RawPassword;
import com.alertmns.identity.domain.model.User;
import com.alertmns.identity.domain.port.incoming.RegisterUserUseCase;
import com.alertmns.identity.domain.port.incoming.command.RegisterUserCommand;
import com.alertmns.identity.domain.port.outgoing.PasswordHasher;
import com.alertmns.identity.domain.port.outgoing.UserRepository;
import com.alertmns.shared.EventPublisher;
import com.alertmns.shared.UserId;

import java.util.Objects;

/**
 * Service applicatif représentant l'orchestration de l'inscription des utilisateurs.
 *
 * <p>Parse (VOs) → check (unicité email) → act (hash + agrégat) → save → publish.</p>
 */
public final class RegisterUserService implements RegisterUserUseCase {

    private final UserRepository repository;
    private final PasswordHasher passwordHasher;
    private final EventPublisher publisher;

    public RegisterUserService(
            UserRepository repository,
            PasswordHasher passwordHasher,
            EventPublisher publisher
    ) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.passwordHasher = Objects.requireNonNull(passwordHasher, "passwordHasher must not be null");
        this.publisher = Objects.requireNonNull(publisher, "publisher must not be null");
    }

    @Override
    public UserId register(RegisterUserCommand command) {
        Email email = Email.of(command.email());
        RawPassword rawPassword = RawPassword.of(command.rawPassword());
        FirstName firstName = FirstName.of(command.firstName());
        LastName lastName = LastName.of(command.lastName());
        Profile profile = Profile.of(firstName, lastName);

        if (repository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        HashedPassword hashedPassword = passwordHasher.hash(rawPassword);

        User user = User.register(email, hashedPassword, profile);
        repository.save(user);
        publisher.publish(user.pullDomainEvents());
        return user.id();
    }
}
