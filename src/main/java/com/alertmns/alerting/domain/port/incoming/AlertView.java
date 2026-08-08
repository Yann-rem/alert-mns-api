package com.alertmns.alerting.domain.port.incoming;

import com.alertmns.alerting.domain.model.Alert;

import java.util.Objects;

/**
 * Modèle de lecture d'une alerte : l'agrégat, augmenté des libellés que son lecteur ne peut pas résoudre lui-même.
 *
 * <p>Ces noms n'ont pas leur place dans l'agrégat. {@link Alert} ne référence que des identifiants, et les
 * dénormaliser figerait un nom que l'anonymisation doit pouvoir effacer (ADR-0017 §2). Ils sont donc résolus à la
 * lecture, dans un modèle dédié — même parti pris que {@code ConversationSummary} côté Messaging.</p>
 *
 * @param alert      l'alerte
 * @param issuerName nom d'affichage de l'émetteur, jamais {@code null}
 * @param groupName  nom du groupe ciblé, {@code null} pour une alerte visant toute l'organisation
 */
public record AlertView(Alert alert, String issuerName, String groupName) {

    public AlertView {
        Objects.requireNonNull(alert, "alert must not be null");
        Objects.requireNonNull(issuerName, "issuerName must not be null");
    }
}
