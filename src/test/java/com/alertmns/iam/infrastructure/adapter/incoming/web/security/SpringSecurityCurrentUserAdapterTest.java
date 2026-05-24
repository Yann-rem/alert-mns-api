package com.alertmns.iam.infrastructure.adapter.incoming.web.security;

import com.alertmns.iam.domain.model.Email;
import com.alertmns.iam.domain.model.FirstName;
import com.alertmns.iam.domain.model.HashedPassword;
import com.alertmns.iam.domain.model.LastName;
import com.alertmns.iam.domain.model.Profile;
import com.alertmns.iam.domain.model.User;
import com.alertmns.iam.domain.model.UserRole;
import com.alertmns.iam.domain.model.UserStatus;
import com.alertmns.shared.AuthenticatedUser;
import com.alertmns.shared.UserId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("SpringSecurityCurrentUserAdapter")
class SpringSecurityCurrentUserAdapterTest {

    SpringSecurityCurrentUserAdapter adapter;
    User user;
    DomainUserDetails details;

    @BeforeEach
    void setUp() {
        adapter = new SpringSecurityCurrentUserAdapter();

        user = User.reconstitute(
                UserId.generate(),
                Email.of("johndoe@example.com"),
                HashedPassword.of("$2a$10$abcdefghijklmnopqrstuuABCDEFGHIJKLMNOPQRSTUVWXYZ012345"),
                Profile.of(FirstName.of("John"), LastName.of("Doe")),
                UserRole.USER,
                UserStatus.ACTIVE,
                false,
                Instant.now()
        );

        details = new DomainUserDetails(user, List.of("ROLE_MEMBER"));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("currentUser")
    class CurrentUser {

        @Test
        @DisplayName("should return AuthenticatedUser when principal is a DomainUserDetails")
        void shouldReturnAuthenticatedUserWhenPrincipalIsDomainUserDetails() {
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    details, null, details.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            Optional<AuthenticatedUser> currentUser = adapter.currentUser();

            assertTrue(currentUser.isPresent());
            assertEquals(user.id(), currentUser.get().userId());
        }

        @Test
        @DisplayName("should return empty when no authentication is present in context")
        void shouldReturnEmptyWhenNoAuthenticationIsPresent() {
            // SecurityContext vide par défaut (clearContext dans @AfterEach précédent ou jamais set)
            SecurityContextHolder.clearContext();

            Optional<AuthenticatedUser> currentUser = adapter.currentUser();

            assertTrue(currentUser.isEmpty());
        }

        @Test
        @DisplayName("should return empty when authentication is not authenticated")
        void shouldReturnEmptyWhenAuthenticationIsNotAuthenticated() {
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(details, null);
            // ce constructeur (sans authorities) marque le token comme non authentifié
            SecurityContextHolder.getContext().setAuthentication(authentication);

            Optional<AuthenticatedUser> currentUser = adapter.currentUser();

            assertTrue(currentUser.isEmpty());
        }

        @Test
        @DisplayName("should return empty when token is anonymous")
        void shouldReturnEmptyWhenTokenIsAnonymous() {
            Authentication anonymous = new AnonymousAuthenticationToken(
                    "key",
                    "anonymousUser",
                    List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
            );
            SecurityContextHolder.getContext().setAuthentication(anonymous);

            Optional<AuthenticatedUser> currentUser = adapter.currentUser();

            assertTrue(currentUser.isEmpty());
        }

        @Test
        @DisplayName("should return empty when principal is a String (e.g. @WithMockUser default)")
        void shouldReturnEmptyWhenPrincipalIsAString() {
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    "someUsername", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            Optional<AuthenticatedUser> currentUser = adapter.currentUser();

            assertTrue(currentUser.isEmpty());
        }

        @Test
        @DisplayName("should return empty when principal is a Spring User (not a DomainUserDetails)")
        void shouldReturnEmptyWhenPrincipalIsSpringUser() {
            org.springframework.security.core.userdetails.User springUser =
                    new org.springframework.security.core.userdetails.User(
                            "user@example.com",
                            "password",
                            List.of(new SimpleGrantedAuthority("ROLE_USER"))
                    );
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    springUser, null, springUser.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            Optional<AuthenticatedUser> currentUser = adapter.currentUser();

            assertTrue(currentUser.isEmpty());
        }
    }
}
