package com.alertmns.shared.web.ws;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.WebSocketSession;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("WebSocketSessionRegistry")
@ExtendWith(MockitoExtension.class)
class WebSocketSessionRegistryTest {

    @Mock
    WebSocketSession session;

    WebSocketSessionRegistry registry = new WebSocketSessionRegistry();
    String userId = UUID.randomUUID().toString();

    @Test
    @DisplayName("closeSessionsOf closes a registered open session and forgets it")
    void shouldCloseRegisteredOpenSession() throws Exception {
        when(session.isOpen()).thenReturn(true);
        registry.register(userId, session);

        registry.closeSessionsOf(userId);

        verify(session).close(WebSocketSessionRegistry.SESSION_REVOKED);
        assertFalse(registry.hasSessions(userId));
    }

    @Test
    @DisplayName("closeSessionsOf does not close a session that is already closed")
    void shouldNotCloseAlreadyClosedSession() throws Exception {
        when(session.isOpen()).thenReturn(false);
        registry.register(userId, session);

        registry.closeSessionsOf(userId);

        verify(session, never()).close(any());
    }

    @Test
    @DisplayName("an unregistered session is no longer closed")
    void shouldNotCloseUnregisteredSession() throws Exception {
        registry.register(userId, session);
        registry.unregister(userId, session);

        registry.closeSessionsOf(userId);

        verify(session, never()).close(any());
        assertFalse(registry.hasSessions(userId));
    }

    @Test
    @DisplayName("closeSessionsOf on an unknown user is a no-op")
    void shouldIgnoreUnknownUser() {
        registry.closeSessionsOf("nobody");

        verifyNoInteractions(session);
    }
}
