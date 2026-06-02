package com.alertmns.organisation.infrastructure.config;

import com.alertmns.identity.domain.port.incoming.RegisterPendingUserUseCase;
import com.alertmns.identity.domain.port.outgoing.UserRepository;
import com.alertmns.organisation.application.AcceptMembershipInvitationService;
import com.alertmns.organisation.application.AddMemberToGroupService;
import com.alertmns.organisation.application.CreateGroupService;
import com.alertmns.organisation.application.CreateOrganisationService;
import com.alertmns.organisation.application.IssueMembershipInvitationService;
import com.alertmns.organisation.application.ReactivateMemberService;
import com.alertmns.organisation.application.RemoveMemberFromGroupService;
import com.alertmns.organisation.application.RenameGroupService;
import com.alertmns.organisation.application.SuspendMemberService;
import com.alertmns.organisation.domain.port.incoming.AcceptMembershipInvitationUseCase;
import com.alertmns.organisation.domain.port.outgoing.GroupMembershipRepository;
import com.alertmns.organisation.domain.port.outgoing.GroupRepository;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import com.alertmns.organisation.domain.port.outgoing.MembershipInvitationRepository;
import com.alertmns.organisation.domain.port.outgoing.OrganisationRepository;
import com.alertmns.organisation.infrastructure.adapter.incoming.event.AcceptMembershipInvitationOnUserActivatedListener;
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
    public CreateOrganisationService createOrganisationService(
            OrganisationRepository repository,
            EventPublisher publisher,
            Clock clock
    ) {
        return new CreateOrganisationService(repository, publisher, clock);
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
                invitationRepository, acceptMembershipInvitationUseCase);
    }
}
