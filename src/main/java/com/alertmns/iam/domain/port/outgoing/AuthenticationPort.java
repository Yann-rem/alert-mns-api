package com.alertmns.iam.domain.port.outgoing;

/**
 * Port sortant pour le hachage du mot de passe.
 * Implémenté par Spring Security dans l'infrastructure.
 */
public interface AuthenticationPort {

    String hashPassword(String rawPassword);
}
