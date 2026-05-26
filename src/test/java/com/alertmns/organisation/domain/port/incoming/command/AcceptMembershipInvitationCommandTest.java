package com.alertmns.organisation.domain.port.incoming.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("AcceptMembershipInvitationCommand")
class AcceptMembershipInvitationCommandTest {

    static final String INVITATION_ID = "11111111-1111-1111-1111-111111111111";
    static final String USER_ID = "22222222-2222-2222-2222-222222222222";

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should expose both fields as provided")
        void shouldExposeBothFieldsAsProvided() {
            AcceptMembershipInvitationCommand command =
                    new AcceptMembershipInvitationCommand(INVITATION_ID, USER_ID);

            assertThat(command.invitationId()).isEqualTo(INVITATION_ID);
            assertThat(command.userId()).isEqualTo(USER_ID);
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null invitationId")
        void shouldRejectNullInvitationId() {
            assertThrows(NullPointerException.class,
                    () -> new AcceptMembershipInvitationCommand(null, USER_ID));
        }

        @Test
        @DisplayName("should reject null userId")
        void shouldRejectNullUserId() {
            assertThrows(NullPointerException.class,
                    () -> new AcceptMembershipInvitationCommand(INVITATION_ID, null));
        }
    }
}
