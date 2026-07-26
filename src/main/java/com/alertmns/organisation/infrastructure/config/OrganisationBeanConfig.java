package com.alertmns.organisation.infrastructure.config;

import com.alertmns.identity.domain.port.incoming.RegisterPendingUserUseCase;
import com.alertmns.identity.domain.port.outgoing.UserRepository;
import com.alertmns.organisation.application.AcceptMembershipInvitationService;
import com.alertmns.organisation.application.AddMemberToGroupService;
import com.alertmns.identity.infrastructure.adapter.outgoing.directory.UserDirectoryAdapter;
import com.alertmns.identity.infrastructure.adapter.outgoing.persistence.UserJpaRepository;
import com.alertmns.organisation.application.ChangeMemberRoleService;
import com.alertmns.organisation.application.ListMembersService;
import com.alertmns.organisation.application.CreateGeneralGroupService;
import com.alertmns.organisation.application.CreateGroupService;
import com.alertmns.organisation.application.CreateOrganisationService;
import com.alertmns.organisation.application.CurrentMemberResolver;
import com.alertmns.organisation.application.IssueMembershipInvitationService;
import com.alertmns.organisation.application.ReactivateMemberService;
import com.alertmns.organisation.application.RemoveMemberFromGroupService;
import com.alertmns.organisation.application.RenameGroupService;
import com.alertmns.organisation.application.SuspendMemberService;
import com.alertmns.organisation.domain.port.incoming.AcceptMembershipInvitationUseCase;
import com.alertmns.organisation.domain.port.incoming.AddMemberToGroupUseCase;
import com.alertmns.organisation.domain.port.outgoing.GroupMembershipRepository;
import com.alertmns.organisation.domain.port.outgoing.GroupRepository;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import com.alertmns.organisation.domain.port.outgoing.UserDirectoryPort;
import com.alertmns.organisation.domain.port.outgoing.MembershipInvitationRepository;
import com.alertmns.organisation.domain.port.outgoing.OrganisationRepository;
import com.alertmns.organisation.infrastructure.adapter.incoming.event.AcceptMembershipInvitationOnUserActivatedListener;
import com.alertmns.organisation.infrastructure.adapter.incoming.event.AddMemberToGeneralGroupOnMemberJoinedListener;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.GroupJpaRepository;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.GroupMembershipJpaRepository;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.GroupMembershipPersistenceAdapter;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.GroupPersistenceAdapter;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.MemberJpaRepository;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.MemberPersistenceAdapter;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.MembershipInvitationJpaRepository;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.MembershipInvitationPersistenceAdapter;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.OrganisationJpaRepository;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.OrganisationPersistenceAdapter;
import com.alertmns.shared.CurrentUserPort;
import com.alertmns.shared.EventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Duration;

@Configuration
public class OrganisationBeanConfig {

    // --- Ports sortants ---

    @Bean
    public OrganisationRepository organisationRepository(OrganisationJpaRepository jpaRepository) {
        return new OrganisationPersistenceAdapter(jpaRepository);
    }

    @Bean
    public GroupRepository groupRepository(GroupJpaRepository jpaRepository) {
        return new GroupPersistenceAdapter(jpaRepository);
    }

    @Bean
    public MemberRepository memberRepository(MemberJpaRepository jpaRepository) {
        return new MemberPersistenceAdapter(jpaRepository);
    }

    @Bean
    public GroupMembershipRepository groupMembershipRepository(GroupMembershipJpaRepository jpaRepository) {
        return new GroupMembershipPersistenceAdapter(jpaRepository);
    }

    @Bean
    public MembershipInvitationRepository membershipInvitationRepository(
            MembershipInvitationJpaRepository jpaRepository
    ) {
        return new MembershipInvitationPersistenceAdapter(jpaRepository);
    }

    // --- Services ---

    @Bean
    public CurrentMemberResolver currentMemberResolver(
            CurrentUserPort currentUserPort,
            MemberRepository memberRepository
    ) {
        return new CurrentMemberResolver(currentUserPort, memberRepository);
    }

    @Bean
    public CreateOrganisationService createOrganisationService(
            OrganisationRepository repository,
            EventPublisher publisher,
            Clock clock
    ) {
        return new CreateOrganisationService(repository, publisher, clock);
    }

    @Bean
    public CreateGeneralGroupService createGeneralGroupService(
            GroupRepository repository,
            EventPublisher publisher,
            Clock clock
    ) {
        return new CreateGeneralGroupService(repository, publisher, clock);
    }

    @Bean
    public CreateGroupService createGroupService(GroupRepository repository, EventPublisher publisher, Clock clock) {
        return new CreateGroupService(repository, publisher, clock);
    }

    @Bean
    public RenameGroupService renameGroupService(GroupRepository repository, EventPublisher publisher, Clock clock) {
        return new RenameGroupService(repository, publisher, clock);
    }

    @Bean
    public ReactivateMemberService reactivateMemberService(
            MemberRepository repository,
            EventPublisher publisher,
            Clock clock
    ) {
        return new ReactivateMemberService(repository, publisher, clock);
    }

    @Bean
    public SuspendMemberService suspendMemberService(
            MemberRepository repository,
            EventPublisher publisher,
            Clock clock
    ) {
        return new SuspendMemberService(repository, publisher, clock);
    }

    @Bean
    public ChangeMemberRoleService changeMemberRoleService(
            MemberRepository repository,
            EventPublisher publisher,
            Clock clock
    ) {
        return new ChangeMemberRoleService(repository, publisher, clock);
    }

    /**
     * Annuaire des utilisateurs fourni par le BC Identity.
     *
     * <p>Câblé ici, dans la config du BC <b>consommateur</b>, symétriquement à
     * {@code MemberUserAuthoritiesAdapter} qui est câblé côté Identity.</p>
     *
     * <p>Le nom du bean est préfixé car le BC Messaging déclare un port homonyme
     * ({@code messaging.domain.port.outgoing.UserDirectoryPort}) au contrat différent : chaque BC
     * définit son propre annuaire selon ses besoins. Les types diffèrent, seuls les noms de beans
     * entraient en collision.</p>
     */
    @Bean
    public UserDirectoryPort organisationUserDirectoryPort(UserJpaRepository userJpaRepository) {
        return new UserDirectoryAdapter(userJpaRepository);
    }

    /** Lecture du backoffice : compose les membres (Organisation) et leur identité (Identity). */
    @Bean
    public ListMembersService listMembersService(
            MemberRepository repository,
            UserDirectoryPort userDirectory
    ) {
        return new ListMembersService(repository, userDirectory);
    }

    @Bean
    public AddMemberToGroupService addMemberToGroupService(
            GroupRepository groupRepository,
            MemberRepository memberRepository,
            GroupMembershipRepository groupMembershipRepository,
            EventPublisher publisher,
            Clock clock
    ) {
        return new AddMemberToGroupService(
                groupRepository,
                memberRepository,
                groupMembershipRepository,
                publisher,
                clock
        );
    }

    @Bean
    public RemoveMemberFromGroupService removeMemberFromGroupService(
            GroupMembershipRepository repository,
            EventPublisher publisher,
            Clock clock
    ) {
        return new RemoveMemberFromGroupService(repository, publisher, clock);
    }

    @Bean
    public IssueMembershipInvitationService issueMembershipInvitationService(
            MembershipInvitationRepository invitationRepository,
            UserRepository userRepository,
            RegisterPendingUserUseCase registerPendingUserUseCase,
            EventPublisher publisher,
            Clock clock,
            @Value("${alertmns.organisation.invitation.ttl}") Duration ttl
    ) {
        return new IssueMembershipInvitationService(
                invitationRepository, userRepository, registerPendingUserUseCase, publisher, clock, ttl);
    }

    @Bean
    public AcceptMembershipInvitationService acceptMembershipInvitationService(
            MembershipInvitationRepository invitationRepository,
            MemberRepository memberRepository,
            EventPublisher publisher,
            Clock clock
    ) {
        return new AcceptMembershipInvitationService(invitationRepository, memberRepository, publisher, clock);
    }

    // --- Event listeners ---

    @Bean
    public AcceptMembershipInvitationOnUserActivatedListener acceptMembershipInvitationOnUserActivatedListener(
            MembershipInvitationRepository invitationRepository,
            AcceptMembershipInvitationUseCase acceptMembershipInvitationUseCase
    ) {
        return new AcceptMembershipInvitationOnUserActivatedListener(
                invitationRepository,
                acceptMembershipInvitationUseCase
        );
    }

    @Bean
    public AddMemberToGeneralGroupOnMemberJoinedListener addMemberToGeneralGroupOnMemberJoinedListener(
            GroupRepository groupRepository,
            AddMemberToGroupUseCase addMemberToGroupUseCase
    ) {
        return new AddMemberToGeneralGroupOnMemberJoinedListener(groupRepository, addMemberToGroupUseCase);
    }
}
