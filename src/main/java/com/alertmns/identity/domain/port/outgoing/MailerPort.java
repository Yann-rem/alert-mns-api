package com.alertmns.identity.domain.port.outgoing;

import com.alertmns.shared.Email;
import com.alertmns.identity.domain.model.FirstName;

import java.net.URI;

/**
 * Port sortant pour l'envoi de courriels transactionnels liés à l'identité.
 */
public interface MailerPort {

    /**
     * Envoie un e-mail d'activation contenant le lien magique à usage unique.
     *
     * @param recipient      l'adresse e-mail du destinataire
     * @param firstName      le prénom du destinataire (pour personnaliser le contenu)
     * @param activationLink le lien magique complet à inclure dans le corps du mail
     */
    void sendActivationEmail(Email recipient, FirstName firstName, URI activationLink);
}
