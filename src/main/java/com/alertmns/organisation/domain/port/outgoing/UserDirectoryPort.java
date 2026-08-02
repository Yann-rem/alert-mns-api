package com.alertmns.organisation.domain.port.outgoing;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Port sortant donnant accès à l'identité des utilisateurs rattachés aux membres.
 *
 * <p>Le BC <i>Organisation</i> connaît le rôle et le statut d'un membre, mais pas son nom ni son
 * e-mail, qui appartiennent au BC <i>Identity</i>. Ce port matérialise cette dépendance dans le
 * sens Organisation → Identity, symétriquement au port
 * {@code UserAuthoritiesProvider} qui va d'Identity vers Organisation.</p>
 *
 * <p>Implémenté côté Identity par
 * {@code com.alertmns.identity.infrastructure.adapter.outgoing.directory.UserDirectoryAdapter}.</p>
 */
public interface UserDirectoryPort {

    /**
     * Résout l'identité d'un lot d'utilisateurs. Les identifiants inconnus sont simplement absents
     * du résultat (pas d'exception) : un membre orphelin ne doit pas faire échouer toute la liste.
     *
     * @param userIds identifiants à résoudre
     * @return les identités trouvées, dans un ordre non garanti
     */
    List<UserSummary> findByIds(Collection<UUID> userIds);

    /**
     * Résout l'identité d'un lot d'utilisateurs à partir de leur e-mail.
     *
     * <p>Sert les invitations en attente, qui ne référencent que l'e-mail de l'invité : le
     * {@code User} correspondant existe (créé PENDING à l'émission de l'invitation) et porte ses
     * prénom et nom.</p>
     *
     * @param emails adresses à résoudre
     * @return les identités trouvées, dans un ordre non garanti
     */
    List<UserSummary> findByEmails(Collection<String> emails);

    /**
     * Recherche les utilisateurs dont le nom, le prénom ou l'e-mail contient {@code term}
     * (insensible à la casse). Sert à filtrer une liste de membres sur un critère textuel qui
     * n'existe que dans Identity.
     *
     * @param term terme de recherche, non vide
     * @return les identifiants des utilisateurs correspondants
     */
    List<UUID> searchIds(String term);

    /**
     * Identité d'un utilisateur, réduite à ce dont l'administration a besoin.
     *
     * <p>{@code accountStatus} est traité comme une <b>valeur opaque d'affichage</b> (PENDING,
     * ACTIVE, SUSPENDED…) : Organisation ne dépend pas de l'énumération d'Identity, conformément à
     * la séparation des BC.</p>
     *
     * <p>Pour un utilisateur anonymisé, les champs sont déjà neutralisés à la source par
     * {@code User.anonymize()} — ce port ne réexpose donc jamais de donnée personnelle effacée.</p>
     */
    record UserSummary(
            UUID userId,
            String email,
            String firstName,
            String lastName,
            String accountStatus,
            boolean anonymized
    ) {}
}
