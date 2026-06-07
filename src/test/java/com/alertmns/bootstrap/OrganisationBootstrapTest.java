package com.alertmns.bootstrap;

import com.alertmns.identity.domain.model.User;
import com.alertmns.identity.domain.model.UserStatus;
import com.alertmns.identity.domain.port.incoming.IssueActivationTokenUseCase;
import com.alertmns.identity.domain.port.incoming.command.IssueActivationTokenCommand;
import com.alertmns.identity.domain.port.outgoing.UserRepository;
import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.model.Organisation;
import com.alertmns.organisation.domain.port.incoming.CreateGeneralGroupUseCase;
import com.alertmns.organisation.domain.port.incoming.CreateOrganisationUseCase;
import com.alertmns.organisation.domain.port.incoming.IssueMembershipInvitationUseCase;
import com.alertmns.organisation.domain.port.incoming.command.CreateGeneralGroupCommand;
import com.alertmns.organisation.domain.port.incoming.command.CreateOrganisationCommand;
import com.alertmns.organisation.domain.port.incoming.command.IssueMembershipInvitationCommand;
import com.alertmns.organisation.domain.port.outgoing.OrganisationRepository;
import com.alertmns.shared.Email;
import com.alertmns.shared.MembershipInvitationId;
import com.alertmns.shared.OrganisationId;
import com.alertmns.shared.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("OrganisationBootstrap")
@ExtendWith(MockitoExtension.class)
class OrganisationBootstrapTest {

    private static final String ORG_NAME = "Metz Numeric School";
    private static final String GENERAL_GROUP_NAME = "Général";
    private static final String ADMIN_EMAIL = "admin@metz-numeric-school.com";
    private static final String ADMIN_FIRST_NAME = "Admin";
    private static final String ADMIN_LAST_NAME = "Système";
    private static final Email ADMIN_EMAIL_VO = Email.of(ADMIN_EMAIL);

    @Mock
    OrganisationRepository organisationRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    CreateOrganisationUseCase createOrganisation;

    @Mock
    CreateGeneralGroupUseCase createGeneralGroup;

    @Mock
    IssueMembershipInvitationUseCase issueMembershipInvitation;

    @Mock
    IssueActivationTokenUseCase issueActivationToken;

    BootstrapProperties enabledProperties;

    @BeforeEach
    void setUp() {
        enabledProperties = properties(true);
    }

    private BootstrapProperties properties(boolean enabled) {
        return new BootstrapProperties(
                enabled,
                new BootstrapProperties.Organisation(ORG_NAME),
                new BootstrapProperties.GeneralGroup(GENERAL_GROUP_NAME),
                new BootstrapProperties.Admin(ADMIN_EMAIL, ADMIN_FIRST_NAME, ADMIN_LAST_NAME)
        );
    }

    private OrganisationBootstrap newBootstrap(BootstrapProperties properties) {
        return new OrganisationBootstrap(
                properties,
                organisationRepository,
                userRepository,
                createOrganisation,
                createGeneralGroup,
                issueMembershipInvitation,
                issueActivationToken
        );
    }

    /** Prépare les stubs d'un premier boot (BD vide, admin créé puis PENDING). */
    private UserId stubFirstBoot(OrganisationId createdOrgId) {
        UserId createdUserId = UserId.generate();
        User pendingAdmin = mock(User.class);
        when(pendingAdmin.id()).thenReturn(createdUserId);
        when(pendingAdmin.status()).thenReturn(UserStatus.PENDING);

        when(organisationRepository.findByName(any())).thenReturn(Optional.empty());
        when(createOrganisation.create(any())).thenReturn(createdOrgId);
        when(userRepository.findByEmail(ADMIN_EMAIL_VO))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(pendingAdmin));
        when(issueMembershipInvitation.issue(any())).thenReturn(MembershipInvitationId.generate());
        when(userRepository.findById(createdUserId)).thenReturn(Optional.of(pendingAdmin));
        return createdUserId;
    }

    @Nested
    @DisplayName("Disabled")
    class Disabled {

        @Test
        @DisplayName("should be a no-op when alertmns.bootstrap.enabled=false")
        void shouldBeNoOpWhenDisabled() {
            newBootstrap(properties(false)).run();

            verifyNoInteractions(
                    organisationRepository,
                    userRepository,
                    createOrganisation,
                    createGeneralGroup,
                    issueMembershipInvitation,
                    issueActivationToken
            );
        }
    }

    @Nested
    @DisplayName("First boot (database is empty)")
    class FirstBoot {

        @Test
        @DisplayName("should create the organisation, issue the admin invitation, and rely on the listener for the magic-link")
        void shouldCreateOrgAndIssueAdminInvitation() {
            OrganisationId createdOrgId = OrganisationId.generate();
            stubFirstBoot(createdOrgId);

            newBootstrap(enabledProperties).run();

            ArgumentCaptor<CreateOrganisationCommand> orgCmd = ArgumentCaptor.forClass(CreateOrganisationCommand.class);
            verify(createOrganisation).create(orgCmd.capture());
            assertThat(orgCmd.getValue().name()).isEqualTo(ORG_NAME);

            ArgumentCaptor<IssueMembershipInvitationCommand> inviteCmd =
                    ArgumentCaptor.forClass(IssueMembershipInvitationCommand.class);
            verify(issueMembershipInvitation).issue(inviteCmd.capture());
            assertThat(inviteCmd.getValue().organisationId()).isEqualTo(createdOrgId.value().toString());
            assertThat(inviteCmd.getValue().invitedEmail()).isEqualTo(ADMIN_EMAIL);
            assertThat(inviteCmd.getValue().firstName()).isEqualTo(ADMIN_FIRST_NAME);
            assertThat(inviteCmd.getValue().lastName()).isEqualTo(ADMIN_LAST_NAME);
            assertThat(inviteCmd.getValue().role()).isEqualTo(MemberRole.ADMIN.name());
        }

        @Test
        @DisplayName("should provision the general group for the organisation using the configured name")
        void shouldProvisionGeneralGroup() {
            OrganisationId createdOrgId = OrganisationId.generate();
            stubFirstBoot(createdOrgId);

            newBootstrap(enabledProperties).run();

            ArgumentCaptor<CreateGeneralGroupCommand> ggCmd =
                    ArgumentCaptor.forClass(CreateGeneralGroupCommand.class);
            verify(createGeneralGroup).create(ggCmd.capture());
            assertThat(ggCmd.getValue().organisationId()).isEqualTo(createdOrgId.value().toString());
            assertThat(ggCmd.getValue().name()).isEqualTo(GENERAL_GROUP_NAME);
        }

        @Test
        @DisplayName("should reissue a magic-link because the freshly created admin is still PENDING")
        void shouldReissueMagicLinkWhenAdminIsPending() {
            OrganisationId createdOrgId = OrganisationId.generate();
            UserId createdUserId = stubFirstBoot(createdOrgId);

            newBootstrap(enabledProperties).run();

            ArgumentCaptor<IssueActivationTokenCommand> tokenCmd =
                    ArgumentCaptor.forClass(IssueActivationTokenCommand.class);
            verify(issueActivationToken).issue(tokenCmd.capture());
            assertThat(tokenCmd.getValue().userId()).isEqualTo(createdUserId.value().toString());
        }

        @Test
        @DisplayName("should execute the steps in order: organisation → general group → invitation → reissue")
        void shouldExecuteStepsInOrder() {
            OrganisationId createdOrgId = OrganisationId.generate();
            stubFirstBoot(createdOrgId);

            newBootstrap(enabledProperties).run();

            InOrder inOrder = inOrder(
                    createOrganisation, createGeneralGroup, issueMembershipInvitation, issueActivationToken);
            inOrder.verify(createOrganisation).create(any());
            inOrder.verify(createGeneralGroup).create(any());
            inOrder.verify(issueMembershipInvitation).issue(any());
            inOrder.verify(issueActivationToken).issue(any());
        }

        @Test
        @DisplayName("should throw IllegalStateException if the User is not found after issuing the invitation")
        void shouldThrowWhenUserNotFoundAfterIssue() {
            OrganisationId createdOrgId = OrganisationId.generate();
            when(organisationRepository.findByName(any())).thenReturn(Optional.empty());
            when(createOrganisation.create(any())).thenReturn(createdOrgId);
            // findByEmail returns empty both times — simulates a partial failure where IssueMembershipInvitation
            // did not actually persist the User (defensive guard against orchestration bugs).
            when(userRepository.findByEmail(ADMIN_EMAIL_VO)).thenReturn(Optional.empty());
            when(issueMembershipInvitation.issue(any())).thenReturn(MembershipInvitationId.generate());

            assertThrows(IllegalStateException.class,
                    () -> newBootstrap(enabledProperties).run());

            verify(issueActivationToken, never()).issue(any());
        }
    }

    @Nested
    @DisplayName("Second boot — admin still PENDING (e.g. activation mail was lost)")
    class SecondBootAdminStillPending {

        @Test
        @DisplayName("should not recreate the organisation when it already exists")
        void shouldNotRecreateOrganisation() {
            OrganisationId existingOrgId = OrganisationId.generate();
            Organisation existingOrg = mock(Organisation.class);
            when(existingOrg.id()).thenReturn(existingOrgId);
            User pendingAdmin = mock(User.class);
            UserId existingUserId = UserId.generate();
            when(pendingAdmin.id()).thenReturn(existingUserId);
            when(pendingAdmin.status()).thenReturn(UserStatus.PENDING);

            when(organisationRepository.findByName(any())).thenReturn(Optional.of(existingOrg));
            when(userRepository.findByEmail(ADMIN_EMAIL_VO)).thenReturn(Optional.of(pendingAdmin));
            when(userRepository.findById(existingUserId)).thenReturn(Optional.of(pendingAdmin));

            newBootstrap(enabledProperties).run();

            verify(createOrganisation, never()).create(any());
        }

        @Test
        @DisplayName("should not re-issue the invitation when the admin user already exists")
        void shouldNotReissueInvitationWhenAdminExists() {
            OrganisationId existingOrgId = OrganisationId.generate();
            Organisation existingOrg = mock(Organisation.class);
            when(existingOrg.id()).thenReturn(existingOrgId);
            User pendingAdmin = mock(User.class);
            UserId existingUserId = UserId.generate();
            when(pendingAdmin.id()).thenReturn(existingUserId);
            when(pendingAdmin.status()).thenReturn(UserStatus.PENDING);

            when(organisationRepository.findByName(any())).thenReturn(Optional.of(existingOrg));
            when(userRepository.findByEmail(ADMIN_EMAIL_VO)).thenReturn(Optional.of(pendingAdmin));
            when(userRepository.findById(existingUserId)).thenReturn(Optional.of(pendingAdmin));

            newBootstrap(enabledProperties).run();

            verify(issueMembershipInvitation, never()).issue(any());
        }

        @Test
        @DisplayName("should reissue the magic-link because the admin is still PENDING")
        void shouldReissueMagicLink() {
            OrganisationId existingOrgId = OrganisationId.generate();
            Organisation existingOrg = mock(Organisation.class);
            when(existingOrg.id()).thenReturn(existingOrgId);
            User pendingAdmin = mock(User.class);
            UserId existingUserId = UserId.generate();
            when(pendingAdmin.id()).thenReturn(existingUserId);
            when(pendingAdmin.status()).thenReturn(UserStatus.PENDING);

            when(organisationRepository.findByName(any())).thenReturn(Optional.of(existingOrg));
            when(userRepository.findByEmail(ADMIN_EMAIL_VO)).thenReturn(Optional.of(pendingAdmin));
            when(userRepository.findById(existingUserId)).thenReturn(Optional.of(pendingAdmin));

            newBootstrap(enabledProperties).run();

            ArgumentCaptor<IssueActivationTokenCommand> tokenCmd =
                    ArgumentCaptor.forClass(IssueActivationTokenCommand.class);
            verify(issueActivationToken).issue(tokenCmd.capture());
            assertThat(tokenCmd.getValue().userId()).isEqualTo(existingUserId.value().toString());
        }
    }

    @Nested
    @DisplayName("Second boot — admin is already ACTIVE")
    class SecondBootAdminActive {

        @Test
        @DisplayName("should not recreate the organisation nor reissue a magic-link")
        void shouldNotRecreateNorReissueWhenAdminActive() {
            OrganisationId existingOrgId = OrganisationId.generate();
            Organisation existingOrg = mock(Organisation.class);
            when(existingOrg.id()).thenReturn(existingOrgId);
            User activeAdmin = mock(User.class);
            UserId existingUserId = UserId.generate();
            when(activeAdmin.id()).thenReturn(existingUserId);
            when(activeAdmin.status()).thenReturn(UserStatus.ACTIVE);

            when(organisationRepository.findByName(any())).thenReturn(Optional.of(existingOrg));
            when(userRepository.findByEmail(ADMIN_EMAIL_VO)).thenReturn(Optional.of(activeAdmin));
            when(userRepository.findById(existingUserId)).thenReturn(Optional.of(activeAdmin));

            newBootstrap(enabledProperties).run();

            verify(createOrganisation, never()).create(any());
            verify(issueMembershipInvitation, never()).issue(any());
            verifyNoInteractions(issueActivationToken);
            // ensureGeneralGroup is still invoked every boot — idempotence is handled inside the service.
            verify(createGeneralGroup).create(any());
        }
    }

    @Nested
    @DisplayName("Invariants")
    class Invariants {

        @Test
        @DisplayName("should reject null properties")
        void shouldRejectNullProperties() {
            assertThrows(NullPointerException.class, () -> new OrganisationBootstrap(
                    null, organisationRepository, userRepository,
                    createOrganisation, createGeneralGroup, issueMembershipInvitation, issueActivationToken));
        }

        @Test
        @DisplayName("should reject null OrganisationRepository")
        void shouldRejectNullOrganisationRepository() {
            assertThrows(NullPointerException.class, () -> new OrganisationBootstrap(
                    enabledProperties, null, userRepository,
                    createOrganisation, createGeneralGroup, issueMembershipInvitation, issueActivationToken));
        }

        @Test
        @DisplayName("should reject null UserRepository")
        void shouldRejectNullUserRepository() {
            assertThrows(NullPointerException.class, () -> new OrganisationBootstrap(
                    enabledProperties, organisationRepository, null,
                    createOrganisation, createGeneralGroup, issueMembershipInvitation, issueActivationToken));
        }

        @Test
        @DisplayName("should reject null CreateOrganisationUseCase")
        void shouldRejectNullCreateOrganisationUseCase() {
            assertThrows(NullPointerException.class, () -> new OrganisationBootstrap(
                    enabledProperties, organisationRepository, userRepository,
                    null, createGeneralGroup, issueMembershipInvitation, issueActivationToken));
        }

        @Test
        @DisplayName("should reject null CreateGeneralGroupUseCase")
        void shouldRejectNullCreateGeneralGroupUseCase() {
            assertThrows(NullPointerException.class, () -> new OrganisationBootstrap(
                    enabledProperties, organisationRepository, userRepository,
                    createOrganisation, null, issueMembershipInvitation, issueActivationToken));
        }

        @Test
        @DisplayName("should reject null IssueMembershipInvitationUseCase")
        void shouldRejectNullIssueMembershipInvitationUseCase() {
            assertThrows(NullPointerException.class, () -> new OrganisationBootstrap(
                    enabledProperties, organisationRepository, userRepository,
                    createOrganisation, createGeneralGroup, null, issueActivationToken));
        }

        @Test
        @DisplayName("should reject null IssueActivationTokenUseCase")
        void shouldRejectNullIssueActivationTokenUseCase() {
            assertThrows(NullPointerException.class, () -> new OrganisationBootstrap(
                    enabledProperties, organisationRepository, userRepository,
                    createOrganisation, createGeneralGroup, issueMembershipInvitation, null));
        }
    }
}
