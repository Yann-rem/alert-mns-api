package com.alertmns.identity.domain.model;

import com.alertmns.identity.domain.event.AbsenceMessageUpdated;
import com.alertmns.identity.domain.event.ProfileUpdated;
import com.alertmns.identity.domain.event.UserActivated;
import com.alertmns.identity.domain.event.UserAnonymized;
import com.alertmns.identity.domain.event.UserReactivated;
import com.alertmns.identity.domain.event.UserRegistered;
import com.alertmns.identity.domain.event.UserSuspended;
import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.Email;
import com.alertmns.shared.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("User")
class UserTest {

    static final String BCRYPT_HASH = "$2a$10$abcdefghijklmnopqrstuuABCDEFGHIJKLMNOPQRSTUVWXYZ012345";
    static final Instant NOW = Instant.parse("2026-05-30T10:00:00Z");
    static final Instant LATER = NOW.plusSeconds(60);

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should register a new user with PENDING status and createdAt = now")
        void shouldRegisterANewUserWithPENDINGStatus() {
            Email email = Email.of("johndoe@example.com");
            HashedPassword hashedPassword = HashedPassword.of(BCRYPT_HASH);
            Profile profile = Profile.of(FirstName.of("John"), LastName.of("Doe"));
            User user = User.register(email, hashedPassword, profile, NOW);
            assertEquals(UserStatus.PENDING, user.status());
            assertNotNull(user.id());
            assertEquals(NOW, user.createdAt());
        }

        @Test
        @DisplayName("should reconstitute an existing user")
        void shouldReconstituteAnExistingUser() {
            UserId id = UserId.generate();
            Email email = Email.of("johndoe@example.com");
            HashedPassword hashedPassword = HashedPassword.of(BCRYPT_HASH);
            Profile profile = Profile.of(FirstName.of("John"), LastName.of("Doe"));
            Instant createdAt = NOW;

            User user = User.reconstitute(
                    id,
                    email,
                    hashedPassword,
                    profile,
                    UserStatus.ACTIVE,
                    true,
                    createdAt
            );

            assertEquals(id, user.id());
            assertEquals(email, user.email());
            assertEquals(hashedPassword, user.hashedPassword());
            assertEquals(profile, user.profile());
            assertEquals(UserStatus.ACTIVE, user.status());
            assertTrue(user.isAnonymized());
            assertEquals(createdAt, user.createdAt());
        }

        @Test
        @DisplayName("should register a new user with isAnonymized = false")
        void shouldRegisterANewUserWithIsAnonymizedFalse() {
            Email email = Email.of("johndoe@example.com");
            HashedPassword hashedPassword = HashedPassword.of(BCRYPT_HASH);
            Profile profile = Profile.of(FirstName.of("John"), LastName.of("Doe"));
            User user = User.register(email, hashedPassword, profile, NOW);
            assertFalse(user.isAnonymized());
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
                    () -> User.register(null, hashedPassword, profile, NOW));
        }

        @Test
        @DisplayName("should reject null hashedPassword")
        void shouldRejectNullHashedPassword() {
            Email email = Email.of("johndoe@example.com");
            Profile profile = Profile.of(FirstName.of("John"), LastName.of("Doe"));
            assertThrows(NullPointerException.class,
                    () -> User.register(email, null, profile, NOW));
        }

        @Test
        @DisplayName("should reject null profile")
        void shouldRejectNullProfile() {
            Email email = Email.of("johndoe@example.com");
            HashedPassword hashedPassword = HashedPassword.of(BCRYPT_HASH);
            assertThrows(NullPointerException.class,
                    () -> User.register(email, hashedPassword, null, NOW));
        }
    }

    @Nested
    @DisplayName("Behaviour")
    class Behaviour {

        User user;

        @BeforeEach
        void setUp() {
            Email email = Email.of("johndoe@example.com");
            HashedPassword hashedPassword = HashedPassword.of(BCRYPT_HASH);
            Profile profile = Profile.of(FirstName.of("John"), LastName.of("Doe"));
            user = User.register(email, hashedPassword, profile, NOW);
        }

        @Test
        @DisplayName("updateProfile should update the user profile")
        void updateProfileShouldUpdateTheUserProfile() {
            user.activateWithPassword(HashedPassword.of(BCRYPT_HASH), NOW);
            FirstName newFirstName = FirstName.of("Jane");
            LastName newLastName = LastName.of("Smith");
            String newAvatar = "https://cdn.example.com/avatar.jpg";
            user.updateProfile(newFirstName, newLastName, newAvatar, LATER);
            assertEquals(newFirstName, user.profile().firstName());
            assertEquals(newLastName, user.profile().lastName());
            assertTrue(user.profile().avatar().isPresent());
            assertEquals(newAvatar, user.profile().avatar().orElseThrow());
        }

        @Test
        @DisplayName("updateAbsenceMessage should update the absence message")
        void updateAbsenceMessageShouldUpdateTheAbsenceMessage() {
            user.activateWithPassword(HashedPassword.of(BCRYPT_HASH), NOW);
            AbsenceMessage absenceMessage = AbsenceMessage.of(
                    "Je ne suis pas disponible pour le moment", true
            );

            user.updateAbsenceMessage(absenceMessage, LATER);
            assertTrue(user.profile().absenceMessage().isPresent());
            assertEquals(absenceMessage, user.profile().absenceMessage().orElseThrow());
        }

        @Test
        @DisplayName("updateProfile should reject non-ACTIVE account")
        void updateProfileShouldRejectNonACTIVEAccount() {
            assertThrows(IllegalStateException.class,
                    () -> user.updateProfile(FirstName.of("Jane"), LastName.of("Smith"), null, NOW));
        }

        @Test
        @DisplayName("updateAbsenceMessage should reject non-ACTIVE account")
        void updateAbsenceMessageShouldRejectNonACTIVEAccount() {
            assertThrows(IllegalStateException.class,
                    () -> user.updateAbsenceMessage(AbsenceMessage.of("Absent", true), NOW));
        }

        @Test
        @DisplayName("activateWithPassword should transition PENDING to ACTIVE")
        void activateWithPasswordShouldTransitionPENDINGToACTIVE() {
            user.activateWithPassword(HashedPassword.of(BCRYPT_HASH), NOW);
            assertEquals(UserStatus.ACTIVE, user.status());
        }

        @Test
        @DisplayName("activateWithPassword should replace the hashed password")
        void activateWithPasswordShouldReplaceTheHashedPassword() {
            String newHash = "$2a$10$zzzzzzzzzzzzzzzzzzzzzzZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ987654";
            HashedPassword newHashedPassword = HashedPassword.of(newHash);

            user.activateWithPassword(newHashedPassword, NOW);

            assertEquals(newHashedPassword, user.hashedPassword());
        }

        @Test
        @DisplayName("activateWithPassword should reject null hashedPassword")
        void activateWithPasswordShouldRejectNullHashedPassword() {
            assertThrows(NullPointerException.class, () -> user.activateWithPassword(null, NOW));
        }

        @Test
        @DisplayName("activateWithPassword should reject non-PENDING account")
        void activateWithPasswordShouldRejectNonPENDINGAccount() {
            user.activateWithPassword(HashedPassword.of(BCRYPT_HASH), NOW);
            assertThrows(IllegalStateException.class,
                    () -> user.activateWithPassword(HashedPassword.of(BCRYPT_HASH), LATER));
        }

        @Test
        @DisplayName("suspend should transition ACTIVE to SUSPENDED")
        void suspendShouldTransitionACTIVEToSUSPENDED() {
            user.activateWithPassword(HashedPassword.of(BCRYPT_HASH), NOW);
            user.suspend(LATER);
            assertEquals(UserStatus.SUSPENDED, user.status());
        }

        @Test
        @DisplayName("suspend should reject non-ACTIVE account")
        void suspendShouldRejectNonACTIVEAccount() {
            assertThrows(IllegalStateException.class, () -> user.suspend(NOW));
        }

        @Test
        @DisplayName("reactivate should transition SUSPENDED to ACTIVE")
        void reactivateShouldTransitionSUSPENDEDToACTIVE() {
            user.activateWithPassword(HashedPassword.of(BCRYPT_HASH), NOW);
            user.suspend(LATER);
            user.reactivate(LATER.plusSeconds(60));
            assertEquals(UserStatus.ACTIVE, user.status());
        }

        @Test
        @DisplayName("reactivate should reject non-SUSPENDED account")
        void reactivateShouldRejectNonSUSPENDEDAccount() {
            assertThrows(IllegalStateException.class, () -> user.reactivate(NOW));
        }

        @Test
        @DisplayName("anonymize should scrub email, name and password with non-identifying placeholders")
        void anonymizeShouldScrubPii() {
            user.activateWithPassword(HashedPassword.of(BCRYPT_HASH), NOW);
            UserId id = user.id();

            user.anonymize(LATER);

            assertEquals("anonymized-" + id.value() + "@deleted.local", user.email().value());
            assertEquals("Utilisateur", user.profile().firstName().value());
            assertEquals("Supprimé", user.profile().lastName().value());
            assertTrue(user.hashedPassword().isUnset());
        }

        @Test
        @DisplayName("anonymize should clear avatar and absence message")
        void anonymizeShouldClearAvatarAndAbsenceMessage() {
            user.activateWithPassword(HashedPassword.of(BCRYPT_HASH), NOW);
            user.updateProfile(FirstName.of("Jane"), LastName.of("Smith"), "https://cdn.example.com/a.jpg", NOW);
            user.updateAbsenceMessage(AbsenceMessage.of("Absent", true), NOW);

            user.anonymize(LATER);

            assertTrue(user.profile().avatar().isEmpty());
            assertTrue(user.profile().absenceMessage().isEmpty());
        }

        @Test
        @DisplayName("anonymize should set isAnonymized and preserve the id and the access status")
        void anonymizeShouldSetFlagAndPreserveIdAndStatus() {
            user.activateWithPassword(HashedPassword.of(BCRYPT_HASH), NOW);
            UserId id = user.id();

            user.anonymize(LATER);

            assertTrue(user.isAnonymized());
            assertEquals(id, user.id());
            assertEquals(UserStatus.ACTIVE, user.status());
        }

        @Test
        @DisplayName("anonymize should be idempotent: a second call is a no-op")
        void anonymizeShouldBeIdempotent() {
            user.activateWithPassword(HashedPassword.of(BCRYPT_HASH), NOW);
            user.anonymize(LATER);
            Email afterFirst = user.email();

            user.anonymize(LATER.plusSeconds(60));

            assertTrue(user.isAnonymized());
            assertEquals(afterFirst, user.email());
        }

        @Test
        @DisplayName("anonymize should reject null now")
        void anonymizeShouldRejectNullNow() {
            assertThrows(NullPointerException.class, () -> user.anonymize(null));
        }
    }

    @Nested
    @DisplayName("Domain Events")
    class DomainEvents {

        User user;

        @BeforeEach
        void setUp() {
            Email email = Email.of("johndoe@example.com");
            HashedPassword hashedPassword = HashedPassword.of(BCRYPT_HASH);
            Profile profile = Profile.of(FirstName.of("John"), LastName.of("Doe"));
            user = User.register(email, hashedPassword, profile, NOW);
        }

        @Test
        @DisplayName("register should emit UserRegistered with occurredOn = now")
        void registerShouldEmitUserRegistered() {
            List<DomainEvent> events = user.pullDomainEvents();
            assertEquals(1, events.size());
            UserRegistered event = assertInstanceOf(UserRegistered.class, events.getFirst());
            assertEquals(user.id(), event.userId());
            assertEquals(user.email(), event.email());
            assertEquals(NOW, event.occurredOn());
        }

        @Test
        @DisplayName("updateProfile should emit ProfileUpdated with occurredOn = now")
        void updateProfileShouldEmitUpdateProfile() {
            user.activateWithPassword(HashedPassword.of(BCRYPT_HASH), NOW);
            user.pullDomainEvents();

            user.updateProfile(
                    FirstName.of("Jane"),
                    LastName.of("Doe"),
                    "https://cdn.example.com/avatar.jpg",
                    LATER
            );

            List<DomainEvent> events = user.pullDomainEvents();
            assertEquals(1, events.size());
            ProfileUpdated event = assertInstanceOf(ProfileUpdated.class, events.getFirst());
            assertEquals(user.id(), event.userId());
            assertEquals(LATER, event.occurredOn());
        }

        @Test
        @DisplayName("updateAbsenceMessage should emit AbsenceMessageUpdated")
        void updateAbsenceMessageShouldEmitUpdateAbsenceMessage() {
            user.activateWithPassword(HashedPassword.of(BCRYPT_HASH), NOW);
            user.pullDomainEvents();

            user.updateAbsenceMessage(AbsenceMessage.of(
                    "je ne suis pas disponible pour le moment",
                    true
            ), LATER);

            List<DomainEvent> events = user.pullDomainEvents();
            assertEquals(1, events.size());
            AbsenceMessageUpdated event = assertInstanceOf(AbsenceMessageUpdated.class, events.getFirst());
            assertEquals(user.id(), event.userId());
            assertEquals(LATER, event.occurredOn());
        }

        @Test
        @DisplayName("activateWithPassword should emit UserActivated with userId, email and occurredOn")
        void activateWithPasswordShouldEmitUserActivated() {
            user.pullDomainEvents();
            user.activateWithPassword(HashedPassword.of(BCRYPT_HASH), LATER);
            List<DomainEvent> events = user.pullDomainEvents();
            assertEquals(1, events.size());
            UserActivated event = assertInstanceOf(UserActivated.class, events.getFirst());
            assertEquals(user.id(), event.userId());
            assertEquals(user.email(), event.email());
            assertEquals(LATER, event.occurredOn());
        }

        @Test
        @DisplayName("suspend should emit UserSuspended")
        void suspendShouldEmitUserSuspended() {
            user.pullDomainEvents();
            user.activateWithPassword(HashedPassword.of(BCRYPT_HASH), NOW);
            user.pullDomainEvents();
            user.suspend(LATER);
            List<DomainEvent> events = user.pullDomainEvents();
            assertEquals(1, events.size());
            UserSuspended event = assertInstanceOf(UserSuspended.class, events.getFirst());
            assertEquals(user.id(), event.userId());
            assertEquals(LATER, event.occurredOn());
        }

        @Test
        @DisplayName("reactivate should emit UserReactivated")
        void reactivateShouldEmitUserReactivated() {
            user.pullDomainEvents();
            user.activateWithPassword(HashedPassword.of(BCRYPT_HASH), NOW);
            user.pullDomainEvents();
            user.suspend(NOW);
            user.pullDomainEvents();
            user.reactivate(LATER);
            List<DomainEvent> events = user.pullDomainEvents();
            assertEquals(1, events.size());
            UserReactivated event = assertInstanceOf(UserReactivated.class, events.getFirst());
            assertEquals(user.id(), event.userId());
            assertEquals(LATER, event.occurredOn());
        }

        @Test
        @DisplayName("anonymize should emit UserAnonymized with occurredOn = now")
        void anonymizeShouldEmitUserAnonymized() {
            user.activateWithPassword(HashedPassword.of(BCRYPT_HASH), NOW);
            user.pullDomainEvents();

            user.anonymize(LATER);

            List<DomainEvent> events = user.pullDomainEvents();
            assertEquals(1, events.size());
            UserAnonymized event = assertInstanceOf(UserAnonymized.class, events.getFirst());
            assertEquals(user.id(), event.userId());
            assertEquals(LATER, event.occurredOn());
        }

        @Test
        @DisplayName("anonymize should not emit a second event when already anonymized")
        void anonymizeTwiceShouldEmitOnlyOneEvent() {
            user.anonymize(NOW);
            user.pullDomainEvents();

            user.anonymize(LATER);

            assertTrue(user.pullDomainEvents().isEmpty());
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

            User user1 = User.reconstitute(
                    id, email, hashedPassword, profile,
                    UserStatus.ACTIVE, false, NOW);

            User user2 = User.reconstitute(
                    id, email, hashedPassword, profile,
                    UserStatus.ACTIVE, false, NOW);

            assertEquals(user1, user2);
        }

        @Test
        @DisplayName("two users with different ids should not be equal")
        void twoUsersWithDifferentIdsShouldNotBeEqual() {
            Email email = Email.of("johndoe@example.com");
            HashedPassword hashedPassword = HashedPassword.of(BCRYPT_HASH);
            Profile profile = Profile.of(FirstName.of("John"), LastName.of("Doe"));
            User user1 = User.register(email, hashedPassword, profile, NOW);
            User user2 = User.register(email, hashedPassword, profile, NOW);
            assertNotEquals(user1, user2);
        }
    }
}
