package com.alertmns.iam.domain.model;

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
public record TokenHash(String hex) {

    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int SHA256_HEX_LENGTH = 64;

    public TokenHash {
        Objects.requireNonNull(hex, "tokenHash must not be null");
        if (hex.length() != SHA256_HEX_LENGTH) {
            throw new IllegalArgumentException("tokenHash must be exactly " + SHA256_HEX_LENGTH + " characters");
        }
    }

    /**
     * Calcule le hash SHA-256 d'un raw token.
     *
     * @param raw le token brut à hacher
     * @return le hash, encodé en hex (64 caractères)
     * @throws IllegalStateException si l'algorithme SHA-256 n'est pas disponible sur la JVM
     */
    public static TokenHash of(RawToken raw) {
        Objects.requireNonNull(raw, "rawToken must not be null");
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] bytes = digest.digest(raw.value().getBytes(StandardCharsets.UTF_8));
            return new TokenHash(HexFormat.of().formatHex(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("hash algorithm " + HASH_ALGORITHM + " must be available on every JVM", e);
        }
    }
}
