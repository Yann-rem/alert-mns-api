package com.alertmns.alerting.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("AlertAudience")
class AlertAudienceTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("organisation should target the whole organisation with no group")
        void organisationTargetsWholeOrg() {
            AlertAudience audience = AlertAudience.organisation();

            assertEquals(AlertAudienceKind.ORGANISATION, audience.kind());
            assertNull(audience.groupId());
        }

        @Test
        @DisplayName("group should target the given group")
        void groupTargetsGroup() {
            UUID groupId = UUID.randomUUID();

            AlertAudience audience = AlertAudience.group(groupId);

            assertEquals(AlertAudienceKind.GROUP, audience.kind());
            assertEquals(groupId, audience.groupId());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject a null kind")
        void rejectsNullKind() {
            assertThrows(NullPointerException.class, () -> new AlertAudience(null, null));
        }

        @Test
        @DisplayName("should reject a GROUP audience without a groupId")
        void rejectsGroupWithoutGroupId() {
            assertThrows(NullPointerException.class,
                    () -> new AlertAudience(AlertAudienceKind.GROUP, null));
        }

        @Test
        @DisplayName("should reject an ORGANISATION audience carrying a groupId")
        void rejectsOrganisationWithGroupId() {
            assertThrows(IllegalArgumentException.class,
                    () -> new AlertAudience(AlertAudienceKind.ORGANISATION, UUID.randomUUID()));
        }
    }
}
