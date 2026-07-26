package com.alertmns.identity.infrastructure.adapter.outgoing.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {

    Optional<UserJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    List<UserJpaEntity> findByIdIn(Collection<UUID> ids);

    /**
     * Recherche textuelle sur le prénom, le nom ou l'e-mail, insensible à la casse.
     *
     * <p>Sert l'annuaire consommé par le BC Organisation pour filtrer une liste de membres sur un
     * critère qui n'existe que dans Identity.</p>
     */
    @Query("""
            select u.id from UserJpaEntity u
            where lower(u.firstName) like lower(concat('%', :term, '%'))
               or lower(u.lastName) like lower(concat('%', :term, '%'))
               or lower(u.email) like lower(concat('%', :term, '%'))
            """)
    List<UUID> searchIds(@Param("term") String term);
}
