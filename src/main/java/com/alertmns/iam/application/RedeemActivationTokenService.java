package com.alertmns.iam.application;

import com.alertmns.iam.domain.exception.ActivationTokenExpiredException;
import com.alertmns.iam.domain.exception.ActivationTokenNotFoundException;
import com.alertmns.iam.domain.exception.UserNotFoundException;
import com.alertmns.iam.domain.model.ActivationToken;
import com.alertmns.iam.domain.model.HashedPassword;
import com.alertmns.iam.domain.model.RawPassword;
import com.alertmns.iam.domain.model.RawToken;
import com.alertmns.iam.domain.model.TokenHash;
import com.alertmns.iam.domain.model.User;
import com.alertmns.iam.domain.port.incoming.RedeemActivationTokenUseCase;
import com.alertmns.iam.domain.port.incoming.command.RedeemActivationTokenCommand;
import com.alertmns.iam.domain.port.outgoing.ActivationTokenRepository;
import com.alertmns.iam.domain.port.outgoing.PasswordHasher;
import com.alertmns.iam.domain.port.outgoing.UserRepository;

import java.util.Objects;

/**
 * Service applicatif consommant un token d'activation et activant le compte utilisateur.
 *
 * <p>Parse (VO raw + hash) → load (token) → verify (non expiré) → load (user) → hash (password) → act
 * ({@code activateWithPassword}) → save → cleanup (suppression du token consommé).</p>
 *
 * <p>Le token est supprimé après usage : pas de soft-delete, pas de marquage {@code consumedAt}. Une seconde tentative
 * sur le même raw token aboutira à {@link ActivationTokenNotFoundException} (HTTP 410 côté adapter).</p>
 */
public final class RedeemActivationTokenService implements RedeemActivationTokenUseCase {

    private final ActivationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public RedeemActivationTokenService(
            ActivationTokenRepository tokenRepository,
            UserRepository userRepository,
            PasswordHasher passwordHasher
    ) {
        this.tokenRepository = Objects.requireNonNull(tokenRepository, "tokenRepository must not be null");
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository must not be null");
        this.passwordHasher = Objects.requireNonNull(passwordHasher, "passwordHasher must not be null");
    }

    /**
     * Consomme un token d'activation et finalise l'activation de l'utilisateur en définissant son mot de passe.
     *
     * @throws ActivationTokenNotFoundException si le hash dérivé du raw token ne correspond à aucun token en base
     * @throws ActivationTokenExpiredException  si le token a dépassé sa date d'expiration
     * @throws UserNotFoundException            si le token référence un utilisateur qui n'existe plus
     * @throws IllegalStateException            si le statut de l'utilisateur n'est pas {@code PENDING}
     */
    @Override
    public void redeem(RedeemActivationTokenCommand command) {
        RawToken raw = RawToken.of(command.rawToken());
        TokenHash hash = TokenHash.of(raw);
        RawPassword rawPassword = RawPassword.of(command.rawPassword());

        ActivationToken token = tokenRepository.findByTokenHash(hash).orElseThrow(
                () -> new ActivationTokenNotFoundException(hash));
        token.verifyUsable();

        User user = userRepository.findById(token.userId())
                .orElseThrow(() -> new UserNotFoundException(token.userId()));

        HashedPassword hashedPassword = passwordHasher.hash(rawPassword);
        user.activateWithPassword(hashedPassword);
        userRepository.save(user);
        tokenRepository.deleteByUserId(user.id());
    }
}
