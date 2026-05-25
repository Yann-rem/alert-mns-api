package com.alertmns.bootstrap;

import com.alertmns.identity.domain.model.Email;
import com.alertmns.identity.domain.model.UserStatus;
import com.alertmns.identity.domain.port.incoming.IssueActivationTokenUseCase;
import com.alertmns.identity.domain.port.incoming.RegisterUserUseCase;
import com.alertmns.identity.domain.port.incoming.command.IssueActivationTokenCommand;
import com.alertmns.identity.domain.port.incoming.command.RegisterUserCommand;
import com.alertmns.identity.domain.port.outgoing.UserRepository;
import com.alertmns.organisation.domain.exception.MemberAlreadyExistsException;
import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.model.OrganisationName;
import com.alertmns.organisation.domain.port.incoming.CreateOrganisationUseCase;
import com.alertmns.organisation.domain.port.incoming.InviteMemberUseCase;
import com.alertmns.organisation.domain.port.incoming.command.CreateOrganisationCommand;
import com.alertmns.organisation.domain.port.incoming.command.InviteMemberCommand;
import com.alertmns.organisation.domain.port.outgoing.OrganisationRepository;
import com.alertmns.shared.OrganisationId;
import com.alertmns.shared.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Base64;
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
 * <p><b>Sécurité du mot de passe initial</b> : un mot de passe aléatoire jetable ({@value #RANDOM_PASSWORD_BYTES}
 * octets, encodé Base64Url) est généré pour la création du User. Il n'est jamais persisté en clair, jamais loggé,
 * jamais retourné. Il sera écrasé lors du redeem du magic-link par {@code User.activateWithPassword}.</p>
 *
 * <p><b>Dette explicite</b> : à l'occasion de l'introduction de {@code MembershipInvitation}, remplacer ce mécanisme
 * par un {@code RegisterPendingUserUseCase(email, profile)} qui ne prend pas de mot de passe. Le random password
 * ci-dessous est un contournement pragmatique du contrat actuel de {@link RegisterUserUseCase}.</p>
 *
 * <p><b>Activation/désactivation</b> : via la propriété {@code alertmns.bootstrap.enabled}. Désactivé en environnement
 * de test via {@code application-test.yml}.</p>
 */
public final class OrganisationBootstrap {

    private static final Logger log = LoggerFactory.getLogger(OrganisationBootstrap.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int RANDOM_PASSWORD_BYTES = 32;

    private final BootstrapProperties properties;
    private final OrganisationRepository organisationRepository;
    private final UserRepository userRepository;
    private final CreateOrganisationUseCase createOrganisation;
    private final RegisterUserUseCase registerUser;
    private final InviteMemberUseCase inviteMember;
    private final IssueActivationTokenUseCase issueActivationToken;

    public OrganisationBootstrap(
            BootstrapProperties properties,
            OrganisationRepository organisationRepository,
            UserRepository userRepository,
            CreateOrganisationUseCase createOrganisation,
            RegisterUserUseCase registerUser,
            InviteMemberUseCase inviteMember,
            IssueActivationTokenUseCase issueActivationToken
    ) {
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
        this.organisationRepository =
                Objects.requireNonNull(organisationRepository, "organisationRepository must not be null");
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository must not be null");
        this.createOrganisation = Objects.requireNonNull(createOrganisation, "createOrganisation must not be null");
        this.registerUser = Objects.requireNonNull(registerUser, "registerUser must not be null");
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
                    return registerUser.register(new RegisterUserCommand(
                            email.value(),
                            generateRandomPassword(),
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

    private static String generateRandomPassword() {
        byte[] bytes = new byte[RANDOM_PASSWORD_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
