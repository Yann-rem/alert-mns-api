package com.alertmns.organisation.infrastructure.config;

import com.alertmns.organisation.application.ActivateMemberService;
import com.alertmns.organisation.application.AddMemberToGroupService;
import com.alertmns.organisation.application.CreateGroupService;
import com.alertmns.organisation.application.CreateOrganisationService;
import com.alertmns.organisation.application.InviteMemberService;
import com.alertmns.organisation.application.ReactivateMemberService;
import com.alertmns.organisation.application.RemoveMemberFromGroupService;
import com.alertmns.organisation.application.RenameGroupService;
import com.alertmns.organisation.application.SuspendMemberService;
import com.alertmns.organisation.domain.port.incoming.ActivateMemberUseCase;
import com.alertmns.organisation.domain.port.outgoing.GroupMembershipRepository;
import com.alertmns.organisation.domain.port.outgoing.GroupRepository;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import com.alertmns.organisation.domain.port.outgoing.OrganisationRepository;
import com.alertmns.organisation.infrastructure.adapter.incoming.event.ActivateMemberOnUserActivatedListener;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.GroupJpaRepository;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.GroupMembershipJpaRepository;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.GroupMembershipPersistenceAdapter;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.GroupPersistenceAdapter;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.MemberJpaRepository;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.MemberPersistenceAdapter;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.OrganisationJpaRepository;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.OrganisationPersistenceAdapter;
import com.alertmns.shared.EventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    // --- Services ---

    @Bean
    public CreateOrganisationService createOrganisationService(
            OrganisationRepository repository,
            EventPublisher publisher
    ) {
        return new CreateOrganisationService(repository, publisher);
    }

    @Bean
    public CreateGroupService createGroupService(GroupRepository repository, EventPublisher publisher) {
        return new CreateGroupService(repository, publisher);
    }

    @Bean
    public RenameGroupService renameGroupService(GroupRepository repository, EventPublisher publisher) {
        return new RenameGroupService(repository, publisher);
    }

    @Bean
    public ActivateMemberService activateMemberService(MemberRepository repository, EventPublisher publisher) {
        return new ActivateMemberService(repository, publisher);
    }

    @Bean
    public InviteMemberService inviteMemberService(MemberRepository repository, EventPublisher publisher) {
        return new InviteMemberService(repository, publisher);
    }

    @Bean
    public ReactivateMemberService reactivateMemberService(MemberRepository repository, EventPublisher publisher) {
        return new ReactivateMemberService(repository, publisher);
    }

    @Bean
    public SuspendMemberService suspendMemberService(MemberRepository repository, EventPublisher publisher) {
        return new SuspendMemberService(repository, publisher);
    }

    @Bean
    public AddMemberToGroupService addMemberToGroupService(
            GroupRepository groupRepository,
            MemberRepository memberRepository,
            GroupMembershipRepository groupMembershipRepository,
            EventPublisher publisher
    ) {
        return new AddMemberToGroupService(groupRepository, memberRepository, groupMembershipRepository, publisher);
    }

    @Bean
    public RemoveMemberFromGroupService removeMemberFromGroupService(
            GroupMembershipRepository repository,
            EventPublisher publisher
    ) {
        return new RemoveMemberFromGroupService(repository, publisher);
    }

    // --- Event listeners ---

    @Bean
    public ActivateMemberOnUserActivatedListener activateMemberOnUserActivatedListener(
            MemberRepository memberRepository,
            ActivateMemberUseCase activateMemberUseCase
    ) {
        return new ActivateMemberOnUserActivatedListener(memberRepository, activateMemberUseCase);
    }
}
