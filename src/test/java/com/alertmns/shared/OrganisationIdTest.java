package com.alertmns.shared;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrganisationId")
class OrganisationIdTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should generate a unique OrganisationId")
        void shouldGenerateAUniqueOrganisationId() {
            OrganisationId organisationId1 = OrganisationId.generate();
            OrganisationId organisationId2 = OrganisationId.generate();
            assertNotEquals(organisationId1, organisationId2);
        }

        @Test
        @DisplayName("should reconstruct OrganisationId from a valid UUID string")
        void shouldReconstructOrganisationIdFromString() {
            String raw = "cb9e42dd-ee85-4cef-ac7b-f9dc652253c5";
            OrganisationId organisationId = OrganisationId.from(raw);
            assertEquals(UUID.fromString(raw), organisationId.value());
        }

        @Test
        @DisplayName("should create OrganisationId from a valid UUID")
        void shouldCreateOrganisationIdFromAValidUUID() {
            UUID uuid = UUID.randomUUID();
            OrganisationId organisationId = OrganisationId.from(uuid);
            assertEquals(uuid, organisationId.value());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null UUID")
        void shouldRejectNullUUID() {
            String raw = null;
            assertThrows(NullPointerException.class, () -> OrganisationId.from(raw));
        }

        @Test
        @DisplayName("should reject invalid UUID string")
        void shouldRejectInvalidUUID() {
            String raw = "invalid";
            assertThrows(IllegalArgumentException.class, () -> OrganisationId.from(raw));
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("two OrganisationIds with same UUID should be equal")
        void twoOrganisationIdsWithSameUUIDShouldBeEqual() {
            String raw = "cb9e42dd-ee85-4cef-ac7b-f9dc652253c5";
            OrganisationId organisationId1 = OrganisationId.from(raw);
            OrganisationId organisationId2 = OrganisationId.from(raw);
            assertEquals(organisationId1, organisationId2);
        }

        @Test
        @DisplayName("two OrganisationIds with different UUIDs should not be equal")
        void twoOrganisationIdsWithDifferentUUIDsShouldNotBeEqual() {
            String raw1 = "cb9e42dd-ee85-4cef-ac7b-f9dc652253c5";
            String raw2 = "72ce2342-1d41-4e0b-b4b8-6e88891e9add";
            OrganisationId organisationId1 = OrganisationId.from(raw1);
            OrganisationId organisationId2 = OrganisationId.from(raw2);
            assertNotEquals(organisationId1, organisationId2);
        }
    }
}
