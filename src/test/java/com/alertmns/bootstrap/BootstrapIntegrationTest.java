package com.alertmns.bootstrap;

import com.alertmns.shared.Email;
import com.alertmns.identity.domain.model.RawPassword;
import com.alertmns.identity.domain.model.User;
import com.alertmns.identity.domain.model.UserStatus;
import com.alertmns.identity.domain.port.outgoing.MailerPort;
import com.alertmns.identity.domain.port.outgoing.PasswordHasher;
import com.alertmns.identity.domain.port.outgoing.UserRepository;
import com.alertmns.identity.infrastructure.adapter.outgoing.persistence.ActivationTokenJpaEntity;
import com.alertmns.identity.infrastructure.adapter.outgoing.persistence.ActivationTokenJpaRepository;
import com.alertmns.identity.infrastructure.adapter.outgoing.persistence.UserJpaEntity;
import com.alertmns.identity.infrastructure.adapter.outgoing.persistence.UserJpaRepository;
import com.alertmns.organisation.domain.model.GroupKind;
import com.alertmns.organisation.domain.model.MembershipInvitationStatus;
import com.alertmns.organisation.domain.model.OrganisationName;
import com.alertmns.organisation.domain.port.outgoing.OrganisationRepository;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.GroupJpaEntity;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.GroupJpaRepository;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.MemberJpaRepository;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.MembershipInvitationJpaRepository;
import com.alertmns.organisation.infrastructure.adapter.outgoing.persistence.OrganisationJpaRepository;
import com.alertmns.shared.OrganisationId;
import com.alertmns.shared.UserId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

/**
 * Test d'intégration du bootstrap au démarrage applicatif (ADR-0010).
 *
 * <p>Réactive le bootstrap via {@code @TestPropertySource} (le profil {@code test} le désactive
 * par défaut, cf. {@code application-test.yml}). Force des valeurs déterministes pour le nom
 * d'organisation et l'email d'admin afin de ne dépendre d'aucune valeur de prod.</p>
 *
 * <p>Au moment où le contexte Spring est prêt, le bootstrap a déjà tourné une première fois
 * (déclenché par {@code ApplicationReadyEvent}). Les tests vérifient l'état post-bootstrap, puis
 * appellent à nouveau {@code bootstrap.run()} pour valider l'idempotence.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "alertmns.bootstrap.enabled=true",
        "alertmns.bootstrap.organisation.name=Bootstrap Test Org",
        "alertmns.bootstrap.general-group.name=Canal Général Test",
        "alertmns.bootstrap.admin.email=bootstrap.admin@alertmns.local",
        "alertmns.bootstrap.admin.first-name=Bootstrap",
        "alertmns.bootstrap.admin.last-name=Admin"
})
class BootstrapIntegrationTest {

    private static final String EXPECTED_ORG_NAME = "Bootstrap Test Org";
    private static final String EXPECTED_GENERAL_GROUP_NAME = "Canal Général Test";
    private static final String EXPECTED_ADMIN_EMAIL = "bootstrap.admin@alertmns.local";

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @MockitoBean
    private MailerPort mailer;

    @Autowired
    private OrganisationBootstrap bootstrap;

    @Autowired
    private OrganisationRepository organisationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganisationJpaRepository organisationJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    @Autowired
    private MembershipInvitationJpaRepository membershipInvitationJpaRepository;

    @Autowired
    private GroupJpaRepository groupJpaRepository;

    @Autowired
    private ActivationTokenJpaRepository activationTokenJpaRepository;

    @Autowired
    private PasswordHasher passwordHasher;

    /**
     * Garantit que chaque test démarre dans l'état post-premier-boot, indépendamment de l'ordre d'exécution.
     *
     * <p>Le bootstrap a tourné une fois au démarrage du contexte Spring, mais le {@link AfterEach} le nettoie : un
     * appel idempotent en {@link BeforeEach} restitue l'état attendu sans casser le caractère "test du composant
     * bootstrap" — c'est précisément le scénario opérationnel (redémarrage applicatif sur BD vide).</p>
     */
    @BeforeEach
    void ensureFreshBootstrap() {
        Mockito.clearInvocations(mailer);
        bootstrap.run();
    }

    @AfterEach
    void cleanDatabase() {
        activationTokenJpaRepository.deleteAll();
        membershipInvitationJpaRepository.deleteAll();
        memberJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
        groupJpaRepository.deleteAll();
        organisationJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("First boot provisions the organisation, the admin user PENDING, a PENDING invitation, and emits a magic-link (no Member yet)")
    void firstBootProvisionsAllAggregates() {
        // Le bootstrap a tourné automatiquement au démarrage du contexte (ApplicationReadyEvent).

        assertThat(organisationRepository.findByName(OrganisationName.of(EXPECTED_ORG_NAME)))
                .as("Organisation must exist with the configured name")
                .isPresent();

        UserJpaEntity admin = userJpaRepository.findByEmail(EXPECTED_ADMIN_EMAIL).orElseThrow();
        assertThat(admin.getStatus()).isEqualTo(UserStatus.PENDING);

        // Depuis D17 : le bootstrap n'invite plus directement de Member. C'est la cascade
        // UserActivated → AcceptMembershipInvitation qui le créera quand l'admin cliquera son magic-link.
        assertThat(memberJpaRepository.findByUserId(admin.getId()))
                .as("No Member yet — the cascade will create it on activation")
                .isEmpty();

        assertThat(membershipInvitationJpaRepository.findByInvitedEmailAndStatus(
                EXPECTED_ADMIN_EMAIL, MembershipInvitationStatus.PENDING))
                .as("A PENDING invitation must have been issued for the admin email")
                .isPresent();

        assertThat(activationTokenJpaRepository.findAll())
                .as("A magic-link activation token must have been emitted")
                .hasSize(1);

        verify(mailer, atLeastOnce()).sendActivationEmail(any(Email.class), any(), any(URI.class));
    }

    @Test
    @DisplayName("First boot provisions the GENERAL group for the organisation with the configured name")
    void firstBootProvisionsGeneralGroup() {
        OrganisationId orgId = organisationRepository.findByName(OrganisationName.of(EXPECTED_ORG_NAME))
                .orElseThrow().id();

        GroupJpaEntity general = groupJpaRepository
                .findByOrganisationIdAndKind(orgId.value(), GroupKind.GENERAL)
                .orElseThrow();
        assertThat(general.getName()).isEqualTo(EXPECTED_GENERAL_GROUP_NAME);
    }

    @Test
    @DisplayName("Second boot is idempotent: no duplicate aggregates, magic-link reissued because admin is still PENDING")
    void secondBootIsIdempotentAndReissuesMagicLink() {
        // État initial : le bootstrap a tourné une fois au démarrage du contexte.
        String firstTokenHash = activationTokenJpaRepository.findAll().getFirst().getHash();
        long orgCountBefore = organisationJpaRepository.count();
        long userCountBefore = userJpaRepository.count();
        long memberCountBefore = memberJpaRepository.count();
        long groupCountBefore = groupJpaRepository.count();

        Mockito.clearInvocations(mailer);
        bootstrap.run();

        assertThat(organisationJpaRepository.count())
                .as("No duplicate organisation must be created")
                .isEqualTo(orgCountBefore);
        assertThat(userJpaRepository.count())
                .as("No duplicate admin user must be created")
                .isEqualTo(userCountBefore);
        assertThat(memberJpaRepository.count())
                .as("No duplicate admin member must be created")
                .isEqualTo(memberCountBefore);
        assertThat(groupJpaRepository.count())
                .as("No duplicate group must be created")
                .isEqualTo(groupCountBefore);

        List<ActivationTokenJpaEntity> tokens = activationTokenJpaRepository.findAll();
        assertThat(tokens)
                .as("Exactly one activation token must remain after reissue (previous deleted)")
                .hasSize(1);
        assertThat(tokens.getFirst().getHash())
                .as("The token must be fresh (different hash from the previous one)")
                .isNotEqualTo(firstTokenHash);

        verify(mailer, atLeastOnce()).sendActivationEmail(any(Email.class), any(), any(URI.class));
    }

    @Test
    @DisplayName("Once the admin is ACTIVE, the bootstrap no longer reissues a magic-link")
    void shouldNotReissueWhenAdminIsActive() {
        UserJpaEntity adminEntity = userJpaRepository.findByEmail(EXPECTED_ADMIN_EMAIL).orElseThrow();
        User admin = userRepository.findById(UserId.from(adminEntity.getId())).orElseThrow();
        // Active l'admin de la même façon que le redeem du magic-link l'aurait fait, sans passer par le flux web.
        admin.activateWithPassword(passwordHasher.hash(RawPassword.of("chosen-password-1234")), Instant.now());
        userRepository.save(admin);
        activationTokenJpaRepository.deleteAll();

        Mockito.clearInvocations(mailer);
        bootstrap.run();

        assertThat(activationTokenJpaRepository.findAll())
                .as("No new token must be emitted since the admin is already ACTIVE")
                .isEmpty();
        Mockito.verifyNoInteractions(mailer);
    }
}
