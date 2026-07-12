package com.alertmns.identity.infrastructure.adapter.incoming.event;

import com.alertmns.identity.domain.event.UserAnonymized;
import com.alertmns.identity.domain.event.UserSuspended;
import com.alertmns.identity.domain.model.FirstName;
import com.alertmns.identity.domain.model.HashedPassword;
import com.alertmns.identity.domain.model.LastName;
import com.alertmns.identity.domain.model.Profile;
import com.alertmns.identity.domain.model.User;
import com.alertmns.identity.domain.model.UserStatus;
import com.alertmns.identity.infrastructure.adapter.incoming.web.security.DomainUserDetails;
import com.alertmns.shared.Email;
import com.alertmns.shared.UserId;
import com.alertmns.shared.web.ws.WebSocketSessionRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@DisplayName("WebSocketRevocationListener")
@ExtendWith(MockitoExtension.class)
class WebSocketRevocationListenerTest {

    static final String BCRYPT_HASH = "$2a$10$abcdefghijklmnopqrstuuABCDEFGHIJKLMNOPQRSTUVWXYZ012345";
    static final Instant NOW = Instant.parse("2026-05-30T10:00:00Z");

    @Mock
    WebSocketSessionRegistry sessionRegistry;

    @InjectMocks
    WebSocketRevocationListener listener;

    private User activeUser(UserId id) {
        return User.reconstitute(
                id, Email.of("jane@example.com"), HashedPassword.of(BCRYPT_HASH),
                Profile.of(FirstName.of("Jane"), LastName.of("Doe")), UserStatus.ACTIVE, false, NOW);
    }

    @Test
    @DisplayName("closes sockets on UserSuspended")
    void shouldCloseOnUserSuspended() {
        UserId userId = UserId.generate();

        listener.onUserSuspended(new UserSuspended(userId, NOW));

        verify(sessionRegistry).closeSessionsOf(userId.value().toString());
    }

    @Test
    @DisplayName("closes sockets on UserAnonymized")
    void shouldCloseOnUserAnonymized() {
        UserId userId = UserId.generate();

        listener.onUserAnonymized(new UserAnonymized(userId, NOW));

        verify(sessionRegistry).closeSessionsOf(userId.value().toString());
    }

    @Test
    @DisplayName("closes sockets for each authenticated user of a destroyed HTTP session")
    void shouldCloseOnSessionDestroyed() {
        UserId userId = UserId.generate();
        DomainUserDetails details = new DomainUserDetails(activeUser(userId), List.of());
        SecurityContext context = new SecurityContextImpl(
                new UsernamePasswordAuthenticationToken(details, null, List.of()));

        listener.revoke(List.of(context));

        verify(sessionRegistry).closeSessionsOf(userId.value().toString());
    }

    @Test
    @DisplayName("ignores a destroyed session context with no authentication")
    void shouldIgnoreContextWithoutAuthentication() {
        listener.revoke(List.of(new SecurityContextImpl()));

        verify(sessionRegistry, never()).closeSessionsOf(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("should reject null registry")
    void shouldRejectNullRegistry() {
        assertThrows(NullPointerException.class, () -> new WebSocketRevocationListener(null));
    }
}
