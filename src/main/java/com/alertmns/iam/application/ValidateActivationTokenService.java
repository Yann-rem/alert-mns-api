package com.alertmns.iam.application;

import com.alertmns.iam.domain.exception.ActivationTokenExpiredException;
import com.alertmns.iam.domain.exception.ActivationTokenNotFoundException;
import com.alertmns.iam.domain.exception.UserNotFoundException;
import com.alertmns.iam.domain.model.ActivationToken;
import com.alertmns.iam.domain.model.RawToken;
import com.alertmns.iam.domain.model.TokenHash;
import com.alertmns.iam.domain.model.User;
import com.alertmns.iam.domain.port.incoming.ValidateActivationTokenUseCase;
import com.alertmns.iam.domain.port.incoming.query.ValidateActivationTokenQuery;
import com.alertmns.iam.domain.port.incoming.result.ActivationTokenContext;
import com.alertmns.iam.domain.port.outgoing.ActivationTokenRepository;
import com.alertmns.iam.domain.port.outgoing.UserRepository;

import java.util.Objects;

/**
 * Service applicatif validant un token d'activation et retournant le contexte utilisateur.
 *
 * <p>Parse (VO raw + hash) → load (token) → verify (non expiré) → load (user) → return (DTO).</p>
 *
 * <p>Lecture pure : aucune mutation, pas d'événement publié. Le statut de l'utilisateur n'est pas vérifié — la garde
 * est portée par l'agrégat au moment du redeem (voir D10).</p>
 */
public final class ValidateActivationTokenService implements ValidateActivationTokenUseCase {

    private final ActivationTokenRepository tokenRepository;
    private final UserRepository userRepository;

    public ValidateActivationTokenService(ActivationTokenRepository tokenRepository, UserRepository userRepository) {
        this.tokenRepository = Objects.requireNonNull(tokenRepository, "tokenRepository must not be null");
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository must not be null");
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
        TokenHash hash = TokenHash.of(raw);

        ActivationToken token = tokenRepository.findByTokenHash(hash).orElseThrow(
                () -> new ActivationTokenNotFoundException(hash));
        token.verifyUsable();

        User user = userRepository.findById(token.userId()).orElseThrow(
                () -> new UserNotFoundException(token.userId()));

        return new ActivationTokenContext(user.email(), user.profile().firstName(), user.profile().lastName());
    }
}
