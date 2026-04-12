package com.alertmns.organisation.domain.port.outgoing;

import com.alertmns.organisation.domain.model.Organisation;
import com.alertmns.organisation.domain.model.OrganisationName;
import com.alertmns.shared.OrganisationId;

import java.util.Optional;

/**
 * Port sortant pour la persistance des organisations.
 */
public interface OrganisationRepository {

    /**
     * Sauvegarde une organisation (création ou mise à jour).
     *
     * @param organisation l'organisation à sauvegarder
     */
    void save(Organisation organisation);

    /**
     * Recherche une organisation par son identifiant.
     *
     * @param id l'identifiant de l'organisation
     * @return l'organisation trouvée, ou vide
     */
    Optional<Organisation> findById(OrganisationId id);

    /**
     * Recherche une organisation par son nom.
     *
     * @param name le nom recherché
     * @return l'organisation trouvée, ou vide
     */
    Optional<Organisation> findByName(OrganisationName name);

    /**
     * Vérifie si un nom d'organisation est déjà utilisé.
     *
     * @param name le nom à vérifier
     * @return {@code true} si le nom existe déjà
     */
    boolean existsByName(OrganisationName name);
}
