package com.alertmns.bootstrap;

import com.alertmns.identity.domain.port.incoming.IssueActivationTokenUseCase;
import com.alertmns.identity.domain.port.outgoing.UserRepository;
import com.alertmns.organisation.domain.port.incoming.CreateGeneralGroupUseCase;
import com.alertmns.organisation.domain.port.incoming.CreateOrganisationUseCase;
import com.alertmns.organisation.domain.port.incoming.IssueMembershipInvitationUseCase;
import com.alertmns.organisation.domain.port.outgoing.OrganisationRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration Spring du module bootstrap.
 *
 * <p>Active le binding des {@link BootstrapProperties}, déclare le composant {@link OrganisationBootstrap}, et le
 * déclenche au démarrage applicatif via un {@link ApplicationListener} sur {@link ApplicationReadyEvent} — choix qui
 * garde {@code OrganisationBootstrap} indépendant du cycle Spring (testable hors contexte).</p>
 *
 * <p>L'annotation {@link ConditionalOnProperty} permet de désactiver l'ensemble en test via
 * {@code alertmns.bootstrap.enabled=false}.</p>
 */
@Configuration
@EnableConfigurationProperties(BootstrapProperties.class)
@ConditionalOnProperty(name = "alertmns.bootstrap.enabled", havingValue = "true", matchIfMissing = true)
public class BootstrapBeanConfig {

    @Bean
    public OrganisationBootstrap organisationBootstrap(
            BootstrapProperties properties,
            OrganisationRepository organisationRepository,
            UserRepository userRepository,
            CreateOrganisationUseCase createOrganisation,
            CreateGeneralGroupUseCase createGeneralGroup,
            IssueMembershipInvitationUseCase issueMembershipInvitation,
            IssueActivationTokenUseCase issueActivationToken
    ) {
        return new OrganisationBootstrap(
                properties,
                organisationRepository,
                userRepository,
                createOrganisation,
                createGeneralGroup,
                issueMembershipInvitation,
                issueActivationToken
        );
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> bootstrapRunner(OrganisationBootstrap bootstrap) {
        return event -> bootstrap.run();
    }
}
