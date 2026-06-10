package com.alertmns.organisation.infrastructure.adapter.incoming.event;

import com.alertmns.organisation.domain.event.MemberJoined;
import com.alertmns.organisation.domain.model.Group;
import com.alertmns.organisation.domain.port.incoming.AddMemberToGroupUseCase;
import com.alertmns.organisation.domain.port.incoming.command.AddMemberToGroupCommand;
import com.alertmns.organisation.domain.port.outgoing.GroupRepository;
import org.springframework.context.event.EventListener;

import java.util.Objects;

/**
 * Listener qui rattache automatiquement un nouveau membre au groupe GENERAL de son organisation dès qu'il la rejoint
 * (cascade {@link MemberJoined} → {@link AddMemberToGroupUseCase}).
 */
public final class AddMemberToGeneralGroupOnMemberJoinedListener {

    private final GroupRepository groupRepository;
    private final AddMemberToGroupUseCase addMemberToGroupUseCase;

    public AddMemberToGeneralGroupOnMemberJoinedListener(
            GroupRepository groupRepository,
            AddMemberToGroupUseCase addMemberToGroupUseCase
    ) {
        this.groupRepository = Objects.requireNonNull(groupRepository, "groupRepository must not be null");
        this.addMemberToGroupUseCase = Objects.requireNonNull(
                addMemberToGroupUseCase, "addMemberToGroupUseCase must not be null");
    }

    @EventListener
    public void onMemberJoinedEvent(MemberJoined event) {
        Group general = groupRepository.findGeneralByOrganisationId(event.organisationId())
                .orElseThrow(() -> new IllegalStateException(
                        "No GENERAL group found for organisation: " + event.organisationId().value()));

        addMemberToGroupUseCase.add(new AddMemberToGroupCommand(
                event.organisationId().value().toString(),
                general.id().value().toString(),
                event.memberId().value().toString()
        ));
    }
}
