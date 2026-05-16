package com.alertmns.iam.infrastructure.adapter.incoming.event;

import com.alertmns.iam.domain.event.UserRegistered;
import com.alertmns.iam.domain.port.incoming.IssueActivationTokenUseCase;
import com.alertmns.iam.domain.port.incoming.command.IssueActivationTokenCommand;
import org.springframework.context.event.EventListener;

import java.util.Objects;

/**
 * Listener qui déclenche l'émission d'un token d'activation à chaque inscription d'utilisateur.
 *
 * <p>Réagit à l'événement {@link UserRegistered} en appelant {@link IssueActivationTokenUseCase} pour l'utilisateur
 * fraîchement créé. L'invitation par un admin crée l'utilisateur en {@code PENDING}, et le lien magique est
 * automatiquement envoyé sans action supplémentaire côté admin.</p>
 *
 * <p>Synchrone : l'émission se fait dans la transaction du register. Si l'issue plante, l'exception propage et
 * l'admin devra réémettre manuellement.</p>
 */
public final class IssueActivationTokenOnUserRegisteredListener {

    private final IssueActivationTokenUseCase issueActivationTokenUseCase;

    public IssueActivationTokenOnUserRegisteredListener(IssueActivationTokenUseCase issueActivationTokenUseCase) {
        this.issueActivationTokenUseCase = Objects.requireNonNull(
                issueActivationTokenUseCase, "issueActivationTokenUseCase must not be null");
    }

    @EventListener
    public void onUserRegistered(UserRegistered event) {
        issueActivationTokenUseCase.issue(
                new IssueActivationTokenCommand(event.userId().value().toString())
        );
    }
}
