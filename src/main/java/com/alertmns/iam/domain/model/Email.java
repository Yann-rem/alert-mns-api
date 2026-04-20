package com.alertmns.iam.domain.model;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object représentant l'adresse email d'un utilisateur.
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
     * <p>La valeur est normalisée (trim + lowercase) avant validation.</p>
     *
     * @param value la valeur brute
     * @return l'email validé
     * @throws IllegalArgumentException si le format est invalide ou dépasse 254 caractères
     */
    public static Email of(String value) {
        return new Email(value);
    }
}
