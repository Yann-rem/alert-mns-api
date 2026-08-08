package com.alertmns.alerting.infrastructure.adapter.outgoing.acl;

import com.alertmns.alerting.domain.port.outgoing.IssuerDirectoryPort;
import com.alertmns.identity.domain.model.User;
import com.alertmns.identity.domain.port.outgoing.UserRepository;
import com.alertmns.organisation.domain.model.Member;
import com.alertmns.organisation.domain.model.MemberId;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import com.alertmns.shared.UserId;

import java.util.UUID;

/**
 * Adapter d'anti-corruption nommant l'émetteur d'une alerte : memberId → userId (BC Organisation), puis
 * userId → nom (BC Identity).
 *
 * <p>La traversée des deux BC voisins est <b>confinée ici</b> : c'est précisément le rôle d'un adaptateur
 * d'anti-corruption de traduire le monde extérieur en ce dont le BC a besoin. Le port, lui, reste mono-besoin.</p>
 *
 * <p>Applique la doctrine RGPD (ADR-0017 §2/§4) : un utilisateur anonymisé ou introuvable est rendu comme
 * {@value IssuerDirectoryPort#DELETED_USER_DISPLAY_NAME}, jamais par une donnée personnelle.</p>
 */
public final class IssuerDirectoryPortAdapter implements IssuerDirectoryPort {

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;

    public IssuerDirectoryPortAdapter(MemberRepository memberRepository, UserRepository userRepository) {
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
    }

    @Override
    public String displayNameOf(UUID issuerId) {
        if (issuerId == null) {
            return DELETED_USER_DISPLAY_NAME;
        }
        return memberRepository.findById(MemberId.from(issuerId))
                .map(Member::userId)
                .flatMap(userId -> userRepository.findById(UserId.from(userId)))
                .map(IssuerDirectoryPortAdapter::render)
                .orElse(DELETED_USER_DISPLAY_NAME);
    }

    private static String render(User user) {
        if (user.isAnonymized()) {
            return DELETED_USER_DISPLAY_NAME;
        }
        return user.profile().firstName().value() + " " + user.profile().lastName().value();
    }
}
