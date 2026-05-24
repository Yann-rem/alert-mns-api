package com.alertmns.iam.infrastructure.adapter.incoming.web.auth;

import com.alertmns.iam.application.RegisterUserService;
import com.alertmns.iam.application.SuspendUserService;
import com.alertmns.iam.domain.model.RawPassword;
import com.alertmns.iam.domain.model.User;
import com.alertmns.iam.domain.port.incoming.command.RegisterUserCommand;
import com.alertmns.iam.domain.port.incoming.command.SuspendUserCommand;
import com.alertmns.iam.domain.port.outgoing.PasswordHasher;
import com.alertmns.iam.domain.port.outgoing.UserRepository;
import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.port.incoming.InviteMemberUseCase;
import com.alertmns.organisation.domain.port.incoming.command.InviteMemberCommand;
import com.alertmns.shared.UserId;

/**
 * Factory de test créant des utilisateurs dans différents états via les use cases du domaine.
 *
 * <p>Reste DDD-pur autant que possible : on passe par les services applicatifs et les méthodes de l'agrégat. Si demain
 * les transitions d'état changent, les tests reflètent le vrai comportement métier.</p>
 *
 * <p><b>Sourcing des autorités Spring Security (D14)</b> : depuis ADR-0012, les rôles sont portés par le BC
 * Organisation via {@code Member.role}. Les méthodes {@link #registerActive(String, String)} et
 * {@link #registerActiveAdmin(String, String)} créent donc un {@code Member} rattaché à l'utilisateur via
 * {@link InviteMemberUseCase}, faute de quoi les {@code @PreAuthorize("hasRole(...)")} ne verraient aucune autorité.
 * L'organisation cible est référencée par {@link #DEFAULT_ORGANISATION_ID} (UUID conventionnel pour les tests).</p>
 */
public final class TestUserFactory {

    /**
     * UUID conventionnel servant d'identifiant d'organisation pour les fixtures de test. Aucun agrégat
     * {@code Organisation} n'est créé en base — {@code InviteMemberUseCase} n'enforce pas l'existence préalable
     * de l'organisation, et les tests n'en ont pas besoin pour valider l'authentification/autorisation.
     */
    public static final String DEFAULT_ORGANISATION_ID = "00000000-0000-0000-0000-000000000001";

    private static final String DEFAULT_FIRST_NAME = "Test";
    private static final String DEFAULT_LAST_NAME = "User";

    private final RegisterUserService registerUserService;
    private final SuspendUserService suspendUserService;
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final InviteMemberUseCase inviteMemberUseCase;

    public TestUserFactory(
            RegisterUserService registerUserService,
            SuspendUserService suspendUserService,
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            InviteMemberUseCase inviteMemberUseCase
    ) {
        this.registerUserService = registerUserService;
        this.suspendUserService = suspendUserService;
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.inviteMemberUseCase = inviteMemberUseCase;
    }

    /**
     * Crée un utilisateur en état PENDING (état initial apres register).
     *
     * <p>Aucun {@code Member} n'est créé : utiliser cette méthode quand le test n'a pas besoin
     * d'autorité Spring Security (ex. tests centrés sur le magic-link avant activation).</p>
     */
    public UserId registerPending(String email, String rawPassword) {
        return registerUserService.register(new RegisterUserCommand(
                email,
                rawPassword,
                DEFAULT_FIRST_NAME,
                DEFAULT_LAST_NAME
        ));
    }

    /**
     * Crée un utilisateur en état ACTIVE rattaché à un {@code Member} role {@code MEMBER}.
     *
     * <p>Le mot de passe persisté final est le re-hash de {@code rawPassword}, de sorte que l'utilisateur peut
     * s'authentifier avec celui-ci (bcrypt produit un hash différent à chaque appel mais {@code matches()} reste
     * vrai).</p>
     *
     * <p>Le {@code Member} est créé en état PENDING (pas de cascade {@code UserActivated} ici car on bypass le redeem
     * du magic-link). Le statut du Member n'impacte pas la résolution des autorités, seul son rôle compte.</p>
     */
    public UserId registerActive(String email, String rawPassword) {
        UserId id = registerPending(email, rawPassword);
        activateInPlace(id, rawPassword);
        inviteMember(id, MemberRole.MEMBER);
        return id;
    }

    /**
     * Crée un utilisateur en état SUSPENDED (PENDING -> activateWithPassword -> suspend).
     *
     * <p>Crée également un {@code Member} role {@code MEMBER} (cf. {@link #registerActive(String, String)}).</p>
     */
    public UserId registerSuspended(String email, String rawPassword) {
        UserId id = registerActive(email, rawPassword);
        suspendUserService.suspend(new SuspendUserCommand(id.value().toString()));
        return id;
    }

    /**
     * Crée un utilisateur ACTIVE rattaché à un {@code Member} role {@code ADMIN}.
     *
     * <p>Remplace l'ancien raccourci qui mutait {@code User.role} en base : depuis D14, les autorités Spring Security
     * sont dérivées de {@code Member.role} via {@code MemberUserAuthoritiesAdapter}. Plus de hack JPA nécessaire.</p>
     */
    public UserId registerActiveAdmin(String email, String rawPassword) {
        UserId id = registerPending(email, rawPassword);
        activateInPlace(id, rawPassword);
        inviteMember(id, MemberRole.ADMIN);
        return id;
    }

    private void activateInPlace(UserId id, String rawPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("User just registered should exist in DB: " + id));
        user.activateWithPassword(passwordHasher.hash(RawPassword.of(rawPassword)));
        userRepository.save(user);
    }

    private void inviteMember(UserId userId, MemberRole role) {
        inviteMemberUseCase.invite(new InviteMemberCommand(
                DEFAULT_ORGANISATION_ID,
                userId.value().toString(),
                role.name()
        ));
    }
}
