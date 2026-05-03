package com.alertmns.iam.infrastructure.adapter.incoming.web.security;

import com.alertmns.iam.domain.model.Email;
import com.alertmns.iam.domain.model.FirstName;
import com.alertmns.iam.domain.model.HashedPassword;
import com.alertmns.iam.domain.model.LastName;
import com.alertmns.iam.domain.model.Profile;
import com.alertmns.iam.domain.model.User;
import com.alertmns.iam.domain.model.UserRole;
import com.alertmns.iam.domain.model.UserStatus;
import com.alertmns.shared.OrganisationId;
import com.alertmns.shared.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("DomainUserDetails")
class DomainUserDetailsTest {

    static final OrganisationId ORGANISATION_ID = OrganisationId.generate();
    static final String EMAIL = "johndoe@example.com";
    static final String HASH = "$2a$10$abcdefghijklmnopqrstuuABCDEFGHIJKLMNOPQRSTUVWXYZ012345";

    static User userWith(UserStatus status, UserRole role, boolean isAnonymized) {
        return User.reconstitute(
                UserId.generate(),
                ORGANISATION_ID,
                Email.of(EMAIL),
                HashedPassword.of(HASH),
                Profile.of(FirstName.of("John"), LastName.of("Doe")),
                role,
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
                    userWith(UserStatus.ACTIVE, UserRole.USER, false));

            assertEquals(EMAIL, details.getUsername());
        }

        @Test
        @DisplayName("should expose hashed password value")
        void shouldExposeHashedPassword() {
            DomainUserDetails details = new DomainUserDetails(
                    userWith(UserStatus.ACTIVE, UserRole.USER, false));

            assertEquals(HASH, details.getPassword());
        }

        @Test
        @DisplayName("should expose role with ROLE_ prefix as authority")
        void shouldExposeRoleWithRolePrefix() {
            DomainUserDetails details = new DomainUserDetails(
                    userWith(UserStatus.ACTIVE, UserRole.USER, false));

            assertEquals(1, details.getAuthorities().size());
            assertEquals("ROLE_USER", details.getAuthorities().iterator().next().getAuthority());
        }

        @Test
        @DisplayName("should expose userId from underlying user")
        void shouldExposeUserId() {
            User user = userWith(UserStatus.ACTIVE, UserRole.USER, false);
            DomainUserDetails details = new DomainUserDetails(user);

            assertEquals(user.id(), details.userId());
        }

        @Test
        @DisplayName("should expose organisationId from underlying user")
        void shouldExposeOrganisationId() {
            User user = userWith(UserStatus.ACTIVE, UserRole.USER, false);
            DomainUserDetails details = new DomainUserDetails(user);

            assertEquals(ORGANISATION_ID, details.organisationId());
        }
    }

    @Nested
    @DisplayName("isEnabled")
    class IsEnabled {

        @Test
        @DisplayName("should be true when status is ACTIVE and not anonymized")
        void shouldBeTrueWhenActiveAndNotAnonymized() {
            DomainUserDetails details = new DomainUserDetails(
                    userWith(UserStatus.ACTIVE, UserRole.USER, false));

            assertTrue(details.isEnabled());
        }

        @Test
        @DisplayName("should be false when status is ACTIVE but anonymized")
        void shouldBeFalseWhenActiveButAnonymized() {
            DomainUserDetails details = new DomainUserDetails(
                    userWith(UserStatus.ACTIVE, UserRole.USER, true));

            assertFalse(details.isEnabled());
        }

        @ParameterizedTest
        @EnumSource(value = UserStatus.class, names = {"PENDING", "SUSPENDED", "BANNED"})
        @DisplayName("should be false when status is not ACTIVE")
        void shouldBeFalseWhenNotActive(UserStatus status) {
            DomainUserDetails details = new DomainUserDetails(
                    userWith(status, UserRole.USER, false));

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
                    userWith(status, UserRole.USER, false));

            assertTrue(details.isAccountNonLocked());
        }

        @ParameterizedTest
        @EnumSource(value = UserStatus.class, names = {"SUSPENDED", "BANNED"})
        @DisplayName("should be false when status is SUSPENDED or BANNED")
        void shouldBeFalseWhenSuspendedOrBanned(UserStatus status) {
            DomainUserDetails details = new DomainUserDetails(
                    userWith(status, UserRole.USER, false));

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
                    userWith(UserStatus.ACTIVE, UserRole.USER, false));

            assertTrue(details.isAccountNonExpired());
        }

        @Test
        @DisplayName("isCredentialsNonExpired should always be true")
        void isCredentialsNonExpiredShouldAlwaysBeTrue() {
            DomainUserDetails details = new DomainUserDetails(
                    userWith(UserStatus.ACTIVE, UserRole.USER, false));

            assertTrue(details.isCredentialsNonExpired());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null user")
        void shouldRejectNullUser() {
            assertThrows(NullPointerException.class, () -> new DomainUserDetails(null));
        }
    }
}
