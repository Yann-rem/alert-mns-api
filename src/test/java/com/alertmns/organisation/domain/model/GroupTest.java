package com.alertmns.organisation.domain.model;

import com.alertmns.organisation.domain.event.GroupCreated;
import com.alertmns.organisation.domain.event.GroupRenamed;
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

@DisplayName("Group")
class GroupTest {

    static final OrganisationId ORGANISATION_ID = OrganisationId.generate();
    static final GroupName NAME = GroupName.of("Développeurs");

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create a new group")
        void shouldCreateANewGroup() {
            Group group = Group.create(ORGANISATION_ID, NAME);
            assertEquals(NAME, group.name());
            assertEquals(ORGANISATION_ID, group.organisationId());
            assertNotNull(group.id());
            assertNotNull(group.createdAt());
        }

        @Test
        @DisplayName("should reconstitute an existing group")
        void shouldReconstituteAnExistingGroup() {
            GroupId id = GroupId.generate();
            Instant createdAt = Instant.now();

            Group group = Group.reconstitute(id, ORGANISATION_ID, NAME, createdAt);

            assertEquals(id, group.id());
            assertEquals(NAME, group.name());
            assertEquals(ORGANISATION_ID, group.organisationId());
            assertEquals(createdAt, group.createdAt());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null name")
        void shouldRejectNullName() {
            assertThrows(NullPointerException.class,
                    () -> Group.create(ORGANISATION_ID, null));
        }

        @Test
        @DisplayName("should reject null organisationId")
        void shouldRejectNullOrganisationId() {
            assertThrows(NullPointerException.class,
                    () -> Group.create(null, NAME));
        }

        @Test
        @DisplayName("should reject null name on reconstitute")
        void shouldRejectNullNameOnReconstitute() {
            GroupId id = GroupId.generate();
            Instant createdAt = Instant.now();
            assertThrows(NullPointerException.class,
                    () -> Group.reconstitute(id, ORGANISATION_ID, null, createdAt));
        }

        @Test
        @DisplayName("should reject null id on reconstitute")
        void shouldRejectNullIdOnReconstitute() {
            Instant createdAt = Instant.now();
            assertThrows(NullPointerException.class,
                    () -> Group.reconstitute(null, ORGANISATION_ID, NAME, createdAt));
        }

        @Test
        @DisplayName("should reject null organisationId on reconstitute")
        void shouldRejectNullOrganisationIdOnReconstitute() {
            GroupId id = GroupId.generate();
            Instant createdAt = Instant.now();
            assertThrows(NullPointerException.class,
                    () -> Group.reconstitute(id, null, NAME, createdAt));
        }

        @Test
        @DisplayName("should reject null createdAt on reconstitute")
        void shouldRejectNullCreatedAtOnReconstitute() {
            GroupId id = GroupId.generate();
            assertThrows(NullPointerException.class,
                    () -> Group.reconstitute(id, ORGANISATION_ID, NAME, null));
        }
    }

    @Nested
    @DisplayName("Behaviour")
    class Behaviour {

        @Test
        @DisplayName("rename should update the group name")
        void renameShouldUpdateTheGroupName() {
            Group group = Group.create(ORGANISATION_ID, NAME);
            GroupName newName = GroupName.of("Designers");
            group.rename(newName);
            assertEquals(newName, group.name());
        }

        @Test
        @DisplayName("rename should not change the name when already unchanged")
        void renameShouldNotChangeTheNameWhenAlreadyUnchanged() {
            Group group = Group.create(ORGANISATION_ID, NAME);
            group.rename(NAME);
            assertEquals(NAME, group.name());
        }

        @Test
        @DisplayName("rename should reject null name")
        void renameShouldRejectNullName() {
            Group group = Group.create(ORGANISATION_ID, NAME);
            assertThrows(NullPointerException.class, () -> group.rename(null));
        }
    }

    @Nested
    @DisplayName("Domain Events")
    class DomainEvents {

        @Test
        @DisplayName("create should emit GroupCreated")
        void createShouldEmitGroupCreated() {
            Group group = Group.create(ORGANISATION_ID, NAME);

            List<DomainEvent> events = group.pullDomainEvents();
            assertEquals(1, events.size());
            GroupCreated event = assertInstanceOf(GroupCreated.class, events.getFirst());
            assertEquals(group.id(), event.groupId());
            assertEquals(group.name(), event.name());
            assertEquals(group.organisationId(), event.organisationId());
        }

        @Test
        @DisplayName("rename should emit GroupRenamed")
        void renameShouldEmitGroupRenamed() {
            Group group = Group.create(ORGANISATION_ID, NAME);
            group.pullDomainEvents();

            GroupName newName = GroupName.of("Designers");
            group.rename(newName);

            List<DomainEvent> events = group.pullDomainEvents();
            assertEquals(1, events.size());
            GroupRenamed event = assertInstanceOf(GroupRenamed.class, events.getFirst());
            assertEquals(ORGANISATION_ID, event.organisationId());
            assertEquals(group.id(), event.groupId());
            assertEquals(newName, event.name());
        }

        @Test
        @DisplayName("rename should not emit any event when name is unchanged")
        void renameShouldNotEmitAnyEventWhenNameIsUnchanged() {
            Group group = Group.create(ORGANISATION_ID, NAME);
            group.pullDomainEvents();

            group.rename(NAME);

            List<DomainEvent> events = group.pullDomainEvents();
            assertTrue(events.isEmpty());
        }

        @Test
        @DisplayName("reconstitute should not emit any event")
        void reconstituteShouldNotEmitAnyEvent() {
            GroupId id = GroupId.generate();
            Instant createdAt = Instant.now();

            Group group = Group.reconstitute(id, ORGANISATION_ID, NAME, createdAt);

            List<DomainEvent> events = group.pullDomainEvents();
            assertTrue(events.isEmpty());
        }

        @Test
        @DisplayName("pullDomainEvents should clear events after pull")
        void pullDomainEventsShouldClearEventsAfterPull() {
            Group group = Group.create(ORGANISATION_ID, NAME);
            group.pullDomainEvents();
            List<DomainEvent> events = group.pullDomainEvents();
            assertTrue(events.isEmpty());
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("two groups with same id should be equal")
        void twoGroupsWithSameIdShouldBeEqual() {
            GroupId id = GroupId.generate();
            Instant createdAt = Instant.now();

            Group group1 = Group.reconstitute(id, ORGANISATION_ID, NAME, createdAt);
            Group group2 = Group.reconstitute(id, ORGANISATION_ID, NAME, createdAt);

            assertEquals(group1, group2);
        }

        @Test
        @DisplayName("two groups with different ids should not be equal")
        void twoGroupsWithDifferentIdsShouldNotBeEqual() {
            Group group1 = Group.create(ORGANISATION_ID, NAME);
            Group group2 = Group.create(ORGANISATION_ID, NAME);
            assertNotEquals(group1, group2);
        }
    }
}
