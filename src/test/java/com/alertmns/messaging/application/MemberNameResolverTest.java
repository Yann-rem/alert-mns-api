package com.alertmns.messaging.application;

import com.alertmns.messaging.domain.port.outgoing.MemberDirectoryPort;
import com.alertmns.messaging.domain.port.outgoing.UserDirectoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("MemberNameResolver")
@ExtendWith(MockitoExtension.class)
class MemberNameResolverTest {

    @Mock
    MemberDirectoryPort memberDirectory;

    @Mock
    UserDirectoryPort userDirectory;

    @InjectMocks
    MemberNameResolver resolver;

    @Nested
    @DisplayName("Single resolution")
    class Single {

        @Test
        @DisplayName("chains member → user → display name")
        void shouldChainMemberToUserToName() {
            UUID memberId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            when(memberDirectory.userIdOf(memberId)).thenReturn(Optional.of(userId));
            when(userDirectory.displayName(userId)).thenReturn("Sofia Nkolo");

            assertEquals("Sofia Nkolo", resolver.nameOf(memberId));
        }

        /** Référence orpheline (ADR-0017 §4) : le membre n'a plus d'utilisateur. */
        @Test
        @DisplayName("falls back to the placeholder when the member has no user")
        void shouldFallBackForOrphanMember() {
            UUID memberId = UUID.randomUUID();
            when(memberDirectory.userIdOf(memberId)).thenReturn(Optional.empty());

            assertEquals(UserDirectoryPort.DELETED_USER_DISPLAY_NAME, resolver.nameOf(memberId));
            verify(userDirectory, never()).displayName(any());
        }

        @Test
        @DisplayName("falls back to the placeholder for a null member, without touching the ports")
        void shouldFallBackForNullMember() {
            assertEquals(UserDirectoryPort.DELETED_USER_DISPLAY_NAME, resolver.nameOf(null));
            verify(memberDirectory, never()).userIdOf(any());
        }
    }

    @Nested
    @DisplayName("Bulk resolution")
    class Bulk {

        /** Un fil de discussion répète les mêmes auteurs : chacun ne doit être résolu qu'une fois. */
        @Test
        @DisplayName("resolves each distinct member only once")
        void shouldResolveDistinctMembersOnce() {
            UUID memberId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            when(memberDirectory.userIdOf(memberId)).thenReturn(Optional.of(userId));
            when(userDirectory.displayName(userId)).thenReturn("Sofia Nkolo");

            Map<UUID, String> names = resolver.namesOf(List.of(memberId, memberId, memberId));

            assertEquals(Map.of(memberId, "Sofia Nkolo"), names);
            verify(memberDirectory, times(1)).userIdOf(memberId);
        }

        @Test
        @DisplayName("ignores null entries")
        void shouldIgnoreNullEntries() {
            assertEquals(Map.of(), resolver.namesOf(Arrays.asList(null, null)));
            verify(memberDirectory, never()).userIdOf(any());
        }

        @Test
        @DisplayName("returns an empty map for an empty input")
        void shouldReturnEmptyMapForEmptyInput() {
            assertEquals(Map.of(), resolver.namesOf(List.of()));
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null collaborators")
        void shouldRejectNullCollaborators() {
            assertThrows(NullPointerException.class, () -> new MemberNameResolver(null, userDirectory));
            assertThrows(NullPointerException.class, () -> new MemberNameResolver(memberDirectory, null));
        }
    }
}
