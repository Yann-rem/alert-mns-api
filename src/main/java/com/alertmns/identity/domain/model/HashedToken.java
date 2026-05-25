package com.alertmns.identity.domain.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

/**
 * Value Object représentant le hash SHA-256 d'un {@link RawToken}.
 *
 * <p>Seul le hash est persisté en base : le raw token est volontairement jeté après envoi à l'utilisateur pour qu'une
 * fuite de la table d'activation ne permette pas à un attaquant de rejouer les liens.</p>
 */
public record HashedToken(String hex) {

    private static final int SHA256_HEX_LENGTH = 64;
    private static final String HASH_ALGORITHM = "SHA-256";

    public HashedToken {
        Objects.requireNonNull(hex, "hashedToken must not be null");
        if (hex.length() != SHA256_HEX_LENGTH) {
            throw new IllegalArgumentException("hashedToken must be exactly " + SHA256_HEX_LENGTH + " characters");
        }
    }

    /**
     * Calcule le hash SHA-256 d'un raw token.
     *
     * @param raw le token brut à hacher
     * @return le hash, encodé en hex (64 caractères)
     * @throws IllegalStateException si l'algorithme SHA-256 n'est pas disponible sur la JVM
     */
    public static HashedToken of(RawToken raw) {
        Objects.requireNonNull(raw, "rawToken must not be null");
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] bytes = digest.digest(raw.value().getBytes(StandardCharsets.UTF_8));
            return new HashedToken(HexFormat.of().formatHex(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("hash algorithm " + HASH_ALGORITHM + " must be available on every JVM", e);
        }
    }

    /**
     * Crée un hash SHA-256 à partir d'une valeur hex déjà calculée (typiquement lue depuis la persistence).
     *
     * @param hex la valeur hex (64 caractères) du hash
     * @return le hash validé
     * @throws IllegalArgumentException si la valeur ne fait pas exactement 64 caractères
     */
    public static HashedToken of(String hex) {
        return new HashedToken(hex);
    }
}
