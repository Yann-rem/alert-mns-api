package com.alertmns.identity.domain.event;

import com.alertmns.shared.DomainEvent;
import com.alertmns.shared.UserId;

import java.time.Instant;

/**
 * Événement de domaine représentant l'anonymisation d'un compte utilisateur (droit à l'effacement, article 17 RGPD).
 *
 * <p>Les données personnelles ({@code email}, profil, mot de passe) ont été remplacées par des placeholders
 * non-identifiants ; le {@code userId} est conservé comme clé de référence opaque. L'anonymisation est orthogonale au
 * statut d'accès (cf. ADR-0017).</p>
 */
public record UserAnonymized(UserId userId, Instant occurredOn) implements DomainEvent {}
