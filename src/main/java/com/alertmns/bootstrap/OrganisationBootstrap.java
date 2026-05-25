package com.alertmns.bootstrap;

import com.alertmns.identity.domain.model.UserStatus;
import com.alertmns.identity.domain.port.incoming.IssueActivationTokenUseCase;
import com.alertmns.identity.domain.port.incoming.RegisterPendingUserUseCase;
import com.alertmns.identity.domain.port.incoming.command.IssueActivationTokenCommand;
import com.alertmns.identity.domain.port.incoming.command.RegisterPendingUserCommand;
import com.alertmns.identity.domain.port.outgoing.UserRepository;
import com.alertmns.organisation.domain.exception.MemberAlreadyExistsException;
import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.model.OrganisationName;
import com.alertmns.organisation.domain.port.incoming.CreateOrganisationUseCase;
import com.alertmns.organisation.domain.port.incoming.InviteMemberUseCase;
import com.alertmns.organisation.domain.port.incoming.command.CreateOrganisationCommand;
import com.alertmns.organisation.domain.port.incoming.command.InviteMemberCommand;
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
 *     <li>Le {@code User} admin initial existe en {@code PENDING} (créé si absent, par email configuré).
 *         À la création, le listener {@code IssueActivationTokenOnUserRegisteredListener} émet automatiquement le
 *         magic-link d'activation.</li>
 *     <li>Le {@code Member} ADMIN correspondant existe en {@code PENDING} (créé si absent).</li>
 *     <li>Si l'admin n'est pas encore {@code ACTIVE}, ré-émet un magic-link d'activation (« self-healing bootstrap » —
 *     sans cela, la perte du mail initial exigerait un redéploiement).</li>
 * </ol>
 *
 * <p><b>Idempotence</b> : chaque étape vérifie l'existence préalable. Aucun doublon n'est créé. Au second démarrage,
 * seules les vérifications passent ; la 4ᵉ étape ré-émet un token <em>tant que</em> l'admin n'est pas {@code ACTIVE},
 * puis devient no-op.</p>
 *
 * <p><b>Création de l'admin</b> : passe par {@link RegisterPendingUserUseCase} qui crée un User en statut
 * {@code PENDING} avec un {@code HashedPassword} sentinelle ({@code HashedPassword.unset()}). Aucun mot de passe réel
 * n'est généré ni stocké. La sentinelle sera remplacée lors du redeem du magic-link par {@code activateWithPassword}.
 * Le User PENDING ne peut pas s'authentifier (cf. {@code DomainUserDetails.isEnabled()}), donc la sentinelle n'est
 * jamais comparée à un mot de passe utilisateur en pratique.</p>
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
    private final RegisterPendingUserUseCase registerPendingUser;
    private final InviteMemberUseCase inviteMember;
    private final IssueActivationTokenUseCase issueActivationToken;

    public OrganisationBootstrap(
            BootstrapProperties properties,
            OrganisationRepository organisationRepository,
            UserRepository userRepository,
            CreateOrganisationUseCase createOrganisation,
            RegisterPendingUserUseCase registerPendingUser,
            InviteMemberUseCase inviteMember,
            IssueActivationTokenUseCase issueActivationToken
    ) {
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
        this.organisationRepository =
                Objects.requireNonNull(organisationRepository, "organisationRepository must not be null");
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository must not be null");
        this.createOrganisation = Objects.requireNonNull(createOrganisation, "createOrganisation must not be null");
        this.registerPendingUser = Objects.requireNonNull(registerPendingUser, "registerPendingUser must not be null");
        this.inviteMember = Objects.requireNonNull(inviteMember, "inviteMember must not be null");
        this.issueActivationToken =
                Objects.requireNonNull(issueActivationToken, "issueActivationToken must not be null");
    }

    public void run() {
        if (!properties.enabled()) {
            log.info("Bootstrap has been disabled (alertmns.bootstrap.enabled=false)");
            return;
        }

        OrganisationId organisationId = ensureOrganisation();
        UserId adminId = ensureAdminUser();
        ensureAdminMember(organisationId, adminId);
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

    private UserId ensureAdminUser() {
        Email email = Email.of(properties.admin().email());
        return userRepository.findByEmail(email)
                .map(u -> {
                    log.info("User already exists with email {}", email.value());
                    return u.id();
                })
                .orElseGet(() -> {
                    log.info("User created with email {}", email.value());
                    return registerPendingUser.register(new RegisterPendingUserCommand(
                            email.value(),
                            properties.admin().firstName(),
                            properties.admin().lastName()
                    ));
                });
    }

    private void ensureAdminMember(OrganisationId organisationId, UserId adminId) {
        try {
            inviteMember.invite(new InviteMemberCommand(
                    organisationId.value().toString(),
                    adminId.value().toString(),
                    MemberRole.ADMIN.name()
            ));
            log.info("Invited admin member {} for organisation {}", adminId, organisationId);
        } catch (MemberAlreadyExistsException e) {
            log.info("Admin member already exists for user {}; skipping", adminId);
        }
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
