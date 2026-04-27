package com.alertmns.organisation.domain.model;

import com.alertmns.organisation.domain.event.OrganisationCreated;
import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.OrganisationId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Organisation")
class OrganisationTest {

    static final OrganisationName NAME = OrganisationName.of("Metz Numeric School");

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create a new organisation")
        void shouldCreateANewOrganisation() {
            Organisation organisation = Organisation.create(NAME);
            assertEquals(NAME, organisation.name());
            assertNotNull(organisation.id());
            assertNotNull(organisation.createdAt());
        }

        @Test
        @DisplayName("should reconstitute an existing organisation")
        void shouldReconstituteAnExistingOrganisation() {
            OrganisationId id = OrganisationId.generate();
            Instant createdAt = Instant.now();

            Organisation organisation = Organisation.reconstitute(id, NAME, createdAt);

            assertEquals(id, organisation.id());
            assertEquals(NAME, organisation.name());
            assertEquals(createdAt, organisation.createdAt());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null name")
        void shouldRejectNullName() {
            assertThrows(NullPointerException.class,
                    () -> Organisation.create(null));
        }

        @Test
        @DisplayName("should reject null id on reconstitute")
        void shouldRejectNullIdOnReconstitute() {
            Instant createdAt = Instant.now();
            assertThrows(NullPointerException.class,
                    () -> Organisation.reconstitute(null, NAME, createdAt));
        }

        @Test
        @DisplayName("should reject null name on reconstitute")
        void shouldRejectNullNameOnReconstitute() {
            OrganisationId id = OrganisationId.generate();
            Instant createdAt = Instant.now();
            assertThrows(NullPointerException.class,
                    () -> Organisation.reconstitute(id, null, createdAt));
        }

        @Test
        @DisplayName("should reject null createdAt on reconstitute")
        void shouldRejectNullCreatedAtOnReconstitute() {
            OrganisationId id = OrganisationId.generate();
            assertThrows(NullPointerException.class,
                    () -> Organisation.reconstitute(id, NAME, null));
        }
    }

    @Nested
    @DisplayName("Domain Events")
    class DomainEvents {

        @Test
        @DisplayName("create should emit OrganisationCreated")
        void createShouldEmitOrganisationCreated() {
            Organisation organisation = Organisation.create(NAME);

            List<DomainEvent> events = organisation.pullDomainEvents();
            assertEquals(1, events.size());
            OrganisationCreated event = assertInstanceOf(OrganisationCreated.class, events.getFirst());
            assertEquals(organisation.id(), event.organisationId());
        }

        @Test
        @DisplayName("reconstitute should not emit any event")
        void reconstituteShouldNotEmitAnyEvent() {
            OrganisationId id = OrganisationId.generate();
            Instant createdAt = Instant.now();

            Organisation organisation = Organisation.reconstitute(id, NAME, createdAt);

            List<DomainEvent> events = organisation.pullDomainEvents();
            assertTrue(events.isEmpty());
        }

        @Test
        @DisplayName("pullDomainEvents should clear events after pull")
        void pullDomainEventsShouldClearEventsAfterPull() {
            Organisation organisation = Organisation.create(NAME);
            organisation.pullDomainEvents();
            List<DomainEvent> events = organisation.pullDomainEvents();
            assertTrue(events.isEmpty());
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("two organisations with same id should be equal")
        void twoOrganisationsWithSameIdShouldBeEqual() {
            OrganisationId id = OrganisationId.generate();
            Instant createdAt = Instant.now();

            Organisation organisation1 = Organisation.reconstitute(id, NAME, createdAt);
            Organisation organisation2 = Organisation.reconstitute(id, NAME, createdAt);

            assertEquals(organisation1, organisation2);
        }

        @Test
        @DisplayName("two organisations with different ids should not be equal")
        void twoOrganisationsWithDifferentIdsShouldNotBeEqual() {
            Organisation organisation1 = Organisation.create(NAME);
            Organisation organisation2 = Organisation.create(NAME);
            assertNotEquals(organisation1, organisation2);
        }
    }
}
