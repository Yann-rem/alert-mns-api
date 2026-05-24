package com.alertmns.iam.domain.model;

import com.alertmns.iam.domain.event.AbsenceMessageUpdated;
import com.alertmns.iam.domain.event.ProfileUpdated;
import com.alertmns.iam.domain.event.UserActivated;
import com.alertmns.iam.domain.event.UserReactivated;
import com.alertmns.iam.domain.event.UserRegistered;
import com.alertmns.iam.domain.event.UserSuspended;
import com.alertmns.iam.domain.exception.BannedUserCannotBeReactivatedException;
import com.alertmns.shared.DomainEvent;
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
            User user = User.register(email, hashedPassword, profile);
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

        User user;

        @BeforeEach
        void setUp() {
            Email email = Email.of("johndoe@example.com");
            HashedPassword hashedPassword = HashedPassword.of(BCRYPT_HASH);
            Profile profile = Profile.of(FirstName.of("John"), LastName.of("Doe"));
            user = User.register(email, hashedPassword, profile);
        }

        @Test
        @DisplayName("updateProfile should update the user profile")
        void updateProfileShouldUpdateTheUserProfile() {
            user.activateWithPassword(HashedPassword.of(BCRYPT_HASH));
            FirstName newFirstName = FirstName.of("Jane");
            LastName newLastName = LastName.of("Smith");
            String newAvatar = "https://cdn.example.com/avatar.jpg";
            user.updateProfile(newFirstName, newLastName, newAvatar);
            assertEquals(newFirstName, user.profile().firstName());
            assertEquals(newLastName, user.profile().lastName());
            assertTrue(user.profile().avatar().isPresent());
            assertEquals(newAvatar, user.profile().avatar().orElseThrow());
        }

        @Test
        @DisplayName("updateAbsenceMessage should update the absence message")
        void updateAbsenceMessageShouldUpdateTheAbsenceMessage() {
            user.activateWithPassword(HashedPassword.of(BCRYPT_HASH));
            AbsenceMessage absenceMessage = AbsenceMessage.of(
                    "Je ne suis pas disponible pour le moment", true
            );

            user.updateAbsenceMessage(absenceMessage);
            assertTrue(user.profile().absenceMessage().isPresent());
            assertEquals(absenceMessage, user.profile().absenceMessage().orElseThrow());
        }

        @Test
        @DisplayName("updateProfile should reject non-ACTIVE account")
        void updateProfileShouldRejectNonACTIVEAccount() {
            assertThrows(IllegalStateException.class,
                    () -> user.updateProfile(FirstName.of("Jane"), LastName.of("Smith"), null));
        }

        @Test
        @DisplayName("updateAbsenceMessage should reject non-ACTIVE account")
        void updateAbsenceMessageShouldRejectNonACTIVEAccount() {
            assertThrows(IllegalStateException.class,
                    () -> user.updateAbsenceMessage(AbsenceMessage.of("Absent", true)));
        }

        @Test
        @DisplayName("activateWithPassword should transition PENDING to ACTIVE")
        void activateWithPasswordShouldTransitionPENDINGToACTIVE() {
            user.activateWithPassword(HashedPassword.of(BCRYPT_HASH));
            assertEquals(UserStatus.ACTIVE, user.status());
        }

        @Test
        @DisplayName("activateWithPassword should replace the hashed password")
        void activateWithPasswordShouldReplaceTheHashedPassword() {
            String newHash = "$2a$10$zzzzzzzzzzzzzzzzzzzzzzZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ987654";
            HashedPassword newHashedPassword = HashedPassword.of(newHash);

            user.activateWithPassword(newHashedPassword);

            assertEquals(newHashedPassword, user.hashedPassword());
        }

        @Test
        @DisplayName("activateWithPassword should reject null hashedPassword")
        void activateWithPasswordShouldRejectNullHashedPassword() {
            assertThrows(NullPointerException.class, () -> user.activateWithPassword(null));
        }

        @Test
        @DisplayName("activateWithPassword should reject non-PENDING account")
        void activateWithPasswordShouldRejectNonPENDINGAccount() {
            user.activateWithPassword(HashedPassword.of(BCRYPT_HASH));
            assertThrows(IllegalStateException.class,
                    () -> user.activateWithPassword(HashedPassword.of(BCRYPT_HASH)));
        }

        @Test
        @DisplayName("suspend should transition ACTIVE to SUSPENDED")
        void suspendShouldTransitionACTIVEToSUSPENDED() {
            user.activateWithPassword(HashedPassword.of(BCRYPT_HASH));
            user.suspend();
            assertEquals(UserStatus.SUSPENDED, user.status());
        }

        @Test
        @DisplayName("suspend should reject non-ACTIVE account")
        void suspendShouldRejectNonACTIVEAccount() {
            assertThrows(IllegalStateException.class, () -> user.suspend());
        }

        @Test
        @DisplayName("reactivate should transition SUSPENDED to ACTIVE")
        void reactivateShouldTransitionSUSPENDEDToACTIVE() {
            user.activateWithPassword(HashedPassword.of(BCRYPT_HASH));
            user.suspend();
            user.reactivate();
            assertEquals(UserStatus.ACTIVE, user.status());
        }

        @Test
        @DisplayName("reactivate should reject non-SUSPENDED account")
        void reactivateShouldRejectNonSUSPENDEDAccount() {
            assertThrows(IllegalStateException.class, () -> user.reactivate());
        }

        @Test
        @DisplayName("reactivate should reject BANNED account with named domain exception")
        void reactivateShouldRejectBANNEDAccount() {
            User bannedUser = User.reconstitute(
                    UserId.generate(),
                    Email.of("banned@example.com"),
                    HashedPassword.of(BCRYPT_HASH),
                    Profile.of(FirstName.of("John"), LastName.of("Doe")),
                    UserStatus.BANNED,
                    false,
                    Instant.now()
            );

            assertThrows(BannedUserCannotBeReactivatedException.class, bannedUser::reactivate);
            assertEquals(UserStatus.BANNED, bannedUser.status());
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
            user = User.register(email, hashedPassword, profile);
        }

        @Test
        @DisplayName("register should emit UserRegistered")
        void registerShouldEmitUserRegistered() {
            List<DomainEvent> events = user.pullDomainEvents();
            assertEquals(1, events.size());
            UserRegistered event = assertInstanceOf(UserRegistered.class, events.getFirst());
            assertEquals(user.id(), event.userId());
            assertEquals(user.email(), event.email());
        }

        @Test
        @DisplayName("updateProfile should emit ProfileUpdated")
        void updateProfileShouldEmitUpdateProfile() {
            user.activateWithPassword(HashedPassword.of(BCRYPT_HASH));
            user.pullDomainEvents();

            user.updateProfile(
                    FirstName.of("Jane"),
                    LastName.of("Doe"),
                    "https://cdn.example.com/avatar.jpg"
            );

            List<DomainEvent> events = user.pullDomainEvents();
            assertEquals(1, events.size());
            ProfileUpdated event = assertInstanceOf(ProfileUpdated.class, events.getFirst());
            assertEquals(user.id(), event.userId());
        }

        @Test
        @DisplayName("updateAbsenceMessage should emit AbsenceMessageUpdated")
        void updateAbsenceMessageShouldEmitUpdateAbsenceMessage() {
            user.activateWithPassword(HashedPassword.of(BCRYPT_HASH));
            user.pullDomainEvents();

            user.updateAbsenceMessage(AbsenceMessage.of(
                    "je ne suis pas disponible pour le moment",
                    true
            ));

            List<DomainEvent> events = user.pullDomainEvents();
            assertEquals(1, events.size());
            AbsenceMessageUpdated event = assertInstanceOf(AbsenceMessageUpdated.class, events.getFirst());
            assertEquals(user.id(), event.userId());
        }

        @Test
        @DisplayName("activateWithPassword should emit UserActivated")
        void activateWithPasswordShouldEmitUserActivated() {
            user.pullDomainEvents();
            user.activateWithPassword(HashedPassword.of(BCRYPT_HASH));
            List<DomainEvent> events = user.pullDomainEvents();
            assertEquals(1, events.size());
            UserActivated event = assertInstanceOf(UserActivated.class, events.getFirst());
            assertEquals(user.id(), event.userId());
        }

        @Test
        @DisplayName("suspend should emit UserSuspended")
        void suspendShouldEmitUserSuspended() {
            user.pullDomainEvents();
            user.activateWithPassword(HashedPassword.of(BCRYPT_HASH));
            user.pullDomainEvents();
            user.suspend();
            List<DomainEvent> events = user.pullDomainEvents();
            assertEquals(1, events.size());
            UserSuspended event = assertInstanceOf(UserSuspended.class, events.getFirst());
            assertEquals(user.id(), event.userId());
        }

        @Test
        @DisplayName("reactivate should emit UserReactivated")
        void reactivateShouldEmitUserReactivated() {
            user.pullDomainEvents();
            user.activateWithPassword(HashedPassword.of(BCRYPT_HASH));
            user.pullDomainEvents();
            user.suspend();
            user.pullDomainEvents();
            user.reactivate();
            List<DomainEvent> events = user.pullDomainEvents();
            assertEquals(1, events.size());
            UserReactivated event = assertInstanceOf(UserReactivated.class, events.getFirst());
            assertEquals(user.id(), event.userId());
        }

        @Test
        @DisplayName("reactivate on BANNED account should not emit any event")
        void reactivateOnBANNEDShouldNotEmitAnyEvent() {
            User bannedUser = User.reconstitute(
                    UserId.generate(),
                    Email.of("banned@example.com"),
                    HashedPassword.of(BCRYPT_HASH),
                    Profile.of(FirstName.of("John"), LastName.of("Doe")),
                    UserStatus.BANNED,
                    false,
                    Instant.now()
            );

            assertThrows(BannedUserCannotBeReactivatedException.class, bannedUser::reactivate);
            assertTrue(bannedUser.pullDomainEvents().isEmpty());
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
                    id,
                    email,
                    hashedPassword,
                    profile,
                    UserStatus.ACTIVE,
                    false,
                    createdAt
            );

            User user2 = User.reconstitute(
                    id,
                    email,
                    hashedPassword,
                    profile,
                    UserStatus.ACTIVE,
                    false,
                    createdAt
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
