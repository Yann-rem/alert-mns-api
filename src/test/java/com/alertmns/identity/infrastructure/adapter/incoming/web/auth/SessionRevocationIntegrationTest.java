package com.alertmns.identity.infrastructure.adapter.incoming.web.auth;

import com.alertmns.identity.application.AnonymizeUserService;
import com.alertmns.identity.application.SuspendUserService;
import com.alertmns.identity.domain.port.incoming.command.AnonymizeUserCommand;
import com.alertmns.identity.domain.port.incoming.command.SuspendUserCommand;
import com.alertmns.shared.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration de la révocation des sessions déjà ouvertes.
 *
 * <p>Le défaut couvert ici a été trouvé par le jeu d'essai, pas par les tests unitaires, et
 * l'explication tient à ce que ces tests vérifiaient : {@code DomainUserDetails} exposait
 * correctement le statut du compte, et le service de suspension écrivait correctement en base. Les
 * deux couches étaient justes ; c'est leur articulation qui manquait, Spring Security ne consultant
 * le statut qu'à l'authentification.</p>
 *
 * <p>D'où le choix d'un test d'intégration plutôt qu'unitaire : seul un aller-retour HTTP complet,
 * avec une session réellement ouverte, peut constater le défaut. Un test du filtre isolé aurait
 * passé sans rien prouver de son câblage dans la chaîne.</p>
 */
@DisplayName("Révocation des sessions ouvertes")
class SessionRevocationIntegrationTest extends AbstractAuthIntegrationTest {

    private static final String EMAIL = "dave@alertmns.local";
    private static final String PASSWORD = "secret123456";

    @Autowired
    private SuspendUserService suspendUserService;

    @Autowired
    private AnonymizeUserService anonymizeUserService;

    @Test
    @DisplayName("An open session stays valid while the account is active")
    void shouldKeepSessionValidWhileAccountIsActive() {
        userFactory.registerActive(EMAIL, PASSWORD);
        AuthCookies cookies = loginAndAcquireCookies(EMAIL, PASSWORD);

        // Contrôle négatif : sans lui, un filtre qui refuserait tout ferait passer les deux
        // autres tests pour la mauvaise raison.
        assertThat(getMe(cookies.session()).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getMe(cookies.session()).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Suspending a user closes the session already opened")
    void shouldCloseOpenSessionWhenUserIsSuspended() {
        UserId userId = userFactory.registerActive(EMAIL, PASSWORD);
        AuthCookies cookies = loginAndAcquireCookies(EMAIL, PASSWORD);
        assertThat(getMe(cookies.session()).getStatusCode()).isEqualTo(HttpStatus.OK);

        suspendUserService.suspend(new SuspendUserCommand(userId.value().toString()));

        ResponseEntity<String> afterSuspension = getMe(cookies.session());
        assertThat(afterSuspension.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Anonymising a user closes the session already opened")
    void shouldCloseOpenSessionWhenUserIsAnonymised() {
        UserId userId = userFactory.registerActive(EMAIL, PASSWORD);
        AuthCookies cookies = loginAndAcquireCookies(EMAIL, PASSWORD);
        assertThat(getMe(cookies.session()).getStatusCode()).isEqualTo(HttpStatus.OK);

        anonymizeUserService.anonymize(new AnonymizeUserCommand(userId.value().toString()));

        ResponseEntity<String> afterAnonymisation = getMe(cookies.session());
        assertThat(afterAnonymisation.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
