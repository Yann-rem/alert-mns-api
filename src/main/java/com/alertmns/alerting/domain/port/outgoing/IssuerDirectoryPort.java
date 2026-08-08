package com.alertmns.alerting.domain.port.outgoing;

import java.util.UUID;

/**
 * Port sortant d'anti-corruption résolvant le nom d'affichage de l'émetteur d'une alerte.
 *
 * <p>Le BC Alerting ne connaît de l'émetteur qu'un {@code memberId} opaque, et son lecteur ne peut pas le traduire :
 * l'annuaire des membres est réservé aux ADMIN. Sans ce port, une alerte n'exposerait qu'un identifiant technique.</p>
 *
 * <p><b>Écart assumé avec le BC Messaging</b>, qui déclare deux ports (Organisation puis Identity) composés dans un
 * résolveur applicatif : Alerting n'a besoin que du nom, la traversée memberId → userId → nom n'est donc pas un
 * service applicatif mais un détail d'implémentation, confiné à l'adaptateur. Le port reste taillé sur le besoin réel
 * de son client (ISP).</p>
 *
 * <p>Résolution <b>au runtime et jamais dénormalisée</b> (ADR-0017 §2) : un membre anonymisé ou disparu devient
 * {@value #DELETED_USER_DISPLAY_NAME} partout, sans migration de données.</p>
 */
public interface IssuerDirectoryPort {

    /** Nom d'affichage renvoyé pour un émetteur anonymisé, supprimé ou introuvable. */
    String DELETED_USER_DISPLAY_NAME = "Utilisateur supprimé";

    /**
     * @param issuerId l'identifiant de membre de l'émetteur, éventuellement {@code null}
     * @return son nom d'affichage, ou {@value #DELETED_USER_DISPLAY_NAME} s'il est anonymisé, introuvable ou absent
     */
    String displayNameOf(UUID issuerId);
}
