package com.alertmns.organisation.infrastructure.adapter.outgoing.persistence;

import com.alertmns.organisation.domain.model.Group;
import com.alertmns.organisation.domain.model.GroupId;
import com.alertmns.organisation.domain.model.GroupKind;
import com.alertmns.organisation.domain.model.GroupName;
import com.alertmns.organisation.domain.port.outgoing.GroupRepository;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.mapper.GroupPersistenceMapper;
import com.alertmns.shared.OrganisationId;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

public final class GroupPersistenceAdapter implements GroupRepository {

    private final GroupJpaRepository jpaRepository;

    public GroupPersistenceAdapter(GroupJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Group group) {
        jpaRepository.save(GroupPersistenceMapper.toEntity(group));
    }

    @Override
    public Optional<Group> findById(GroupId id) {
        return jpaRepository
                .findById(id.value())
                .map(GroupPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Group> findGeneralByOrganisationId(OrganisationId organisationId) {
        return jpaRepository
                .findByOrganisationIdAndKind(organisationId.value(), GroupKind.GENERAL)
                .map(GroupPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByOrganisationIdAndGroupName(OrganisationId organisationId, GroupName name) {
        return jpaRepository.existsByOrganisationIdAndName(organisationId.value(), name.value());
    }

    @Override
    public boolean existsGeneralByOrganisationId(OrganisationId organisationId) {
        return jpaRepository.existsByOrganisationIdAndKind(organisationId.value(), GroupKind.GENERAL);
    }

    @Override
    public List<Group> findByOrganisationId(
            OrganisationId organisationId, String search, int page, int size) {
        return jpaRepository
                .findFiltered(organisationId.value(), likePattern(search), PageRequest.of(page, size))
                .stream()
                .map(GroupPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public long countByOrganisationId(OrganisationId organisationId, String search) {
        return jpaRepository.countFiltered(organisationId.value(), likePattern(search));
    }

    /**
     * Traduit un terme de recherche en motif LIKE. Absence de terme → {@code %}, qui laisse tout
     * passer. Les caractères jokers saisis par l'utilisateur sont échappés pour rester littéraux.
     */
    private static String likePattern(String search) {
        if (search == null || search.isBlank()) {
            return "%";
        }
        String escaped = search.trim().replace("!", "!!").replace("%", "!%").replace("_", "!_");
        return "%" + escaped + "%";
    }
}
