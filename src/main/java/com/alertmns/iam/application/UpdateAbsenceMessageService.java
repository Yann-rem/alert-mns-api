package com.alertmns.iam.application;

import com.alertmns.iam.domain.exception.UserNotFoundException;
import com.alertmns.iam.domain.model.AbsenceMessage;
import com.alertmns.iam.domain.model.User;
import com.alertmns.iam.domain.model.UserId;
import com.alertmns.iam.domain.port.incoming.UpdateAbsenceMessageUseCase;
import com.alertmns.iam.domain.port.incoming.command.UpdateAbsenceMessageCommand;
import com.alertmns.shared.EventPublisher;
import com.alertmns.iam.domain.port.outgoing.UserRepository;

import java.util.Objects;

/**
 * Service applicatif représentant l’orchestration de la mise à jour du message d’absence d’un utilisateur.
 *
 * <p>Charge l’agrégat → met à jour le message d’absence → persiste → publie les événements.</p>
 */
public final class UpdateAbsenceMessageService implements UpdateAbsenceMessageUseCase {

    private final UserRepository repository;
    private final EventPublisher publisher;

    public UpdateAbsenceMessageService(UserRepository repository, EventPublisher publisher) {
        this.repository = Objects.requireNonNull(
                repository, "repository must not be null"
        );

        this.publisher = Objects.requireNonNull(
                publisher, "publisher must not be null"
        );
    }

    @Override
    public void update(UpdateAbsenceMessageCommand command) {
        UserId id = UserId.from(command.userId());
        User user = repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        AbsenceMessage message = AbsenceMessage.of(command.content(), command.active());
        user.updateAbsenceMessage(message);
        repository.save(user);
        publisher.publish(user.pullDomainEvents());
    }
}
