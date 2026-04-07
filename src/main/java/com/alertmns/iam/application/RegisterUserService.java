package com.alertmns.iam.application;

import com.alertmns.iam.domain.exception.EmailAlreadyExistsException;
import com.alertmns.iam.domain.model.Email;
import com.alertmns.iam.domain.model.FirstName;
import com.alertmns.iam.domain.model.HashedPassword;
import com.alertmns.iam.domain.model.LastName;
import com.alertmns.iam.domain.model.Profile;
import com.alertmns.iam.domain.model.User;
import com.alertmns.iam.domain.port.incoming.RegisterUserUseCase;
import com.alertmns.iam.domain.port.incoming.command.RegisterUserCommand;
import com.alertmns.iam.domain.port.outgoing.AuthenticationPort;
import com.alertmns.iam.domain.port.outgoing.EventPublisher;
import com.alertmns.iam.domain.port.outgoing.UserRepository;

import java.util.Objects;

/**
 * Service applicatif pour l'inscription des utilisateurs.
 * Orchestre le processus d'inscription : validation, logique métier,
 * persistance et publication des événements du domaine.
 */
final public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepository repository;
    private final AuthenticationPort authentication;
    private final EventPublisher publisher;

    public RegisterUserService(
            UserRepository repository,
            AuthenticationPort authentication,
            EventPublisher publisher
    ) {
        this.repository = Objects.requireNonNull(
                repository, "repository must not be null"
        );

        this.authentication = Objects.requireNonNull(
                authentication, "authentication must not be null"
        );

        this.publisher = Objects.requireNonNull(
                publisher, "publisher must not be null"
        );
    }

    @Override
    public void register(RegisterUserCommand command) {
        Email email = Email.of(command.email());

        if (repository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        HashedPassword hashedPassword = HashedPassword.of(
                authentication.hashPassword(command.rawPassword())
        );

        FirstName firstName = FirstName.of(command.firstName());
        LastName lastName = LastName.of(command.lastName());
        Profile profile = Profile.of(firstName, lastName);
        User user = User.register(email, hashedPassword, profile);
        repository.save(user);
        publisher.publish(user.pullDomainEvents());
    }
}
