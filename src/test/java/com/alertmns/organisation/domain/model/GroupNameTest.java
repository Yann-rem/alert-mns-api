package com.alertmns.organisation.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("GroupName")
class GroupNameTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create a group name")
        void shouldCreateAGroupName() {
            String value = "Développeurs";
            GroupName name = GroupName.of(value);
            assertEquals("Développeurs", name.value());
        }

        @Test
        @DisplayName("should normalize group name by trimming spaces")
        void shouldNormalizeGroupName() {
            String value = " Développeurs ";
            GroupName name = GroupName.of(value);
            assertEquals("Développeurs", name.value());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null group name")
        void shouldRejectNullGroupName() {
            String value = null;
            assertThrows(NullPointerException.class, () -> GroupName.of(value));
        }

        @Test
        @DisplayName("should reject blank group name")
        void shouldRejectBlankGroupName() {
            String value = " ";
            assertThrows(IllegalArgumentException.class, () -> GroupName.of(value));
        }

        @Test
        @DisplayName("should accept group name with max length")
        void shouldAcceptGroupNameWithMaxLength() {
            String value = "a".repeat(150);
            GroupName name = GroupName.of(value);
            assertEquals(value, name.value());
        }

        @Test
        @DisplayName("should reject group name exceeding max length")
        void shouldRejectGroupNameExceedingMaxLength() {
            String value = "a".repeat(151);
            assertThrows(IllegalArgumentException.class, () -> GroupName.of(value));
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("two group names with same value should be equal")
        void twoGroupNamesWithSameValueShouldBeEqual() {
            GroupName name1 = GroupName.of("Développeurs");
            GroupName name2 = GroupName.of("Développeurs");
            assertEquals(name1, name2);
        }

        @Test
        @DisplayName("two group names with different values should not be equal")
        void twoGroupNamesWithDifferentValuesShouldNotBeEqual() {
            GroupName name1 = GroupName.of("Développeurs");
            GroupName name2 = GroupName.of("Designers");
            assertNotEquals(name1, name2);
        }
    }
}
