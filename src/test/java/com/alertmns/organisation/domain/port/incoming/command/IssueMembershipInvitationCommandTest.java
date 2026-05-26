package com.alertmns.organisation.domain.port.incoming.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("IssueMembershipInvitationCommand")
class IssueMembershipInvitationCommandTest {

    static final String ORGANISATION_ID = "00000000-0000-0000-0000-000000000001";
    static final String INVITED_EMAIL = "invited@example.com";
    static final String FIRST_NAME = "Alice";
    static final String LAST_NAME = "Doe";
    static final String ROLE = "MEMBER";

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should expose all 5 fields as provided")
        void shouldExposeAllFieldsAsProvided() {
            IssueMembershipInvitationCommand command = new IssueMembershipInvitationCommand(
                    ORGANISATION_ID, INVITED_EMAIL, FIRST_NAME, LAST_NAME, ROLE);

            assertThat(command.organisationId()).isEqualTo(ORGANISATION_ID);
            assertThat(command.invitedEmail()).isEqualTo(INVITED_EMAIL);
            assertThat(command.firstName()).isEqualTo(FIRST_NAME);
            assertThat(command.lastName()).isEqualTo(LAST_NAME);
            assertThat(command.role()).isEqualTo(ROLE);
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null organisationId")
        void shouldRejectNullOrganisationId() {
            assertThrows(NullPointerException.class,
                    () -> new IssueMembershipInvitationCommand(
                            null, INVITED_EMAIL, FIRST_NAME, LAST_NAME, ROLE));
        }

        @Test
        @DisplayName("should reject null invitedEmail")
        void shouldRejectNullInvitedEmail() {
            assertThrows(NullPointerException.class,
                    () -> new IssueMembershipInvitationCommand(
                            ORGANISATION_ID, null, FIRST_NAME, LAST_NAME, ROLE));
        }

        @Test
        @DisplayName("should reject null firstName")
        void shouldRejectNullFirstName() {
            assertThrows(NullPointerException.class,
                    () -> new IssueMembershipInvitationCommand(
                            ORGANISATION_ID, INVITED_EMAIL, null, LAST_NAME, ROLE));
        }

        @Test
        @DisplayName("should reject null lastName")
        void shouldRejectNullLastName() {
            assertThrows(NullPointerException.class,
                    () -> new IssueMembershipInvitationCommand(
                            ORGANISATION_ID, INVITED_EMAIL, FIRST_NAME, null, ROLE));
        }

        @Test
        @DisplayName("should reject null role")
        void shouldRejectNullRole() {
            assertThrows(NullPointerException.class,
                    () -> new IssueMembershipInvitationCommand(
                            ORGANISATION_ID, INVITED_EMAIL, FIRST_NAME, LAST_NAME, null));
        }
    }
}
