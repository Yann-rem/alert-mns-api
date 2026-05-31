package com.alertmns.identity.application;

import com.alertmns.identity.domain.exception.ActivationTokenExpiredException;
import com.alertmns.identity.domain.exception.ActivationTokenNotFoundException;
import com.alertmns.identity.domain.exception.UserNotFoundException;
import com.alertmns.identity.domain.model.ActivationToken;
import com.alertmns.identity.domain.model.HashedToken;
import com.alertmns.identity.domain.model.RawToken;
import com.alertmns.identity.domain.model.User;
import com.alertmns.identity.domain.port.incoming.ValidateActivationTokenUseCase;
import com.alertmns.identity.domain.port.incoming.query.ValidateActivationTokenQuery;
import com.alertmns.identity.domain.port.incoming.result.ActivationTokenContext;
import com.alertmns.identity.domain.port.outgoing.ActivationTokenRepository;
import com.alertmns.identity.domain.port.outgoing.UserRepository;

import java.time.Clock;
import java.util.Objects;

/**
 * Service applicatif validant un token d'activation et retournant le contexte utilisateur.
 *
 * <p>Parse (VO raw + hash) → load (token) → verify (non expiré) → load (user) → return (DTO).</p>
 */
public final class ValidateActivationTokenService implements ValidateActivationTokenUseCase {

    private final ActivationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public ValidateActivationTokenService(
            ActivationTokenRepository tokenRepository,
            UserRepository userRepository,
            Clock clock
    ) {
        this.tokenRepository = Objects.requireNonNull(tokenRepository, "tokenRepository must not be null");
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    /**
     * Valide un token et retourne le contexte utilisateur.
     *
     * @throws ActivationTokenNotFoundException si le hash dérivé du raw token ne correspond à aucun token en base
     * @throws ActivationTokenExpiredException  si le token a dépassé sa date d'expiration
     * @throws UserNotFoundException            si le token référence un utilisateur qui n'existe plus
     */
    @Override
    public ActivationTokenContext validate(ValidateActivationTokenQuery query) {
        RawToken raw = RawToken.of(query.rawToken());
        HashedToken hash = HashedToken.of(raw);

        ActivationToken token = tokenRepository.findByHash(hash).orElseThrow(
                () -> new ActivationTokenNotFoundException(hash));
        token.verifyUsable(clock.instant());

        User user = userRepository.findById(token.userId()).orElseThrow(
                () -> new UserNotFoundException(token.userId()));

        return new ActivationTokenContext(user.email(), user.profile().firstName(), user.profile().lastName());
    }
}
