package com.alertmns.identity.application;

import com.alertmns.identity.domain.exception.UserNotFoundException;
import com.alertmns.identity.domain.model.ActivationToken;
import com.alertmns.identity.domain.model.RawToken;
import com.alertmns.identity.domain.model.User;
import com.alertmns.identity.domain.port.incoming.IssueActivationTokenUseCase;
import com.alertmns.identity.domain.port.incoming.command.IssueActivationTokenCommand;
import com.alertmns.identity.domain.port.outgoing.ActivationTokenRepository;
import com.alertmns.identity.domain.port.outgoing.MailerPort;
import com.alertmns.identity.domain.port.outgoing.UserRepository;
import com.alertmns.shared.UserId;

import java.net.URI;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.Objects;

/**
 * Service applicatif émettant un token d'activation et envoyant le lien magique par e-mail.
 *
 * <p>Parse (VO id) → load (agrégat) → generate (raw token + ActivationToken) → replace (suppression des éventuels
 * tokens en cours pour ce user) → save → notify (mail).</p>
 *
 * <p>Le raw token n'est jamais persisté : seul son hash l'est. Réémettre supprime tout token précédent pour le même
 * user (idempotence côté ré-envoi).</p>
 */
public final class IssueActivationTokenService implements IssueActivationTokenUseCase {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;

    private final ActivationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final MailerPort mailer;
    private final Duration ttl;
    private final Clock clock;
    private final URI frontendBaseUrl;

    public IssueActivationTokenService(
            ActivationTokenRepository tokenRepository,
            UserRepository userRepository,
            MailerPort mailer,
            Duration ttl,
            Clock clock,
            URI frontendBaseUrl
    ) {
        this.tokenRepository = Objects.requireNonNull(tokenRepository, "tokenRepository must not be null");
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository must not be null");
        this.mailer = Objects.requireNonNull(mailer, "mailer must not be null");
        this.ttl = Objects.requireNonNull(ttl, "ttl must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        this.frontendBaseUrl = Objects.requireNonNull(frontendBaseUrl, "frontendBaseUrl must not be null");
    }

    /**
     * Émet un token d'activation à usage unique pour un utilisateur en {@code PENDING}.
     *
     * <p>Aucune vérification de statut n'est faite à l'émission : si l'utilisateur est déjà {@code ACTIVE}, le redeem
     * échouera proprement avec une exception métier.</p>
     *
     * @throws UserNotFoundException si l'utilisateur n'existe pas
     */
    @Override
    public void issue(IssueActivationTokenCommand command) {
        UserId id = UserId.from(command.userId());
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        RawToken raw = generateRawToken();
        ActivationToken.IssuedToken issued = ActivationToken.issue(id, raw, ttl, clock.instant());

        tokenRepository.deleteByUserId(id);
        tokenRepository.save(issued.activationToken());

        URI link = frontendBaseUrl.resolve("/activation?token=" + raw.value());
        mailer.sendActivationEmail(user.email(), user.profile().firstName(), link);
    }

    private static RawToken generateRawToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return RawToken.of(Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
    }
}
