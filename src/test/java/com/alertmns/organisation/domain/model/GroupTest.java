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
    static final Instant NOW = Instant.parse("2026-05-30T10:00:00Z");
    static final Instant LATER = NOW.plusSeconds(60);

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("createStandard should create a STANDARD group with createdAt = now")
        void shouldCreateAStandardGroup() {
            Group group = Group.createStandard(ORGANISATION_ID, NAME, NOW);

            assertEquals(NAME, group.name());
            assertEquals(ORGANISATION_ID, group.organisationId());
            assertEquals(GroupKind.STANDARD, group.kind());
            assertNotNull(group.id());
            assertEquals(NOW, group.createdAt());
        }

        @Test
        @DisplayName("createGeneral should create a GENERAL group with createdAt = now")
        void shouldCreateAGeneralGroup() {
            Group group = Group.createGeneral(ORGANISATION_ID, NAME, NOW);

            assertEquals(NAME, group.name());
            assertEquals(ORGANISATION_ID, group.organisationId());
            assertEquals(GroupKind.GENERAL, group.kind());
            assertNotNull(group.id());
            assertEquals(NOW, group.createdAt());
        }

        @Test
        @DisplayName("should reconstitute an existing group preserving its kind")
        void shouldReconstituteAnExistingGroup() {
            GroupId id = GroupId.generate();
            Instant createdAt = Instant.now();

            Group group = Group.reconstitute(id, ORGANISATION_ID, NAME, GroupKind.GENERAL, createdAt);

            assertEquals(id, group.id());
            assertEquals(NAME, group.name());
            assertEquals(ORGANISATION_ID, group.organisationId());
            assertEquals(GroupKind.GENERAL, group.kind());
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
                    () -> Group.createStandard(ORGANISATION_ID, null, NOW));
        }

        @Test
        @DisplayName("should reject null organisationId")
        void shouldRejectNullOrganisationId() {
            assertThrows(NullPointerException.class,
                    () -> Group.createStandard(null, NAME, NOW));
        }

        @Test
        @DisplayName("should reject null name on reconstitute")
        void shouldRejectNullNameOnReconstitute() {
            GroupId id = GroupId.generate();
            Instant createdAt = Instant.now();
            assertThrows(NullPointerException.class,
                    () -> Group.reconstitute(id, ORGANISATION_ID, null, GroupKind.STANDARD, createdAt));
        }

        @Test
        @DisplayName("should reject null id on reconstitute")
        void shouldRejectNullIdOnReconstitute() {
            Instant createdAt = Instant.now();
            assertThrows(NullPointerException.class,
                    () -> Group.reconstitute(null, ORGANISATION_ID, NAME, GroupKind.STANDARD, createdAt));
        }

        @Test
        @DisplayName("should reject null organisationId on reconstitute")
        void shouldRejectNullOrganisationIdOnReconstitute() {
            GroupId id = GroupId.generate();
            Instant createdAt = Instant.now();
            assertThrows(NullPointerException.class,
                    () -> Group.reconstitute(id, null, NAME, GroupKind.STANDARD, createdAt));
        }

        @Test
        @DisplayName("should reject null kind on reconstitute")
        void shouldRejectNullKindOnReconstitute() {
            GroupId id = GroupId.generate();
            Instant createdAt = Instant.now();
            assertThrows(NullPointerException.class,
                    () -> Group.reconstitute(id, ORGANISATION_ID, NAME, null, createdAt));
        }

        @Test
        @DisplayName("should reject null createdAt on reconstitute")
        void shouldRejectNullCreatedAtOnReconstitute() {
            GroupId id = GroupId.generate();
            assertThrows(NullPointerException.class,
                    () -> Group.reconstitute(id, ORGANISATION_ID, NAME, GroupKind.STANDARD, null));
        }
    }

    @Nested
    @DisplayName("Behaviour")
    class Behaviour {

        @Test
        @DisplayName("rename should update the group name")
        void renameShouldUpdateTheGroupName() {
            Group group = Group.createStandard(ORGANISATION_ID, NAME, NOW);
            GroupName newName = GroupName.of("Designers");
            group.rename(newName, LATER);
            assertEquals(newName, group.name());
        }

        @Test
        @DisplayName("rename should not change the name when already unchanged")
        void renameShouldNotChangeTheNameWhenAlreadyUnchanged() {
            Group group = Group.createStandard(ORGANISATION_ID, NAME, NOW);
            group.rename(NAME, LATER);
            assertEquals(NAME, group.name());
        }

        @Test
        @DisplayName("rename should reject null name")
        void renameShouldRejectNullName() {
            Group group = Group.createStandard(ORGANISATION_ID, NAME, NOW);
            assertThrows(NullPointerException.class, () -> group.rename(null, LATER));
        }

        @Test
        @DisplayName("rename should preserve the kind")
        void renameShouldPreserveTheKind() {
            Group group = Group.createGeneral(ORGANISATION_ID, NAME, NOW);
            group.rename(GroupName.of("Annonces"), LATER);
            assertEquals(GroupKind.GENERAL, group.kind());
        }
    }

    @Nested
    @DisplayName("Domain Events")
    class DomainEvents {

        @Test
        @DisplayName("createStandard should emit GroupCreated with kind STANDARD and occurredOn = now")
        void createStandardShouldEmitGroupCreated() {
            Group group = Group.createStandard(ORGANISATION_ID, NAME, NOW);

            List<DomainEvent> events = group.pullDomainEvents();
            assertEquals(1, events.size());
            GroupCreated event = assertInstanceOf(GroupCreated.class, events.getFirst());
            assertEquals(group.id(), event.groupId());
            assertEquals(group.name(), event.name());
            assertEquals(group.organisationId(), event.organisationId());
            assertEquals(GroupKind.STANDARD, event.kind());
            assertEquals(NOW, event.occurredOn());
        }

        @Test
        @DisplayName("createGeneral should emit GroupCreated with kind GENERAL and occurredOn = now")
        void createGeneralShouldEmitGroupCreated() {
            Group group = Group.createGeneral(ORGANISATION_ID, NAME, NOW);

            List<DomainEvent> events = group.pullDomainEvents();
            assertEquals(1, events.size());
            GroupCreated event = assertInstanceOf(GroupCreated.class, events.getFirst());
            assertEquals(group.id(), event.groupId());
            assertEquals(group.name(), event.name());
            assertEquals(group.organisationId(), event.organisationId());
            assertEquals(GroupKind.GENERAL, event.kind());
            assertEquals(NOW, event.occurredOn());
        }

        @Test
        @DisplayName("rename should emit GroupRenamed with occurredOn = now")
        void renameShouldEmitGroupRenamed() {
            Group group = Group.createStandard(ORGANISATION_ID, NAME, NOW);
            group.pullDomainEvents();

            GroupName newName = GroupName.of("Designers");
            group.rename(newName, LATER);

            List<DomainEvent> events = group.pullDomainEvents();
            assertEquals(1, events.size());
            GroupRenamed event = assertInstanceOf(GroupRenamed.class, events.getFirst());
            assertEquals(ORGANISATION_ID, event.organisationId());
            assertEquals(group.id(), event.groupId());
            assertEquals(newName, event.name());
            assertEquals(LATER, event.occurredOn());
        }

        @Test
        @DisplayName("rename should not emit any event when name is unchanged")
        void renameShouldNotEmitAnyEventWhenNameIsUnchanged() {
            Group group = Group.createStandard(ORGANISATION_ID, NAME, NOW);
            group.pullDomainEvents();

            group.rename(NAME, LATER);

            List<DomainEvent> events = group.pullDomainEvents();
            assertTrue(events.isEmpty());
        }

        @Test
        @DisplayName("reconstitute should not emit any event")
        void reconstituteShouldNotEmitAnyEvent() {
            GroupId id = GroupId.generate();

            Group group = Group.reconstitute(id, ORGANISATION_ID, NAME, GroupKind.STANDARD, NOW);

            List<DomainEvent> events = group.pullDomainEvents();
            assertTrue(events.isEmpty());
        }

        @Test
        @DisplayName("pullDomainEvents should clear events after pull")
        void pullDomainEventsShouldClearEventsAfterPull() {
            Group group = Group.createStandard(ORGANISATION_ID, NAME, NOW);
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

            Group group1 = Group.reconstitute(id, ORGANISATION_ID, NAME, GroupKind.STANDARD, createdAt);
            Group group2 = Group.reconstitute(id, ORGANISATION_ID, NAME, GroupKind.STANDARD, createdAt);

            assertEquals(group1, group2);
        }

        @Test
        @DisplayName("two groups with different ids should not be equal")
        void twoGroupsWithDifferentIdsShouldNotBeEqual() {
            Group group1 = Group.createStandard(ORGANISATION_ID, NAME, NOW);
            Group group2 = Group.createStandard(ORGANISATION_ID, NAME, NOW);
            assertNotEquals(group1, group2);
        }
    }
}
