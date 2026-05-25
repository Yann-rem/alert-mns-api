package com.alertmns.identity.infrastructure.adapter.outgoing.mailer;

import com.alertmns.identity.domain.model.Email;
import com.alertmns.identity.domain.model.FirstName;
import com.alertmns.identity.domain.port.outgoing.MailerPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * Adapter de stub pour le {@link MailerPort} : log le contenu du mail au lieu de l'envoyer réellement.
 *
 * <p>Temporaire, utile en dev pour copier-coller le lien magique depuis les logs. À remplacer par un
 * {@code SmtpMailerAdapter} (ou équivalent) avant l'ouverture publique.</p>
 */
public final class LoggingMailerAdapter implements MailerPort {

    private static final Logger log = LoggerFactory.getLogger(LoggingMailerAdapter.class);

    @Override
    public void sendActivationEmail(Email recipient, FirstName firstName, URI activationLink) {
        log.info("Activation email to {} (firstName={}): {}", recipient.value(), firstName.value(), activationLink);
    }
}
