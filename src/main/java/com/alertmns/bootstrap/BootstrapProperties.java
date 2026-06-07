package com.alertmns.bootstrap;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration du bootstrap au démarrage applicatif.
 *
 * <p>Lue depuis le préfixe {@code alertmns.bootstrap} dans {@code application.yml}.</p>
 *
 * <p><b>Kill-switch</b> : la propriété {@code enabled} agit comme interrupteur global. Positionnée {@code false} dans
 * {@code application-test.yml} pour empêcher le bootstrap d'interférer avec les tests (qui créent leurs fixtures
 * explicitement). En prod, {@code true} (valeur par défaut via {@code @ConditionalOnProperty(matchIfMissing = true)}
 * côté {@link BootstrapBeanConfig}).</p>
 *
 * @param enabled      interrupteur global du bootstrap
 * @param organisation paramètres de l'unique organisation à provisionner
 * @param generalGroup paramètres du canal général provisionné au bootstrap
 * @param admin        paramètres de l'admin initial
 */
@ConfigurationProperties(prefix = "alertmns.bootstrap")
public record BootstrapProperties(
        boolean enabled,
        Organisation organisation,
        GeneralGroup generalGroup,
        Admin admin
) {

    /**
     * @param name nom métier de l'organisation (unique)
     */
    public record Organisation(@NotBlank String name) {}

    /**
     * @param name nom du canal général provisionné au bootstrap
     */
    public record GeneralGroup(@NotBlank String name) {}

    /**
     * @param email     email de l'admin initial
     * @param firstName prénom de l'admin initial
     * @param lastName  nom de l'admin initial
     */
    public record Admin(
            @NotBlank String email,
            @NotBlank String firstName,
            @NotBlank String lastName
    ) {}
}
