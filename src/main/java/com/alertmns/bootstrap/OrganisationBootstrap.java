package com.alertmns.bootstrap;

import com.alertmns.identity.domain.model.UserStatus;
import com.alertmns.identity.domain.port.incoming.IssueActivationTokenUseCase;
import com.alertmns.identity.domain.port.incoming.command.IssueActivationTokenCommand;
import com.alertmns.identity.domain.port.outgoing.UserRepository;
import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.model.OrganisationName;
import com.alertmns.organisation.domain.port.incoming.CreateOrganisationUseCase;
import com.alertmns.organisation.domain.port.incoming.IssueMembershipInvitationUseCase;
import com.alertmns.organisation.domain.port.incoming.command.CreateOrganisationCommand;
import com.alertmns.organisation.domain.port.incoming.command.IssueMembershipInvitationCommand;
import com.alertmns.organisation.domain.port.outgoing.OrganisationRepository;
import com.alertmns.shared.Email;
import com.alertmns.shared.OrganisationId;
import com.alertmns.shared.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Composant de bootstrap idempotent au démarrage applicatif.
 *
 * <p>À chaque appel à {@link #run()}, garantit, dans cet ordre :</p>
 * <ol>
 *     <li>L'unique {@code Organisation} existe (créée si absente, par nom configuré).</li>
 *     <li>L'admin initial est invité (création de l'invitation + User PENDING si absent ; no-op si User déjà
 *     présent).</li>
 *     <li>Si l'admin n'est pas encore {@code ACTIVE}, réémet un magic-link d'activation (self-healing).</li>
 * </ol>
 *
 * <p><b>Idempotence</b> : chaque étape vérifie l'existence préalable. Aucun doublon n'est créé. Au second démarrage,
 * seules les vérifications passent ; la 3ᵉ étape réémet un token <em>tant que</em> l'admin n'est pas {@code ACTIVE},
 * puis devient no-op.</p>
 *
 * <p><b>Activation/désactivation</b> : via la propriété {@code alertmns.bootstrap.enabled}. Désactivé en environnement
 * de test via {@code application-test.yml}.</p>
 */
public final class OrganisationBootstrap {

    private static final Logger log = LoggerFactory.getLogger(OrganisationBootstrap.class);

    private final BootstrapProperties properties;
    private final OrganisationRepository organisationRepository;
    private final UserRepository userRepository;
    private final CreateOrganisationUseCase createOrganisation;
    private final IssueMembershipInvitationUseCase issueMembershipInvitation;
    private final IssueActivationTokenUseCase issueActivationToken;

    public OrganisationBootstrap(
            BootstrapProperties properties,
            OrganisationRepository organisationRepository,
            UserRepository userRepository,
            CreateOrganisationUseCase createOrganisation,
            IssueMembershipInvitationUseCase issueMembershipInvitation,
            IssueActivationTokenUseCase issueActivationToken
    ) {
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
        this.organisationRepository = Objects.requireNonNull(
                organisationRepository, "organisationRepository must not be null");
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository must not be null");
        this.createOrganisation = Objects.requireNonNull(createOrganisation, "createOrganisation must not be null");
        this.issueMembershipInvitation = Objects.requireNonNull(
                issueMembershipInvitation, "issueMembershipInvitation must not be null");
        this.issueActivationToken = Objects.requireNonNull(
                issueActivationToken, "issueActivationToken must not be null");
    }

    public void run() {
        if (!properties.enabled()) {
            log.info("Bootstrap has been disabled (alertmns.bootstrap.enabled=false)");
            return;
        }

        OrganisationId organisationId = ensureOrganisation();
        UserId adminId = ensureAdminInvitation(organisationId);
        reissueMagicLinkIfAdminNotActive(adminId);
    }

    private OrganisationId ensureOrganisation() {
        OrganisationName name = OrganisationName.of(properties.organisation().name());
        return organisationRepository.findByName(name)
                .map(o -> {
                    log.info("Organisation already exists with name {}", name.value());
                    return o.id();
                })
                .orElseGet(() -> {
                    log.info("Organisation created with name {}", name.value());
                    return createOrganisation.create(new CreateOrganisationCommand(name.value()));
                });
    }

    private UserId ensureAdminInvitation(OrganisationId organisationId) {
        Email email = Email.of(properties.admin().email());
        return userRepository.findByEmail(email)
                .map(u -> {
                    log.info("Admin user already exists with email {}; skipping invitation", email.value());
                    return u.id();
                })
                .orElseGet(() -> {
                    log.info("Issuing initial admin invitation for {}", email.value());
                    issueMembershipInvitation.issue(new IssueMembershipInvitationCommand(
                            organisationId.value().toString(),
                            email.value(),
                            properties.admin().firstName(),
                            properties.admin().lastName(),
                            MemberRole.ADMIN.name()
                    ));
                    return userRepository.findByEmail(email)
                            .orElseThrow(() -> new IllegalStateException(
                                    "User must exist after IssueMembershipInvitation: " + email.value()))
                            .id();
                });
    }

    private void reissueMagicLinkIfAdminNotActive(UserId adminId) {
        boolean isActive = userRepository.findById(adminId)
                .map(u -> u.status() == UserStatus.ACTIVE)
                .orElse(false);

        if (isActive) {
            log.info("Initial admin user is already ACTIVE; skipping magic-link reissue");
            return;
        }
        log.info("Initial admin user is not ACTIVE yet; reissuing magic-link");
        issueActivationToken.issue(new IssueActivationTokenCommand(adminId.value().toString()));
    }
}
