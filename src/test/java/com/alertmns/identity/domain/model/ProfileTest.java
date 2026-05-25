package com.alertmns.identity.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Profile")
class ProfileTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create a minimal profile (firstName + lastName only)")
        void shouldCreateAMinimalProfile() {
            FirstName firstName = FirstName.of("John");
            LastName lastName = LastName.of("Doe");
            Profile profile = Profile.of(firstName, lastName);
            assertEquals(firstName, profile.firstName());
            assertEquals(lastName, profile.lastName());
            assertTrue(profile.avatar().isEmpty());
            assertTrue(profile.absenceMessage().isEmpty());
        }

        @Test
        @DisplayName("should create a profile with avatar and absence message")
        void shouldCreateAProfileWithAvatarAndAbsenceMessage() {
            FirstName firstName = FirstName.of("John");
            LastName lastName = LastName.of("Doe");
            String avatar = "https://cdn.example.com/avatar.jpg";

            AbsenceMessage absenceMessage = AbsenceMessage.of(
                    "Je ne suis pas disponible pour le moment", true
            );

            Profile profile = Profile.of(firstName, lastName, avatar)
                    .withAbsenceMessage(absenceMessage);

            assertEquals(firstName, profile.firstName());
            assertEquals(lastName, profile.lastName());
            assertTrue(profile.avatar().isPresent());
            assertTrue(profile.absenceMessage().isPresent());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null first name")
        void shouldRejectNullFirstName() {
            LastName lastName = LastName.of("Doe");
            assertThrows(NullPointerException.class, () -> Profile.of(null, lastName));
        }

        @Test
        @DisplayName("should reject null last name")
        void shouldRejectNullLastName() {
            FirstName firstName = FirstName.of("John");
            assertThrows(NullPointerException.class, () -> Profile.of(firstName, null));
        }
    }

    @Nested
    @DisplayName("Behaviour")
    class Behaviour {

        @Test
        @DisplayName("withIdentity should return a new profile preserving absence message")
        void withIdentityShouldReturnANewProfilePreservingAbsenceMessage() {
            FirstName firstName = FirstName.of("John");
            LastName lastName = LastName.of("Doe");
            AbsenceMessage absenceMessage = AbsenceMessage.of("Absent", true);

            Profile profile = Profile.of(firstName, lastName)
                    .withAbsenceMessage(absenceMessage);

            FirstName newFirstName = FirstName.of("Jane");
            String avatar = "https://cdn.example.com/avatar.jpg";
            Profile updated = profile.withIdentity(newFirstName, lastName, avatar);

            assertNotSame(profile, updated);
            assertEquals(newFirstName, updated.firstName());
            assertEquals(avatar, updated.avatar().orElseThrow());
            assertTrue(updated.absenceMessage().isPresent());
            assertEquals(absenceMessage, updated.absenceMessage().orElseThrow());
        }

        @Test
        @DisplayName("withAbsenceMessage should return a new profile with absence message")
        void withAbsenceMessageShouldReturnANewProfileWithAbsenceMessage() {
            FirstName firstName = FirstName.of("John");
            LastName lastName = LastName.of("Doe");

            AbsenceMessage absenceMessage = AbsenceMessage.of(
                    "Je ne suis pas disponible pour le moment", true
            );

            Profile profile = Profile.of(firstName, lastName);
            Profile profileWithAbsenceMessage = profile.withAbsenceMessage(absenceMessage);
            assertNotSame(profile, profileWithAbsenceMessage);
            assertTrue(profile.absenceMessage().isEmpty());
            assertTrue(profileWithAbsenceMessage.absenceMessage().isPresent());
            assertEquals(absenceMessage, profileWithAbsenceMessage.absenceMessage().orElseThrow());
        }

        @Test
        @DisplayName("activateAbsenceMessage should return a new profile with active absence message")
        void activateAbsenceMessageShouldReturnANewProfileWithActiveAbsenceMessage() {
            FirstName firstName = FirstName.of("John");
            LastName lastName = LastName.of("Doe");

            AbsenceMessage absenceMessage = AbsenceMessage.of(
                    "Je ne suis pas disponible pour le moment", false
            );

            Profile profile = Profile.of(firstName, lastName);

            Profile profileWithAbsenceMessage = profile
                    .withAbsenceMessage(absenceMessage)
                    .activateAbsenceMessage();

            assertNotSame(profile, profileWithAbsenceMessage);
            assertTrue(profile.absenceMessage().isEmpty());
            assertTrue(profileWithAbsenceMessage.absenceMessage().isPresent());
            assertTrue(profileWithAbsenceMessage.absenceMessage().orElseThrow().active());
        }

        @Test
        @DisplayName("deactivateAbsenceMessage should return a new profile with inactive absence message")
        void deactivateAbsenceMessageShouldReturnANewProfileWithInactiveAbsenceMessage() {
            FirstName firstName = FirstName.of("John");
            LastName lastName = LastName.of("Doe");

            AbsenceMessage absenceMessage = AbsenceMessage.of(
                    "Je ne suis pas disponible pour le moment", true
            );

            Profile profile = Profile.of(firstName, lastName);

            Profile profileWithAbsenceMessage = profile
                    .withAbsenceMessage(absenceMessage)
                    .deactivateAbsenceMessage();

            assertNotSame(profile, profileWithAbsenceMessage);
            assertTrue(profile.absenceMessage().isEmpty());
            assertTrue(profileWithAbsenceMessage.absenceMessage().isPresent());
            assertFalse(profileWithAbsenceMessage.absenceMessage().orElseThrow().active());
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("two identical profiles should be equal")
        void twoIdenticalProfilesShouldBeEqual() {
            Profile profile1 = Profile.of(FirstName.of("John"), LastName.of("Doe"));
            Profile profile2 = Profile.of(FirstName.of("John"), LastName.of("Doe"));
            assertEquals(profile1, profile2);
        }

        @Test
        @DisplayName("two profiles with different first names should not be equal")
        void twoProfilesWithDifferentFirstNamesShouldNotBeEqual() {
            Profile profile1 = Profile.of(FirstName.of("John"), LastName.of("Doe"));
            Profile profile2 = Profile.of(FirstName.of("Jane"), LastName.of("Doe"));
            assertNotEquals(profile1, profile2);
        }
    }
}
