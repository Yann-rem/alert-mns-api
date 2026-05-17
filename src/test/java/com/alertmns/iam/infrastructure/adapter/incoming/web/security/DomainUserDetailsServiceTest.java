package com.alertmns.iam.infrastructure.adapter.incoming.web.security;

import com.alertmns.iam.domain.model.Email;
import com.alertmns.iam.domain.model.FirstName;
import com.alertmns.iam.domain.model.HashedPassword;
import com.alertmns.iam.domain.model.LastName;
import com.alertmns.iam.domain.model.Profile;
import com.alertmns.iam.domain.model.User;
import com.alertmns.iam.domain.model.UserRole;
import com.alertmns.iam.domain.model.UserStatus;
import com.alertmns.iam.domain.port.outgoing.UserRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("DomainUserDetailsService")
@ExtendWith(MockitoExtension.class)
class DomainUserDetailsServiceTest {

    static final String EMAIL = "johndoe@example.com";

    @Mock
    UserRepository userRepository;

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
                UserRole.USER,
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

            UserDetails details = service.loadUserByUsername(EMAIL);

            DomainUserDetails domainUserDetails = assertInstanceOf(DomainUserDetails.class, details);
            assertEquals(user.id(), domainUserDetails.userId());
            assertEquals(EMAIL, domainUserDetails.getUsername());
        }

        @Test
        @DisplayName("should query repository with the email VO")
        void shouldQueryRepositoryWithEmailVO() {
            when(userRepository.findByEmail(Email.of(EMAIL))).thenReturn(Optional.of(user));

            service.loadUserByUsername(EMAIL);
            // verified by the strict stub matching above: if the email VO was wrong, the stub would not match
        }

        @Test
        @DisplayName("should throw UsernameNotFoundException when user is not found")
        void shouldThrowUsernameNotFoundExceptionWhenUserIsNotFound() {
            when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

            assertThrows(UsernameNotFoundException.class,
                    () -> service.loadUserByUsername(EMAIL));
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
        @DisplayName("should reject null repository")
        void shouldRejectNullRepository() {
            assertThrows(NullPointerException.class,
                    () -> new DomainUserDetailsService(null));
        }
    }
}
