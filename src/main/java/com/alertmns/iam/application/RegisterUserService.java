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
 * Orchestre le processus d'inscription : validation, logique métier, persistance et publication d'événements.
 */
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepository userRepository;
    private final AuthenticationPort authenticationPort;
    private final EventPublisher eventPublisher;

    public RegisterUserService(
            UserRepository userRepository,
            AuthenticationPort authenticationPort,
            EventPublisher eventPublisher
    ) {
        this.userRepository = Objects.requireNonNull(userRepository, "UserRepository ne peut pas être null");
        this.authenticationPort = Objects.requireNonNull(authenticationPort, "AuthenticationPort ne peut pas être null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "EventPublisher ne peut pas être null");
    }

    @Override
    public void register(RegisterUserCommand command) {
        Email email = Email.of(command.email());

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        HashedPassword hashedPassword = HashedPassword.of(
                authenticationPort.hashPassword(command.rawPassword())
        );

        FirstName firstName = FirstName.of(command.firstName());
        LastName lastName = LastName.of(command.lastName());
        Profile profile = Profile.of(firstName, lastName);
        User user = User.register(email, hashedPassword, profile);
        userRepository.save(user);
        eventPublisher.publish(user.pullDomainEvents());
    }
}
