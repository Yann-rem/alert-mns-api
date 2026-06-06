package com.alertmns.organisation.domain.port.outgoing;

import com.alertmns.organisation.domain.model.Group;
import com.alertmns.organisation.domain.model.GroupId;
import com.alertmns.organisation.domain.model.GroupKind;
import com.alertmns.organisation.domain.model.GroupName;
import com.alertmns.shared.OrganisationId;

import java.util.Optional;

/**
 * Port sortant pour la persistance des groupes d'une organisation.
 */
public interface GroupRepository {

    /**
     * Sauvegarde un groupe (création ou mise à jour).
     *
     * @param group le groupe à sauvegarder
     */
    void save(Group group);

    /**
     * Recherche un groupe par son identifiant.
     *
     * @param id l'identifiant du groupe
     * @return le groupe trouvé, ou vide
     */
    Optional<Group> findById(GroupId id);

    /**
     * Recherche le groupe {@link GroupKind#GENERAL} d'une organisation.
     *
     * @param organisationId l'identifiant de l'organisation
     * @return le groupe général de l'organisation, ou vide s'il n'existe pas encore
     */
    Optional<Group> findGeneralByOrganisationId(OrganisationId organisationId);

    /**
     * Vérifie si un nom de groupe est déjà utilisé au sein d'une organisation.
     *
     * @param organisationId l'identifiant de l'organisation
     * @param name           le nom recherché
     * @return {@code true} si le nom existe déjà dans cette organisation
     */
    boolean existsByOrganisationIdAndGroupName(OrganisationId organisationId, GroupName name);

    /**
     * Vérifie si une organisation possède déjà son groupe {@link GroupKind#GENERAL}.
     *
     * @param organisationId l'identifiant de l'organisation
     * @return {@code true} si un groupe général existe déjà pour cette organisation
     */
    boolean existsGeneralByOrganisationId(OrganisationId organisationId);
}
