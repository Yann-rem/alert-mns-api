package com.alertmns.bootstrap;

import com.alertmns.iam.domain.model.User;
import com.alertmns.iam.domain.model.UserStatus;
import com.alertmns.iam.domain.port.incoming.IssueActivationTokenUseCase;
import com.alertmns.iam.domain.port.incoming.RegisterUserUseCase;
import com.alertmns.iam.domain.port.incoming.command.IssueActivationTokenCommand;
import com.alertmns.iam.domain.port.incoming.command.RegisterUserCommand;
import com.alertmns.iam.domain.port.outgoing.UserRepository;
import com.alertmns.organisation.domain.exception.MemberAlreadyExistsException;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.model.Organisation;
import com.alertmns.organisation.domain.port.incoming.CreateOrganisationUseCase;
import com.alertmns.organisation.domain.port.incoming.InviteMemberUseCase;
import com.alertmns.organisation.domain.port.incoming.command.CreateOrganisationCommand;
import com.alertmns.organisation.domain.port.incoming.command.InviteMemberCommand;
import com.alertmns.organisation.domain.port.outgoing.OrganisationRepository;
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
import java.util.UUID;

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
    private static final String ADMIN_EMAIL = "admin@metz-numeric-school.com";
    private static final String ADMIN_FIRST_NAME = "Admin";
    private static final String ADMIN_LAST_NAME = "Système";

    @Mock
    OrganisationRepository organisationRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    CreateOrganisationUseCase createOrganisation;

    @Mock
    RegisterUserUseCase registerUser;

    @Mock
    InviteMemberUseCase inviteMember;

    @Mock
    IssueActivationTokenUseCase issueActivationToken;

    BootstrapProperties enabledProperties;

    @BeforeEach
    void setUp() {
        enabledProperties = new BootstrapProperties(
                true,
                new BootstrapProperties.Organisation(ORG_NAME),
                new BootstrapProperties.Admin(ADMIN_EMAIL, ADMIN_FIRST_NAME, ADMIN_LAST_NAME)
        );
    }

    private OrganisationBootstrap newBootstrap(BootstrapProperties properties) {
        return new OrganisationBootstrap(
                properties,
                organisationRepository,
                userRepository,
                createOrganisation,
                registerUser,
                inviteMember,
                issueActivationToken
        );
    }

    @Nested
    @DisplayName("Disabled")
    class Disabled {

        @Test
        @DisplayName("should be a no-op when alertmns.bootstrap.enabled=false")
        void shouldBeNoOpWhenDisabled() {
            BootstrapProperties disabled = new BootstrapProperties(
                    false,
                    new BootstrapProperties.Organisation(ORG_NAME),
                    new BootstrapProperties.Admin(ADMIN_EMAIL, ADMIN_FIRST_NAME, ADMIN_LAST_NAME)
            );

            newBootstrap(disabled).run();

            verifyNoInteractions(
                    organisationRepository,
                    userRepository,
                    createOrganisation,
                    registerUser,
                    inviteMember,
                    issueActivationToken
            );
        }
    }

    @Nested
    @DisplayName("First boot (database is empty)")
    class FirstBoot {

        @Test
        @DisplayName("should create the organisation, the admin user, the admin member, and rely on the listener for the magic-link")
        void shouldProvisionAllAggregatesOnFirstBoot() {
            OrganisationId createdOrgId = OrganisationId.generate();
            UserId createdUserId = UserId.generate();
            MemberId createdMemberId = MemberId.generate();
            User pendingAdmin = mock(User.class);
            when(pendingAdmin.status()).thenReturn(UserStatus.PENDING);

            when(organisationRepository.findByName(any())).thenReturn(Optional.empty());
            when(createOrganisation.create(any())).thenReturn(createdOrgId);
            when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
            when(registerUser.register(any())).thenReturn(createdUserId);
            when(inviteMember.invite(any())).thenReturn(createdMemberId);
            when(userRepository.findById(createdUserId)).thenReturn(Optional.of(pendingAdmin));

            newBootstrap(enabledProperties).run();

            ArgumentCaptor<CreateOrganisationCommand> orgCmd = ArgumentCaptor.forClass(CreateOrganisationCommand.class);
            verify(createOrganisation).create(orgCmd.capture());
            assertThat(orgCmd.getValue().name()).isEqualTo(ORG_NAME);

            ArgumentCaptor<RegisterUserCommand> userCmd = ArgumentCaptor.forClass(RegisterUserCommand.class);
            verify(registerUser).register(userCmd.capture());
            assertThat(userCmd.getValue().email()).isEqualTo(ADMIN_EMAIL);
            assertThat(userCmd.getValue().firstName()).isEqualTo(ADMIN_FIRST_NAME);
            assertThat(userCmd.getValue().lastName()).isEqualTo(ADMIN_LAST_NAME);
            assertThat(userCmd.getValue().rawPassword())
                    .as("Le mot de passe initial est généré aléatoirement, non vide, jamais en clair dans la config")
                    .isNotBlank();

            ArgumentCaptor<InviteMemberCommand> inviteCmd = ArgumentCaptor.forClass(InviteMemberCommand.class);
            verify(inviteMember).invite(inviteCmd.capture());
            assertThat(inviteCmd.getValue().organisationId()).isEqualTo(createdOrgId.value().toString());
            assertThat(inviteCmd.getValue().userId()).isEqualTo(createdUserId.value().toString());
            assertThat(inviteCmd.getValue().role()).isEqualTo(MemberRole.ADMIN.name());
        }

        @Test
        @DisplayName("should reissue a magic-link because the freshly created admin is still PENDING")
        void shouldReissueMagicLinkWhenAdminIsPending() {
            OrganisationId createdOrgId = OrganisationId.generate();
            UserId createdUserId = UserId.generate();
            User pendingAdmin = mock(User.class);
            when(pendingAdmin.status()).thenReturn(UserStatus.PENDING);

            when(organisationRepository.findByName(any())).thenReturn(Optional.empty());
            when(createOrganisation.create(any())).thenReturn(createdOrgId);
            when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
            when(registerUser.register(any())).thenReturn(createdUserId);
            when(inviteMember.invite(any())).thenReturn(MemberId.generate());
            when(userRepository.findById(createdUserId)).thenReturn(Optional.of(pendingAdmin));

            newBootstrap(enabledProperties).run();

            ArgumentCaptor<IssueActivationTokenCommand> tokenCmd =
                    ArgumentCaptor.forClass(IssueActivationTokenCommand.class);
            verify(issueActivationToken).issue(tokenCmd.capture());
            assertThat(tokenCmd.getValue().userId()).isEqualTo(createdUserId.value().toString());
        }

        @Test
        @DisplayName("should execute the 4 bootstrap steps in order: organisation → user → member → reissue")
        void shouldExecuteStepsInOrder() {
            OrganisationId createdOrgId = OrganisationId.generate();
            UserId createdUserId = UserId.generate();
            User pendingAdmin = mock(User.class);
            when(pendingAdmin.status()).thenReturn(UserStatus.PENDING);

            when(organisationRepository.findByName(any())).thenReturn(Optional.empty());
            when(createOrganisation.create(any())).thenReturn(createdOrgId);
            when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
            when(registerUser.register(any())).thenReturn(createdUserId);
            when(inviteMember.invite(any())).thenReturn(MemberId.generate());
            when(userRepository.findById(createdUserId)).thenReturn(Optional.of(pendingAdmin));

            newBootstrap(enabledProperties).run();

            InOrder inOrder = inOrder(createOrganisation, registerUser, inviteMember, issueActivationToken);
            inOrder.verify(createOrganisation).create(any());
            inOrder.verify(registerUser).register(any());
            inOrder.verify(inviteMember).invite(any());
            inOrder.verify(issueActivationToken).issue(any());
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
            when(userRepository.findByEmail(any())).thenReturn(Optional.of(pendingAdmin));
            when(userRepository.findById(existingUserId)).thenReturn(Optional.of(pendingAdmin));

            newBootstrap(enabledProperties).run();

            verify(createOrganisation, never()).create(any());
        }

        @Test
        @DisplayName("should not recreate the admin user when it already exists")
        void shouldNotRecreateAdminUser() {
            OrganisationId existingOrgId = OrganisationId.generate();
            Organisation existingOrg = mock(Organisation.class);
            when(existingOrg.id()).thenReturn(existingOrgId);
            User pendingAdmin = mock(User.class);
            UserId existingUserId = UserId.generate();
            when(pendingAdmin.id()).thenReturn(existingUserId);
            when(pendingAdmin.status()).thenReturn(UserStatus.PENDING);

            when(organisationRepository.findByName(any())).thenReturn(Optional.of(existingOrg));
            when(userRepository.findByEmail(any())).thenReturn(Optional.of(pendingAdmin));
            when(userRepository.findById(existingUserId)).thenReturn(Optional.of(pendingAdmin));

            newBootstrap(enabledProperties).run();

            verify(registerUser, never()).register(any());
        }

        @Test
        @DisplayName("should remain idempotent when InviteMemberUseCase throws MemberAlreadyExistsException")
        void shouldSwallowMemberAlreadyExistsException() {
            OrganisationId existingOrgId = OrganisationId.generate();
            Organisation existingOrg = mock(Organisation.class);
            when(existingOrg.id()).thenReturn(existingOrgId);
            User pendingAdmin = mock(User.class);
            UserId existingUserId = UserId.generate();
            when(pendingAdmin.id()).thenReturn(existingUserId);
            when(pendingAdmin.status()).thenReturn(UserStatus.PENDING);

            when(organisationRepository.findByName(any())).thenReturn(Optional.of(existingOrg));
            when(userRepository.findByEmail(any())).thenReturn(Optional.of(pendingAdmin));
            when(userRepository.findById(existingUserId)).thenReturn(Optional.of(pendingAdmin));
            when(inviteMember.invite(any()))
                    .thenThrow(new MemberAlreadyExistsException(existingOrgId, UUID.randomUUID()));

            // Ne doit pas lever : le flux continue jusqu'au ré-amorçage du magic-link.
            newBootstrap(enabledProperties).run();

            verify(issueActivationToken).issue(any());
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
            when(userRepository.findByEmail(any())).thenReturn(Optional.of(pendingAdmin));
            when(userRepository.findById(existingUserId)).thenReturn(Optional.of(pendingAdmin));
            when(inviteMember.invite(any()))
                    .thenThrow(new MemberAlreadyExistsException(existingOrgId, UUID.randomUUID()));

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
        @DisplayName("should be fully idempotent: no creation, no reissue")
        void shouldBeFullyNoOpWhenAdminActive() {
            OrganisationId existingOrgId = OrganisationId.generate();
            Organisation existingOrg = mock(Organisation.class);
            when(existingOrg.id()).thenReturn(existingOrgId);
            User activeAdmin = mock(User.class);
            UserId existingUserId = UserId.generate();
            when(activeAdmin.id()).thenReturn(existingUserId);
            when(activeAdmin.status()).thenReturn(UserStatus.ACTIVE);

            when(organisationRepository.findByName(any())).thenReturn(Optional.of(existingOrg));
            when(userRepository.findByEmail(any())).thenReturn(Optional.of(activeAdmin));
            when(userRepository.findById(existingUserId)).thenReturn(Optional.of(activeAdmin));
            when(inviteMember.invite(any()))
                    .thenThrow(new MemberAlreadyExistsException(existingOrgId, UUID.randomUUID()));

            newBootstrap(enabledProperties).run();

            verify(createOrganisation, never()).create(any());
            verify(registerUser, never()).register(any());
            verifyNoInteractions(issueActivationToken);
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
                    createOrganisation, registerUser, inviteMember, issueActivationToken));
        }

        @Test
        @DisplayName("should reject null OrganisationRepository")
        void shouldRejectNullOrganisationRepository() {
            assertThrows(NullPointerException.class, () -> new OrganisationBootstrap(
                    enabledProperties, null, userRepository,
                    createOrganisation, registerUser, inviteMember, issueActivationToken));
        }

        @Test
        @DisplayName("should reject null UserRepository")
        void shouldRejectNullUserRepository() {
            assertThrows(NullPointerException.class, () -> new OrganisationBootstrap(
                    enabledProperties, organisationRepository, null,
                    createOrganisation, registerUser, inviteMember, issueActivationToken));
        }

        @Test
        @DisplayName("should reject null CreateOrganisationUseCase")
        void shouldRejectNullCreateOrganisationUseCase() {
            assertThrows(NullPointerException.class, () -> new OrganisationBootstrap(
                    enabledProperties, organisationRepository, userRepository,
                    null, registerUser, inviteMember, issueActivationToken));
        }

        @Test
        @DisplayName("should reject null RegisterUserUseCase")
        void shouldRejectNullRegisterUserUseCase() {
            assertThrows(NullPointerException.class, () -> new OrganisationBootstrap(
                    enabledProperties, organisationRepository, userRepository,
                    createOrganisation, null, inviteMember, issueActivationToken));
        }

        @Test
        @DisplayName("should reject null InviteMemberUseCase")
        void shouldRejectNullInviteMemberUseCase() {
            assertThrows(NullPointerException.class, () -> new OrganisationBootstrap(
                    enabledProperties, organisationRepository, userRepository,
                    createOrganisation, registerUser, null, issueActivationToken));
        }

        @Test
        @DisplayName("should reject null IssueActivationTokenUseCase")
        void shouldRejectNullIssueActivationTokenUseCase() {
            assertThrows(NullPointerException.class, () -> new OrganisationBootstrap(
                    enabledProperties, organisationRepository, userRepository,
                    createOrganisation, registerUser, inviteMember, null));
        }
    }
}
