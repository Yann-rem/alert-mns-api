package com.alertmns.iam.domain.model;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object représentant l'adresse email d'un utilisateur.
 */
public final class Email {

    private static final int MAX_LENGTH = 254;
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$");

    private final String value;

    private Email(String value) {
        Objects.requireNonNull(value, "email must not be null");
        String normalized = value.trim().toLowerCase();
        if (normalized.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("email must not exceed " + MAX_LENGTH + " characters");
        }
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("email format is invalid: " + normalized);
        }
        this.value = normalized;
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

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(value, email.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
