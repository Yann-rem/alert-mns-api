package com.alertmns.iam.domain.port.outgoing;

/**
 * Port sortant pour le hachage du mot de passe.
 *
 * <p>Implémenté par Spring Security dans l'infrastructure.</p>
 */
public interface PasswordHasher {

    /**
     * Hache un mot de passe brut.
     *
     * @param rawPassword le mot de passe en clair
     * @return le hash du mot de passe
     */
    String hash(String rawPassword);
}
