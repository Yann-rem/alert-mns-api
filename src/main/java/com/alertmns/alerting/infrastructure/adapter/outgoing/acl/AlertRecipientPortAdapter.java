package com.alertmns.alerting.infrastructure.adapter.outgoing.acl;

import com.alertmns.alerting.domain.port.outgoing.AlertRecipientPort;
import com.alertmns.organisation.domain.model.GroupId;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import com.alertmns.shared.OrganisationId;

import java.util.List;
import java.util.UUID;

/**
 * Adapter d'anti-corruption traduisant le besoin du BC Alerting (« qui sont les destinataires de cette alerte ? ») vers
 * les requêtes du BC Organisation.
 *
 * <p>Doublon d'ACL assumé par BC (ISP), comme {@code GroupMembershipPortAdapter} : chaque BC déclare son propre port.
 * La résolution renvoie des {@code userId} (clé de routage du push).</p>
 */
public final class AlertRecipientPortAdapter implements AlertRecipientPort {

    private final MemberRepository memberRepository;

    public AlertRecipientPortAdapter(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public List<UUID> organisationRecipients(OrganisationId organisationId) {
        return memberRepository.findUserIdsByOrganisationId(organisationId);
    }

    @Override
    public List<UUID> groupRecipients(UUID groupId) {
        return memberRepository.findUserIdsByGroupId(GroupId.from(groupId));
    }
}
