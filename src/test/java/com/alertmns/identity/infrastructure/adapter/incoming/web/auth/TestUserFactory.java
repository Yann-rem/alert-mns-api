package com.alertmns.identity.infrastructure.adapter.incoming.web.auth;

import com.alertmns.identity.application.RegisterPendingUserService;
import com.alertmns.identity.application.SuspendUserService;
import com.alertmns.identity.domain.model.RawPassword;
import com.alertmns.identity.domain.model.User;
import com.alertmns.identity.domain.port.incoming.command.RegisterPendingUserCommand;
import com.alertmns.identity.domain.port.incoming.command.SuspendUserCommand;
import com.alertmns.identity.domain.port.outgoing.PasswordHasher;
import com.alertmns.identity.domain.port.outgoing.UserRepository;
import com.alertmns.organisation.domain.model.MemberRole;
import com.alertmns.organisation.domain.port.incoming.InviteMemberUseCase;
import com.alertmns.organisation.domain.port.incoming.IssueMembershipInvitationUseCase;
import com.alertmns.organisation.domain.port.incoming.command.InviteMemberCommand;
import com.alertmns.organisation.domain.port.incoming.command.IssueMembershipInvitationCommand;
import com.alertmns.shared.Email;
import com.alertmns.shared.UserId;

/**
 * Factory de test créant des utilisateurs dans différents états via les use cases du domaine.
 *
 * <p>Reste DDD-pur autant que possible : on passe par les services applicatifs et les méthodes de l'agrégat. Si demain
 * les transitions d'état changent, les tests reflètent le vrai comportement métier.</p>
 *
 * <p><b>Création des Users via deux portes d'entrée distinctes</b> :</p>
 * <ul>
 *     <li>{@link #registerPending(String)} — création directe via {@link RegisterPendingUserService},
 *         sans invitation. Utiliser pour les tests qui ne déclenchent pas la cascade
 *         {@code UserActivated → AcceptMembershipInvitation} (tests de /validate, login PENDING, etc.).</li>
 *     <li>{@link #issueMembershipInvitation(String)} — orchestration complète via
 *         {@link IssueMembershipInvitationUseCase}. Crée User PENDING + invitation PENDING. Utiliser
 *         pour les tests qui passent par le redeem du magic-link (la cascade trouvera l'invitation).</li>
 * </ul>
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
     * {@code Organisation} n'est créé en base — {@code InviteMemberUseCase} et
     * {@code IssueMembershipInvitationUseCase} n'enforcent pas l'existence préalable de l'organisation.
     */
    public static final String DEFAULT_ORGANISATION_ID = "00000000-0000-0000-0000-000000000001";

    private static final String DEFAULT_FIRST_NAME = "Test";
    private static final String DEFAULT_LAST_NAME = "User";

    private final RegisterPendingUserService registerPendingUserService;
    private final SuspendUserService suspendUserService;
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final InviteMemberUseCase inviteMemberUseCase;
    private final IssueMembershipInvitationUseCase issueMembershipInvitationUseCase;

    public TestUserFactory(
            RegisterPendingUserService registerPendingUserService,
            SuspendUserService suspendUserService,
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            InviteMemberUseCase inviteMemberUseCase,
            IssueMembershipInvitationUseCase issueMembershipInvitationUseCase
    ) {
        this.registerPendingUserService = registerPendingUserService;
        this.suspendUserService = suspendUserService;
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.inviteMemberUseCase = inviteMemberUseCase;
        this.issueMembershipInvitationUseCase = issueMembershipInvitationUseCase;
    }

    /**
     * Crée un utilisateur en état PENDING (sans invitation, sans Member), avec un {@code HashedPassword.unset()}.
     *
     * <p>À utiliser quand le test n'a pas besoin de la cascade d'acceptation (ex. tests de /validate, login PENDING).
     * Pour un flow complet avec invitation, utiliser {@link #issueMembershipInvitation(String)}.</p>
     */
    public UserId registerPending(String email) {
        return registerPendingUserService.register(new RegisterPendingUserCommand(
                email,
                DEFAULT_FIRST_NAME,
                DEFAULT_LAST_NAME
        ));
    }

    /**
     * Émet une invitation pour un email donné et retourne le userId du User PENDING créé en orchestration.
     *
     * <p>Passe par {@link IssueMembershipInvitationUseCase} — c'est le chemin nominal D17 : User PENDING +
     * invitation PENDING en BD. Le magic-link est émis via la cascade {@code UserRegistered → IssueActivationToken}.
     * Lors du redeem, la cascade {@code UserActivated → AcceptMembershipInvitation} trouvera l'invitation et créera
     * le {@code Member} ACTIVE.</p>
     */
    public UserId issueMembershipInvitation(String email) {
        issueMembershipInvitationUseCase.issue(new IssueMembershipInvitationCommand(
                DEFAULT_ORGANISATION_ID,
                email,
                DEFAULT_FIRST_NAME,
                DEFAULT_LAST_NAME,
                MemberRole.MEMBER.name()
        ));
        return userRepository.findByEmail(Email.of(email))
                .orElseThrow(() -> new IllegalStateException(
                        "User must exist after IssueMembershipInvitation: " + email))
                .id();
    }

    /**
     * Crée un utilisateur en état ACTIVE rattaché à un {@code Member} role {@code MEMBER}.
     *
     * <p>L'utilisateur est créé PENDING (mot de passe sentinelle), puis activé via
     * {@code activateWithPassword(rawPassword)} : le hash final permet à l'utilisateur de s'authentifier avec
     * {@code rawPassword}.</p>
     *
     * <p>Le {@code Member} est créé en état PENDING (pas de cascade {@code UserActivated} ici car on bypass le redeem
     * du magic-link). Le statut du Member n'impacte pas la résolution des autorités, seul son rôle compte.</p>
     */
    public UserId registerActive(String email, String rawPassword) {
        UserId id = registerPending(email);
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
     * <p>Depuis D14, les autorités Spring Security sont dérivées de {@code Member.role} via
     * {@code MemberUserAuthoritiesAdapter}.</p>
     */
    public UserId registerActiveAdmin(String email, String rawPassword) {
        UserId id = registerPending(email);
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
