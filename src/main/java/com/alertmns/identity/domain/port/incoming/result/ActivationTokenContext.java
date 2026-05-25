package com.alertmns.identity.domain.port.incoming.result;

import com.alertmns.shared.Email;
import com.alertmns.identity.domain.model.FirstName;
import com.alertmns.identity.domain.model.LastName;

import java.util.Objects;

/**
 * Résultat retourné par la validation d'un token d'activation.
 *
 * <p>Porte les informations nécessaires à l'affichage de la page d'activation côté front : adresse e-mail à confirmer
 * et identité pour personnaliser le contenu.</p>
 *
 * <p>Le mot de passe n'est jamais exposé. L'identifiant utilisateur n'est pas exposé non plus (le front n'a pas besoin
 * de le connaître ; le redeem utilise le raw token comme clé).</p>
 */
public record ActivationTokenContext(Email email, FirstName firstName, LastName lastName) {

    public ActivationTokenContext {
        Objects.requireNonNull(email, "email must not be null");
        Objects.requireNonNull(firstName, "firstName must not be null");
        Objects.requireNonNull(lastName, "lastName must not be null");
    }
}
