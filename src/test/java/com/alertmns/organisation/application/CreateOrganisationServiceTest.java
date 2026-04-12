package com.alertmns.organisation.application;

import com.alertmns.organisation.domain.exception.OrganisationNameAlreadyExistsException;
import com.alertmns.organisation.domain.model.Organisation;
import com.alertmns.organisation.domain.port.incoming.command.CreateOrganisationCommand;
import com.alertmns.organisation.domain.port.outgoing.OrganisationRepository;
import com.alertmns.shared.EventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("CreateOrganisationService")
@ExtendWith(MockitoExtension.class)
class CreateOrganisationServiceTest {

    @Mock
    OrganisationRepository repository;

    @Mock
    EventPublisher publisher;

    @InjectMocks
    CreateOrganisationService service;

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create an organisation")
        void shouldCreateAnOrganisation() {
            when(repository.existsByName(any())).thenReturn(false);

            CreateOrganisationCommand command = new CreateOrganisationCommand("Metz Numeric School");

            service.create(command);
            verify(repository).save(any(Organisation.class));
            verify(publisher).publish(anyList());
        }

        @Test
        @DisplayName("should throw OrganisationNameAlreadyExistsException when name already exists")
        void shouldThrowOrganisationNameAlreadyExistsExceptionWhenNameAlreadyExists() {
            when(repository.existsByName(any())).thenReturn(true);

            CreateOrganisationCommand command = new CreateOrganisationCommand("Metz Numeric School");

            assertThrows(OrganisationNameAlreadyExistsException.class, () -> service.create(command));
            verify(repository, never()).save(any());
            verify(publisher, never()).publish(anyList());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null repository")
        void shouldRejectNullRepository() {
            assertThrows(NullPointerException.class,
                    () -> new CreateOrganisationService(null, publisher));
        }

        @Test
        @DisplayName("should reject null publisher")
        void shouldRejectNullPublisher() {
            assertThrows(NullPointerException.class,
                    () -> new CreateOrganisationService(repository, null));
        }
    }
}
