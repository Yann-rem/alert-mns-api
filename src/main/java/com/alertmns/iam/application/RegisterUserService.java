package com.alertmns.iam.application;

import com.alertmns.iam.domain.exception.EmailAlreadyExistsException;
import com.alertmns.iam.domain.model.Email;
import com.alertmns.iam.domain.model.FirstName;
import com.alertmns.iam.domain.model.HashedPassword;
import com.alertmns.iam.domain.model.LastName;
import com.alertmns.iam.domain.model.Profile;
import com.alertmns.iam.domain.model.User;
import com.alertmns.iam.domain.model.UserId;
import com.alertmns.iam.domain.port.incoming.RegisterUserUseCase;
import com.alertmns.shared.OrganisationId;
import com.alertmns.iam.domain.port.incoming.command.RegisterUserCommand;
import com.alertmns.iam.domain.port.outgoing.AuthenticationPort;
import com.alertmns.shared.EventPublisher;
import com.alertmns.iam.domain.port.outgoing.UserRepository;

import java.util.Objects;

/**
 * Service applicatif représentant l'orchestration de l'inscription des utilisateurs.
 *
 * <p>Valide → crée les VO → persiste → publie les événements.</p>
 */
public final class RegisterUserService implements RegisterUserUseCase {

    private final UserRepository repository;
    private final AuthenticationPort authentication;
    private final EventPublisher publisher;

    public RegisterUserService(
            UserRepository repository,
            AuthenticationPort authentication,
            EventPublisher publisher
    ) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.authentication = Objects.requireNonNull(authentication, "authentication must not be null");
        this.publisher = Objects.requireNonNull(publisher, "publisher must not be null");
    }

    @Override
    public UserId register(RegisterUserCommand command) {
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
        OrganisationId organisationId = OrganisationId.from(command.organisationId());
        User user = User.register(organisationId, email, hashedPassword, profile);
        repository.save(user);
        publisher.publish(user.pullDomainEvents());
        return user.id();
    }
}
