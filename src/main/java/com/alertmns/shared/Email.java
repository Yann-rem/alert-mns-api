package com.alertmns.shared;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object représentant une adresse email, partagé par plusieurs Bounded Contexts.
 *
 * <p>Réside dans le <em>shared kernel</em> car la notion d'email n'est pas exclusive à un BC :</p>
 * <ul>
 *   <li>Identity : identifiant d'authentification de l'agrégat {@code User}.</li>
 *   <li>Organisation : destinataire d'une {@code MembershipInvitation} (D17), avant qu'un User
 *       n'existe en BD.</li>
 * </ul>
 *
 * <p>La valeur est normalisée à la construction : {@code strip()} pour retirer les espaces de
 * début/fin, puis {@code toLowerCase()} pour garantir l'unicité (deux emails ne différant que par
 * la casse représentent la même boîte). Validation par regex permissive + limite de longueur
 * (RFC 5321 max 254 caractères).</p>
 */
public record Email(String value) {

    private static final int MAX_LENGTH = 254;
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$");

    public Email {
        Objects.requireNonNull(value, "email must not be null");
        value = value.strip().toLowerCase();
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("email must not exceed " + MAX_LENGTH + " characters");
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("email format is invalid: " + value);
        }
    }

    /**
     * Crée un email à partir d'une valeur brute.
     *
     * <p>La valeur est normalisée (strip + lowercase) avant validation.</p>
     *
     * @param value la valeur brute
     * @return l'email validé
     * @throws IllegalArgumentException si le format est invalide ou dépasse 254 caractères
     */
    public static Email of(String value) {
        return new Email(value);
    }
}
