package com.alertmns.identity.infrastructure.adapter.incoming.web.security;

import com.alertmns.identity.domain.model.Email;
import com.alertmns.identity.domain.model.FirstName;
import com.alertmns.identity.domain.model.HashedPassword;
import com.alertmns.identity.domain.model.LastName;
import com.alertmns.identity.domain.model.Profile;
import com.alertmns.identity.domain.model.User;
import com.alertmns.identity.domain.model.UserStatus;
import com.alertmns.identity.domain.port.outgoing.UserAuthoritiesProvider;
import com.alertmns.identity.domain.port.outgoing.UserRepository;
import com.alertmns.shared.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("DomainUserDetailsService")
@ExtendWith(MockitoExtension.class)
class DomainUserDetailsServiceTest {

    static final String EMAIL = "johndoe@example.com";

    @Mock
    UserRepository userRepository;

    @Mock
    UserAuthoritiesProvider userAuthoritiesProvider;

    @InjectMocks
    DomainUserDetailsService service;

    User user;

    @BeforeEach
    void setUp() {
        user = User.reconstitute(
                UserId.generate(),
                Email.of(EMAIL),
                HashedPassword.of("$2a$10$abcdefghijklmnopqrstuuABCDEFGHIJKLMNOPQRSTUVWXYZ012345"),
                Profile.of(FirstName.of("John"), LastName.of("Doe")),
                UserStatus.ACTIVE,
                false,
                Instant.now()
        );
    }

    @Nested
    @DisplayName("loadUserByUsername")
    class LoadUserByUsername {

        @Test
        @DisplayName("should return DomainUserDetails when user is found")
        void shouldReturnDomainUserDetailsWhenUserIsFound() {
            when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
            when(userAuthoritiesProvider.findAuthorities(user.id())).thenReturn(List.of());

            UserDetails details = service.loadUserByUsername(EMAIL);

            DomainUserDetails domainUserDetails = assertInstanceOf(DomainUserDetails.class, details);
            assertEquals(user.id(), domainUserDetails.userId());
            assertEquals(EMAIL, domainUserDetails.getUsername());
        }

        @Test
        @DisplayName("should query repository with the email VO")
        void shouldQueryRepositoryWithEmailVO() {
            when(userRepository.findByEmail(Email.of(EMAIL))).thenReturn(Optional.of(user));
            when(userAuthoritiesProvider.findAuthorities(user.id())).thenReturn(List.of());

            service.loadUserByUsername(EMAIL);
            // verified by the strict stub matching above: if the email VO was wrong, the stub would not match
        }

        @Test
        @DisplayName("should resolve authorities via the provider and expose them on the UserDetails")
        void shouldResolveAuthoritiesViaProvider() {
            when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
            when(userAuthoritiesProvider.findAuthorities(user.id())).thenReturn(List.of("ROLE_ADMIN"));

            UserDetails details = service.loadUserByUsername(EMAIL);

            assertThat(details.getAuthorities())
                    .extracting("authority")
                    .containsExactly("ROLE_ADMIN");
            verify(userAuthoritiesProvider).findAuthorities(user.id());
        }

        @Test
        @DisplayName("should expose an empty authority collection when the provider returns none")
        void shouldExposeEmptyAuthoritiesWhenProviderReturnsNone() {
            when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
            when(userAuthoritiesProvider.findAuthorities(user.id())).thenReturn(List.of());

            UserDetails details = service.loadUserByUsername(EMAIL);

            assertThat(details.getAuthorities()).isEmpty();
        }

        @Test
        @DisplayName("should throw UsernameNotFoundException when user is not found")
        void shouldThrowUsernameNotFoundExceptionWhenUserIsNotFound() {
            when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

            assertThrows(UsernameNotFoundException.class,
                    () -> service.loadUserByUsername(EMAIL));
            verify(userAuthoritiesProvider, never()).findAuthorities(any());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when email is not a valid format")
        void shouldThrowWhenEmailIsInvalid() {
            // Email.of() valide le format ; loadUserByUsername propage l'exception levée par le VO
            assertThrows(IllegalArgumentException.class,
                    () -> service.loadUserByUsername("not-an-email"));
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null UserRepository")
        void shouldRejectNullUserRepository() {
            assertThrows(NullPointerException.class,
                    () -> new DomainUserDetailsService(null, userAuthoritiesProvider));
        }

        @Test
        @DisplayName("should reject null UserAuthoritiesProvider")
        void shouldRejectNullUserAuthoritiesProvider() {
            assertThrows(NullPointerException.class,
                    () -> new DomainUserDetailsService(userRepository, null));
        }
    }
}
