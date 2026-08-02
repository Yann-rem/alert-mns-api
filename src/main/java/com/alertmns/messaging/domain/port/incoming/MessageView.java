package com.alertmns.messaging.domain.port.incoming;

import com.alertmns.messaging.domain.model.Message;

import java.util.Objects;

/**
 * Message accompagné du nom de son auteur, pour la lecture.
 *
 * <p>Le nom n'appartient pas à l'agrégat {@link Message}, qui ne référence qu'un {@code memberId}
 * opaque : il est résolu à la lecture (ADR-0017 §2). Ce couple évite de le dénormaliser tout en
 * épargnant au client un second appel pour chaque auteur.</p>
 */
public record MessageView(Message message, String authorName) {

    public MessageView {
        Objects.requireNonNull(message, "message must not be null");
        Objects.requireNonNull(authorName, "authorName must not be null");
    }
}
