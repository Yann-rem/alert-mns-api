package com.alertmns.iam.application;

import com.alertmns.iam.domain.exception.UserNotFoundException;
import com.alertmns.iam.domain.model.AbsenceMessage;
import com.alertmns.iam.domain.model.User;
import com.alertmns.iam.domain.model.UserId;
import com.alertmns.iam.domain.port.incoming.UpdateAbsenceMessageUseCase;
import com.alertmns.iam.domain.port.incoming.command.UpdateAbsenceMessageCommand;
import com.alertmns.iam.domain.port.outgoing.EventPublisher;
import com.alertmns.iam.domain.port.outgoing.UserRepository;

import java.util.Objects;

/**
 * Service applicatif pour la mise à jour du message d'absence d'un utilisateur.
 * Orchestre le processus de mise à jour : chargement de l’agrégat, logique métier,
 * persistance et publication des événements du domaine.
 */
public class UpdateAbsenceMessageService implements UpdateAbsenceMessageUseCase {

    private final UserRepository userRepository;
    private final EventPublisher eventPublisher;

    public UpdateAbsenceMessageService(UserRepository userRepository, EventPublisher eventPublisher) {
        this.userRepository = Objects.requireNonNull(
                userRepository, "UserRepository ne peut pas être null"
        );

        this.eventPublisher = Objects.requireNonNull(
                eventPublisher, "EventPublisher ne peut pas être null"
        );
    }

    @Override
    public void updateAbsenceMessage(UpdateAbsenceMessageCommand command) {
        UserId id = UserId.from(command.userId());

        User foundUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        AbsenceMessage message = AbsenceMessage.of(command.content(), command.active());
        foundUser.updateAbsenceMessage(message);
        userRepository.save(foundUser);
        eventPublisher.publish(foundUser.pullDomainEvents());
    }
}
