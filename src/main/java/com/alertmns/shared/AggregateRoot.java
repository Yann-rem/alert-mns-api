package com.alertmns.shared;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe de base pour tous les agrégats racines du domaine.
 *
 * <p>Un agrégat racine accumule des événements de domaine au fil de ses mutations
 * métier via {@link #registerEvent(DomainEvent)}. Ces événements sont ensuite
 * récupérés et vidés par {@link #pullDomainEvents()}, typiquement par le service
 * applicatif après la sauvegarde de l'agrégat.</p>
 */
public abstract class AggregateRoot {

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    /**
     * Enregistre un événement de domaine survenu lors d'une mutation métier.
     *
     * @param event l'événement à enregistrer
     */
    protected void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    /**
     * Retourne et vide la liste des événements de domaine accumulés.
     *
     * <p>Chaque appel vide la liste interne : les événements ne sont retournés qu'une seule fois.</p>
     *
     * @return une copie immuable des événements enregistrés
     */
    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }
}
