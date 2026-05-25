package com.alertmns.identity.infrastructure.adapter.incoming.web.security;

import com.alertmns.shared.Email;
import com.alertmns.identity.domain.model.FirstName;
import com.alertmns.identity.domain.model.HashedPassword;
import com.alertmns.identity.domain.model.LastName;
import com.alertmns.identity.domain.model.Profile;
import com.alertmns.identity.domain.model.User;
import com.alertmns.identity.domain.model.UserStatus;
import com.alertmns.shared.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("DomainUserDetails")
class DomainUserDetailsTest {

    static final String EMAIL = "johndoe@example.com";
    static final String HASH = "$2a$10$abcdefghijklmnopqrstuuABCDEFGHIJKLMNOPQRSTUVWXYZ012345";
    static final List<String> NO_AUTHORITIES = List.of();
    static final List<String> ROLE_MEMBER = List.of("ROLE_MEMBER");

    static User userWith(UserStatus status, boolean isAnonymized) {
        return User.reconstitute(
                UserId.generate(),
                Email.of(EMAIL),
                HashedPassword.of(HASH),
                Profile.of(FirstName.of("John"), LastName.of("Doe")),
                status,
                isAnonymized,
                Instant.now()
        );
    }

    @Nested
    @DisplayName("Mapping")
    class Mapping {

        @Test
        @DisplayName("should expose email as username")
        void shouldExposeEmailAsUsername() {
            DomainUserDetails details = new DomainUserDetails(
                    userWith(UserStatus.ACTIVE, false), ROLE_MEMBER);

            assertEquals(EMAIL, details.getUsername());
        }

        @Test
        @DisplayName("should expose hashed password value")
        void shouldExposeHashedPassword() {
            DomainUserDetails details = new DomainUserDetails(
                    userWith(UserStatus.ACTIVE, false), ROLE_MEMBER);

            assertEquals(HASH, details.getPassword());
        }

        @Test
        @DisplayName("should wrap injected authority names as SimpleGrantedAuthority")
        void shouldWrapInjectedAuthoritiesAsSimpleGrantedAuthority() {
            DomainUserDetails details = new DomainUserDetails(
                    userWith(UserStatus.ACTIVE, false), List.of("ROLE_ADMIN"));

            assertThat(details.getAuthorities())
                    .extracting("authority")
                    .containsExactly("ROLE_ADMIN");
        }

        @Test
        @DisplayName("should preserve multiple authorities in order")
        void shouldPreserveMultipleAuthoritiesInOrder() {
            DomainUserDetails details = new DomainUserDetails(
                    userWith(UserStatus.ACTIVE, false), List.of("ROLE_ADMIN", "ROLE_MEMBER"));

            assertThat(details.getAuthorities())
                    .extracting("authority")
                    .containsExactly("ROLE_ADMIN", "ROLE_MEMBER");
        }

        @Test
        @DisplayName("should expose an empty authority collection when none are provided")
        void shouldExposeEmptyAuthoritiesWhenNoneProvided() {
            DomainUserDetails details = new DomainUserDetails(
                    userWith(UserStatus.ACTIVE, false), NO_AUTHORITIES);

            assertThat(details.getAuthorities()).isEmpty();
        }

        @Test
        @DisplayName("should expose userId from underlying user")
        void shouldExposeUserId() {
            User user = userWith(UserStatus.ACTIVE, false);
            DomainUserDetails details = new DomainUserDetails(user, ROLE_MEMBER);

            assertEquals(user.id(), details.userId());
        }
    }

    @Nested
    @DisplayName("isEnabled")
    class IsEnabled {

        @Test
        @DisplayName("should be true when status is ACTIVE and not anonymized")
        void shouldBeTrueWhenActiveAndNotAnonymized() {
            DomainUserDetails details = new DomainUserDetails(
                    userWith(UserStatus.ACTIVE, false), ROLE_MEMBER);

            assertTrue(details.isEnabled());
        }

        @Test
        @DisplayName("should be false when status is ACTIVE but anonymized")
        void shouldBeFalseWhenActiveButAnonymized() {
            DomainUserDetails details = new DomainUserDetails(
                    userWith(UserStatus.ACTIVE, true), ROLE_MEMBER);

            assertFalse(details.isEnabled());
        }

        @ParameterizedTest
        @EnumSource(value = UserStatus.class, names = {"PENDING", "SUSPENDED", "BANNED"})
        @DisplayName("should be false when status is not ACTIVE")
        void shouldBeFalseWhenNotActive(UserStatus status) {
            DomainUserDetails details = new DomainUserDetails(
                    userWith(status, false), ROLE_MEMBER);

            assertFalse(details.isEnabled());
        }
    }

    @Nested
    @DisplayName("isAccountNonLocked")
    class IsAccountNonLocked {

        @ParameterizedTest
        @EnumSource(value = UserStatus.class, names = {"PENDING", "ACTIVE"})
        @DisplayName("should be true when status is PENDING or ACTIVE")
        void shouldBeTrueWhenPendingOrActive(UserStatus status) {
            DomainUserDetails details = new DomainUserDetails(
                    userWith(status, false), ROLE_MEMBER);

            assertTrue(details.isAccountNonLocked());
        }

        @ParameterizedTest
        @EnumSource(value = UserStatus.class, names = {"SUSPENDED", "BANNED"})
        @DisplayName("should be false when status is SUSPENDED or BANNED")
        void shouldBeFalseWhenSuspendedOrBanned(UserStatus status) {
            DomainUserDetails details = new DomainUserDetails(
                    userWith(status, false), ROLE_MEMBER);

            assertFalse(details.isAccountNonLocked());
        }
    }

    @Nested
    @DisplayName("Always-true flags")
    class AlwaysTrueFlags {

        @Test
        @DisplayName("isAccountNonExpired should always be true")
        void isAccountNonExpiredShouldAlwaysBeTrue() {
            DomainUserDetails details = new DomainUserDetails(
                    userWith(UserStatus.ACTIVE, false), ROLE_MEMBER);

            assertTrue(details.isAccountNonExpired());
        }

        @Test
        @DisplayName("isCredentialsNonExpired should always be true")
        void isCredentialsNonExpiredShouldAlwaysBeTrue() {
            DomainUserDetails details = new DomainUserDetails(
                    userWith(UserStatus.ACTIVE, false), ROLE_MEMBER);

            assertTrue(details.isCredentialsNonExpired());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null user")
        void shouldRejectNullUser() {
            assertThrows(NullPointerException.class,
                    () -> new DomainUserDetails(null, ROLE_MEMBER));
        }

        @Test
        @DisplayName("should reject null authority names")
        void shouldRejectNullAuthorityNames() {
            assertThrows(NullPointerException.class,
                    () -> new DomainUserDetails(userWith(UserStatus.ACTIVE, false), null));
        }
    }
}
