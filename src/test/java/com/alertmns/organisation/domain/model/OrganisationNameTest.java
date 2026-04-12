package com.alertmns.organisation.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("OrganisationName")
class OrganisationNameTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create an organisation name")
        void shouldCreateAnOrganisationName() {
            String value = "Metz Numeric School";
            OrganisationName name = OrganisationName.of(value);
            assertEquals("Metz Numeric School", name.value());
        }

        @Test
        @DisplayName("should normalize organisation name by trimming spaces")
        void shouldNormalizeOrganisationName() {
            String value = " Metz Numeric School ";
            OrganisationName name = OrganisationName.of(value);
            assertEquals("Metz Numeric School", name.value());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null organisation name")
        void shouldRejectNullOrganisationName() {
            String value = null;
            assertThrows(NullPointerException.class, () -> OrganisationName.of(value));
        }

        @Test
        @DisplayName("should reject blank organisation name")
        void shouldRejectBlankOrganisationName() {
            String value = " ";
            assertThrows(IllegalArgumentException.class, () -> OrganisationName.of(value));
        }

        @Test
        @DisplayName("should accept organisation name with max length")
        void shouldAcceptOrganisationNameWithMaxLength() {
            String value = "a".repeat(150);
            OrganisationName name = OrganisationName.of(value);
            assertEquals(value, name.value());
        }

        @Test
        @DisplayName("should reject organisation name exceeding max length")
        void shouldRejectOrganisationNameExceedingMaxLength() {
            String value = "a".repeat(151);
            assertThrows(IllegalArgumentException.class, () -> OrganisationName.of(value));
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("two organisation names with same value should be equal")
        void twoOrganisationNamesWithSameValueShouldBeEqual() {
            OrganisationName name1 = OrganisationName.of("Metz Numeric School");
            OrganisationName name2 = OrganisationName.of("Metz Numeric School");
            assertEquals(name1, name2);
        }

        @Test
        @DisplayName("two organisation names with different values should not be equal")
        void twoOrganisationNamesWithDifferentValuesShouldNotBeEqual() {
            OrganisationName name1 = OrganisationName.of("Metz Numeric School");
            OrganisationName name2 = OrganisationName.of("Paris Numeric School");
            assertNotEquals(name1, name2);
        }
    }
}
