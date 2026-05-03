package com.alertmns.iam.application;

import com.alertmns.iam.domain.exception.UserNotFoundException;
import com.alertmns.iam.domain.model.AbsenceMessage;
import com.alertmns.iam.domain.model.User;
import com.alertmns.shared.UserId;
import com.alertmns.iam.domain.port.incoming.UpdateAbsenceMessageUseCase;
import com.alertmns.iam.domain.port.incoming.command.UpdateAbsenceMessageCommand;
import com.alertmns.iam.domain.port.outgoing.UserRepository;
import com.alertmns.shared.EventPublisher;

import java.util.Objects;

/**
 * Service applicatif représentant l'orchestration de la mise à jour du message d'absence d'un utilisateur.
 *
 * <p>Parse (VOs) → load (agrégat) → act (updateAbsenceMessage) → save → publish.</p>
 */
public final class UpdateAbsenceMessageService implements UpdateAbsenceMessageUseCase {

    private final UserRepository repository;
    private final EventPublisher publisher;

    public UpdateAbsenceMessageService(UserRepository repository, EventPublisher publisher) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.publisher = Objects.requireNonNull(publisher, "publisher must not be null");
    }

    @Override
    public void update(UpdateAbsenceMessageCommand command) {
        UserId id = UserId.from(command.userId());
        AbsenceMessage message = AbsenceMessage.of(command.content(), command.active());
        User user = repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        user.updateAbsenceMessage(message);
        repository.save(user);
        publisher.publish(user.pullDomainEvents());
    }
}
