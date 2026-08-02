package com.alertmns.organisation.infrastructure.adapter.outgoing.persistence;

import com.alertmns.organisation.domain.model.GroupKind;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupJpaRepository extends JpaRepository<GroupJpaEntity, UUID> {

    Optional<GroupJpaEntity> findByOrganisationIdAndKind(UUID organisationId, GroupKind kind);

    boolean existsByOrganisationIdAndName(UUID organisationId, String name);

    boolean existsByOrganisationIdAndKind(UUID organisationId, GroupKind kind);

    /**
     * Liste filtrée des groupes. L'appelant fournit un <b>motif LIKE déjà construit</b> ({@code %}
     * pour « pas de filtre ») plutôt qu'un terme nullable : un paramètre {@code null} à l'intérieur
     * d'un {@code concat()} empêche Postgres d'inférer son type et fait échouer la requête.
     */
    @Query("""
            select g from GroupJpaEntity g
            where g.organisationId = :organisationId
              and lower(g.name) like lower(:pattern) escape '!'
            order by g.name asc, g.id asc
            """)
    List<GroupJpaEntity> findFiltered(
            @Param("organisationId") UUID organisationId,
            @Param("pattern") String pattern,
            Pageable pageable);

    /** Compte les groupes correspondant au même motif que {@link #findFiltered}. */
    @Query("""
            select count(g) from GroupJpaEntity g
            where g.organisationId = :organisationId
              and lower(g.name) like lower(:pattern) escape '!'
            """)
    long countFiltered(
            @Param("organisationId") UUID organisationId,
            @Param("pattern") String pattern);
}
