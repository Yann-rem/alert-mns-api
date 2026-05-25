package com.alertmns.identity.infrastructure.adapter.incoming.web.auth;

import com.alertmns.identity.domain.model.HashedToken;
import com.alertmns.identity.domain.model.RawToken;
import com.alertmns.identity.domain.model.UserStatus;
import com.alertmns.identity.domain.port.incoming.IssueActivationTokenUseCase;
import com.alertmns.identity.domain.port.incoming.command.IssueActivationTokenCommand;
import com.alertmns.identity.domain.port.outgoing.MailerPort;
import com.alertmns.identity.infrastructure.adapter.outgoing.persistence.ActivationTokenJpaEntity;
import com.alertmns.identity.infrastructure.adapter.outgoing.persistence.ActivationTokenJpaRepository;
import com.alertmns.identity.infrastructure.adapter.outgoing.persistence.UserJpaEntity;
import com.alertmns.identity.infrastructure.adapter.outgoing.persistence.UserJpaRepository;
import com.alertmns.organisation.domain.model.MemberStatus;
import com.alertmns.organisation.domain.port.incoming.InviteMemberUseCase;
import com.alertmns.organisation.domain.port.incoming.command.InviteMemberCommand;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.MemberJpaEntity;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.MemberJpaRepository;
import com.alertmns.shared.UserId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@DisplayName("Magic-link activation flow")
class MagicLinkActivationIntegrationTest extends AbstractAuthIntegrationTest {

    private static final String EMAIL = "magic.link@alertmns.local";
    private static final String DUMMY_PASSWORD = "dummypass1234";
    private static final String CHOSEN_PASSWORD = "chosenpass5678";
    private static final String UNKNOWN_RAW_TOKEN = "this-raw-token-was-never-issued";
    private static final String DEFAULT_ORGANISATION_ID = "00000000-0000-0000-0000-000000000001";

    @MockitoBean
    private MailerPort mailer;

    @Autowired
    private IssueActivationTokenUseCase issueUseCase;

    @Autowired
    private InviteMemberUseCase inviteMemberUseCase;

    @Autowired
    private ActivationTokenJpaRepository activationTokenJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    /**
     * Nettoie les artefacts spécifiques à ce test (tokens). Les Members sont nettoyés par le parent
     * {@link AbstractAuthIntegrationTest#cleanDatabase()} depuis D14.
     */
    @AfterEach
    void cleanActivationTokens() {
        activationTokenJpaRepository.deleteAll();
    }

    // --- /validate ---

    @Test
    @DisplayName("GET /validate with valid token returns 200 and user context")
    void shouldReturnUserContextWhenTokenIsValid() {
        PendingUser pending = registerPendingAndCaptureRawToken(EMAIL, DUMMY_PASSWORD);

        ResponseEntity<String> response = validate(pending.rawToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .contains("\"email\":\"" + EMAIL + "\"")
                .contains("\"firstName\":\"Test\"")
                .contains("\"lastName\":\"User\"");
    }

    @Test
    @DisplayName("GET /validate with unknown token returns 410 Gone")
    void shouldReturn410WhenValidateTokenIsUnknown() {
        ResponseEntity<String> response = validate(UNKNOWN_RAW_TOKEN);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GONE);
        assertThat(response.getBody()).contains("Activation link is no longer valid");
    }

    @Test
    @DisplayName("GET /validate with expired token returns 410 Gone")
    void shouldReturn410WhenValidateTokenIsExpired() {
        UserId userId = userFactory.registerPending(EMAIL, DUMMY_PASSWORD);
        String rawToken = "expired-raw-token-value";
        insertExpiredTokenFor(userId, rawToken);

        ResponseEntity<String> response = validate(rawToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GONE);
        assertThat(response.getBody()).contains("Activation link is no longer valid");
    }

    @Test
    @DisplayName("GET /validate without token query param returns 400 Bad Request")
    void shouldReturn400WhenValidateTokenIsMissing() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/auth/magic-link/validate", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("GET /validate is reachable anonymously (no session, no CSRF)")
    void shouldAllowAnonymousAccessToValidate() {
        PendingUser pending = registerPendingAndCaptureRawToken(EMAIL, DUMMY_PASSWORD);

        // No cookies, no headers — fully anonymous client
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/auth/magic-link/validate?token={token}", String.class, pending.rawToken());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // --- /redeem ---

    @Test
    @DisplayName("POST /redeem with valid token activates the user and cascades to Member")
    void shouldActivateUserWhenRedeemingValidToken() {
        PendingUser pending = registerPendingAndCaptureRawToken(EMAIL, DUMMY_PASSWORD);
        inviteMemberFor(pending.userId());

        ResponseEntity<String> response = redeem(pending.rawToken(), CHOSEN_PASSWORD);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        UserJpaEntity savedUser = userJpaRepository.findById(pending.userId().value()).orElseThrow();
        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(activationTokenJpaRepository.findAll()).isEmpty();
        // Cascade UserActivated → ActivateMember : le Member rattaché passe aussi en ACTIVE
        MemberJpaEntity member = memberJpaRepository.findByUserId(pending.userId().value()).orElseThrow();
        assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
    }

    @Test
    @DisplayName("POST /redeem with unknown token returns 410 Gone")
    void shouldReturn410WhenRedeemTokenIsUnknown() {
        ResponseEntity<String> response = redeem(UNKNOWN_RAW_TOKEN, CHOSEN_PASSWORD);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GONE);
        assertThat(response.getBody()).contains("Activation link is no longer valid");
    }

    @Test
    @DisplayName("POST /redeem with expired token returns 410 Gone")
    void shouldReturn410WhenRedeemTokenIsExpired() {
        UserId userId = userFactory.registerPending(EMAIL, DUMMY_PASSWORD);
        String rawToken = "expired-raw-token-value";
        insertExpiredTokenFor(userId, rawToken);

        ResponseEntity<String> response = redeem(rawToken, CHOSEN_PASSWORD);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GONE);
        assertThat(response.getBody()).contains("Activation link is no longer valid");
        // User stayed PENDING
        UserJpaEntity user = userJpaRepository.findById(userId.value()).orElseThrow();
        assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING);
    }

    @Test
    @DisplayName("POST /redeem is reachable anonymously without CSRF token")
    void shouldAllowAnonymousRedeemWithoutCsrf() {
        PendingUser pending = registerPendingAndCaptureRawToken(EMAIL, DUMMY_PASSWORD);
        inviteMemberFor(pending.userId());

        // No cookies, no X-XSRF-TOKEN header — atteste l'exemption CSRF
        ResponseEntity<String> response = redeem(pending.rawToken(), CHOSEN_PASSWORD);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    // --- Re-issue ---

    @Test
    @DisplayName("Re-issuing invalidates the previous token")
    void shouldInvalidatePreviousTokenWhenReissued() {
        PendingUser pending = registerPendingAndCaptureRawToken(EMAIL, DUMMY_PASSWORD);
        String firstRawToken = pending.rawToken();
        String secondRawToken = issueAdditionalTokenAndCaptureRawToken(pending.userId());

        assertThat(firstRawToken).isNotEqualTo(secondRawToken);

        ResponseEntity<String> firstValidate = validate(firstRawToken);
        assertThat(firstValidate.getStatusCode()).isEqualTo(HttpStatus.GONE);

        ResponseEntity<String> secondValidate = validate(secondRawToken);
        assertThat(secondValidate.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // --- End-to-end ---

    @Test
    @DisplayName("End-to-end: register (auto-issue) → invite Member → validate → redeem → cascade → login")
    void endToEndActivationFlow() {
        PendingUser pending = registerPendingAndCaptureRawToken(EMAIL, DUMMY_PASSWORD);
        inviteMemberFor(pending.userId());

        ResponseEntity<String> validateResponse = validate(pending.rawToken());
        assertThat(validateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> redeemResponse = redeem(pending.rawToken(), CHOSEN_PASSWORD);
        assertThat(redeemResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Token deleted, user ACTIVE
        assertThat(activationTokenJpaRepository.findAll()).isEmpty();
        UserJpaEntity activated = userJpaRepository.findById(pending.userId().value()).orElseThrow();
        assertThat(activated.getStatus()).isEqualTo(UserStatus.ACTIVE);

        // Cascade UserActivated → ActivateMember : Member ACTIVE également
        MemberJpaEntity member = memberJpaRepository.findByUserId(pending.userId().value()).orElseThrow();
        assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);

        // The user can now log in with the password they chose at redeem
        ResponseEntity<String> loginResponse = login(EMAIL, CHOSEN_PASSWORD);
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // And NOT with the throwaway password used at registration
        ResponseEntity<String> badLogin = login(EMAIL, DUMMY_PASSWORD);
        assertThat(badLogin.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // --- Helpers ---

    private ResponseEntity<String> validate(String rawToken) {
        return restTemplate.getForEntity(
                "/api/auth/magic-link/validate?token={token}", String.class, rawToken);
    }

    private ResponseEntity<String> redeem(String rawToken, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"token\":\"" + rawToken + "\",\"password\":\"" + password + "\"}";
        return restTemplate.postForEntity(
                "/api/auth/magic-link/redeem", new HttpEntity<>(body, headers), String.class);
    }

    /**
     * Crée un utilisateur PENDING et capture le raw token émis automatiquement via la cascade
     * {@code UserRegistered → IssueActivationToken}. C'est le chemin nominal d'activation : un seul appel suffit pour
     * obtenir un user et son magic-link.
     */
    private PendingUser registerPendingAndCaptureRawToken(String email, String password) {
        Mockito.clearInvocations(mailer);
        UserId userId = userFactory.registerPending(email, password);

        ArgumentCaptor<URI> linkCaptor = ArgumentCaptor.forClass(URI.class);
        verify(mailer).sendActivationEmail(any(), any(), linkCaptor.capture());

        return new PendingUser(userId, extractRawToken(linkCaptor.getValue()));
    }

    /**
     * Émet un token d'activation supplémentaire pour un user existant et capture le raw token. Utilisé uniquement par
     * le test de ré-émission, qui valide qu'un second issue invalide le premier token (cascade ou pas).
     */
    private String issueAdditionalTokenAndCaptureRawToken(UserId userId) {
        Mockito.clearInvocations(mailer);
        issueUseCase.issue(new IssueActivationTokenCommand(userId.value().toString()));

        ArgumentCaptor<URI> linkCaptor = ArgumentCaptor.forClass(URI.class);
        verify(mailer).sendActivationEmail(any(), any(), linkCaptor.capture());

        return extractRawToken(linkCaptor.getValue());
    }

    private static String extractRawToken(URI link) {
        String query = link.getQuery();
        return query.substring("token=".length());
    }

    /**
     * Invite un Member PENDING rattaché à un user existant. Indispensable avant tout redeem qui activera le user :
     * la cascade {@code UserActivated → ActivateMember} suppose qu'un Member rattaché existe en BD.
     */
    private void inviteMemberFor(UserId userId) {
        inviteMemberUseCase.invite(new InviteMemberCommand(
                DEFAULT_ORGANISATION_ID,
                userId.value().toString(),
                "MEMBER"
        ));
    }

    /**
     * Insère directement en BD un token déjà expiré pour l'utilisateur cible. Permet de tester l'expiration sans
     * attendre le TTL. À remplacer par un {@code Clock} injectable quand la dette D8 sera traitée.
     */
    private void insertExpiredTokenFor(UserId userId, String rawTokenValue) {
        HashedToken hash = HashedToken.of(RawToken.of(rawTokenValue));
        Instant past = Instant.now().minus(Duration.ofHours(49));
        Instant expiredOneHourAgo = Instant.now().minus(Duration.ofHours(1));
        ActivationTokenJpaEntity entity = new ActivationTokenJpaEntity(
                UUID.randomUUID(),
                userId.value(),
                hash.hex(),
                past,
                expiredOneHourAgo
        );
        activationTokenJpaRepository.save(entity);
    }

    private record PendingUser(UserId userId, String rawToken) {
    }
}
