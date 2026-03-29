package com.alertmns.iam.domain.model;

import com.alertmns.iam.domain.event.UserActivated;
import com.alertmns.iam.domain.event.UserDisabled;
import com.alertmns.iam.domain.event.UserRegistered;
import com.alertmns.shared.DomainEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User")
class UserTest {

    private static final String BCRYPT_HASH =
            "$2a$10$abcdefghijklmnopqrstuuABCDEFGHIJKLMNOPQRSTUVWXYZ012345";

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should register a new user with PENDING status")
        void shouldRegisterANewUserWithPENDINGStatus() {
            Email email = Email.of("johndoe@example.com");
            HashedPassword hashedPassword = HashedPassword.of(BCRYPT_HASH);
            Profile profile = Profile.of(FirstName.of("John"), LastName.of("Doe"));
            User user = User.register(email, hashedPassword, profile);
            assertEquals(UserStatus.PENDING, user.status());
            assertEquals(Role.USER, user.role());
            assertNotNull(user.id());
            assertNotNull(user.createdAt());
        }

        @Test
        @DisplayName("should reconstitute an existing user")
        void shouldReconstituteAnExistingUser() {
            UserId id = UserId.generate();
            Email email = Email.of("johndoe@example.com");
            HashedPassword hashedPassword = HashedPassword.of(BCRYPT_HASH);
            Profile profile = Profile.of(FirstName.of("John"), LastName.of("Doe"));
            Instant createdAt = Instant.now();

            User user = User.reconstitute(
                    id, email, hashedPassword, profile,
                    Role.ADMIN, UserStatus.ACTIVE, createdAt
            );

            assertEquals(id, user.id());
            assertEquals(email, user.email());
            assertEquals(hashedPassword, user.hashedPassword());
            assertEquals(profile, user.profile());
            assertEquals(Role.ADMIN, user.role());
            assertEquals(UserStatus.ACTIVE, user.status());
            assertEquals(createdAt, user.createdAt());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null email")
        void shouldRejectNullEmail() {
            HashedPassword hashedPassword = HashedPassword.of(BCRYPT_HASH);
            Profile profile = Profile.of(FirstName.of("John"), LastName.of("Doe"));
            assertThrows(NullPointerException.class,
                    () -> User.register(null, hashedPassword, profile));
        }

        @Test
        @DisplayName("should reject null hashedPassword")
        void shouldRejectNullHashedPassword() {
            Email email = Email.of("johndoe@example.com");
            Profile profile = Profile.of(FirstName.of("John"), LastName.of("Doe"));
            assertThrows(NullPointerException.class,
                    () -> User.register(email, null, profile));
        }

        @Test
        @DisplayName("should reject null profile")
        void shouldRejectNullProfile() {
            Email email = Email.of("johndoe@example.com");
            HashedPassword hashedPassword = HashedPassword.of(BCRYPT_HASH);
            assertThrows(NullPointerException.class,
                    () -> User.register(email, hashedPassword, null));
        }
    }

    @Nested
    @DisplayName("Behaviour")
    class Behaviour {

        private User user;

        @BeforeEach
        void setUp() {
            Email email = Email.of("johndoe@example.com");
            HashedPassword hashedPassword = HashedPassword.of(BCRYPT_HASH);
            Profile profile = Profile.of(FirstName.of("John"), LastName.of("Doe"));
            user = User.register(email, hashedPassword, profile);
        }

        @Test
        @DisplayName("activate should transition PENDING to ACTIVE")
        void activateShouldTransitionPENDINGToACTIVE() {
            user.activate();
            assertEquals(UserStatus.ACTIVE, user.status());
        }

        @Test
        @DisplayName("activate should reject non-PENDING account")
        void activateShouldRejectNonPENDINGAccount() {
            user.activate();
            assertThrows(IllegalStateException.class, () -> user.activate());
        }

        @Test
        @DisplayName("disable should transition ACTIVE to DISABLED")
        void disableShouldTransitionACTIVEToDISABLED() {
            user.activate();
            user.disable();
            assertEquals(UserStatus.DISABLED, user.status());
        }

        @Test
        @DisplayName("disable should reject non-ACTIVE account")
        void disableShouldRejectNonACTIVEAccount() {
            assertThrows(IllegalStateException.class, () -> user.disable());
        }

        @Test
        @DisplayName("reactivate should transition DISABLED to ACTIVE")
        void reactivateShouldTransitionDISABLEDToACTIVE() {
            user.activate();
            user.disable();
            user.reactivate();
            assertEquals(UserStatus.ACTIVE, user.status());
        }

        @Test
        @DisplayName("reactivate should reject non-DISABLED account")
        void reactivateShouldRejectNonDISABLEDAccount() {
            assertThrows(IllegalStateException.class, () -> user.reactivate());
        }
    }

    @Nested
    @DisplayName("Domain Events")
    class DomainEvents {

        private User user;

        @BeforeEach
        void setUp() {
            Email email = Email.of("johndoe@example.com");
            HashedPassword hashedPassword = HashedPassword.of(BCRYPT_HASH);
            Profile profile = Profile.of(FirstName.of("John"), LastName.of("Doe"));
            user = User.register(email, hashedPassword, profile);
        }

        @Test
        @DisplayName("register should emit UserRegistered")
        void registerShouldEmitUserRegistered() {
            List<DomainEvent> events = user.pullDomainEvents();
            assertEquals(1, events.size());
            assertInstanceOf(UserRegistered.class, events.getFirst());
            UserRegistered event = (UserRegistered) events.getFirst();
            assertEquals(user.id(), event.userId());
            assertEquals(user.email(), event.email());
            assertEquals(Role.USER, event.role());
        }

        @Test
        @DisplayName("activate should emit UserActivated")
        void activateShouldEmitUserActivated() {
            user.pullDomainEvents();
            user.activate();
            List<DomainEvent> events = user.pullDomainEvents();
            assertEquals(1, events.size());
            assertInstanceOf(UserActivated.class, events.getFirst());
            UserActivated event = (UserActivated) events.getFirst();
            assertEquals(user.id(), event.userId());
        }

        @Test
        @DisplayName("disable should emit UserDisabled")
        void disableShouldEmitUserDisabled() {
            user.pullDomainEvents();
            user.activate();
            user.pullDomainEvents();
            user.disable();
            List<DomainEvent> events = user.pullDomainEvents();
            assertEquals(1, events.size());
            assertInstanceOf(UserDisabled.class, events.getFirst());
            UserDisabled event = (UserDisabled) events.getFirst();
            assertEquals(user.id(), event.userId());
        }

        @Test
        @DisplayName("pullDomainEvents should clear events after pull")
        void pullDomainEventsShouldClearEventsAfterPull() {
            user.pullDomainEvents();
            List<DomainEvent> events = user.pullDomainEvents();
            assertTrue(events.isEmpty());
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("two users with same id should be equal")
        void twoUsersWithSameIdShouldBeEqual() {
            UserId id = UserId.generate();
            Email email = Email.of("johndoe@example.com");
            HashedPassword hashedPassword = HashedPassword.of(BCRYPT_HASH);
            Profile profile = Profile.of(FirstName.of("John"), LastName.of("Doe"));
            Instant createdAt = Instant.now();

            User user1 = User.reconstitute(
                    id, email, hashedPassword, profile,
                    Role.ADMIN, UserStatus.ACTIVE, createdAt
            );

            User user2 = User.reconstitute(
                    id, email, hashedPassword, profile,
                    Role.ADMIN, UserStatus.ACTIVE, createdAt
            );

            assertEquals(user1, user2);
        }

        @Test
        @DisplayName("two users with different ids should not be equal")
        void twoUsersWithDifferentIdsShouldNotBeEqual() {
            Email email = Email.of("johndoe@example.com");
            HashedPassword hashedPassword = HashedPassword.of(BCRYPT_HASH);
            Profile profile = Profile.of(FirstName.of("John"), LastName.of("Doe"));
            User user1 = User.register(email, hashedPassword, profile);
            User user2 = User.register(email, hashedPassword, profile);
            assertNotEquals(user1, user2);
        }
    }
}
