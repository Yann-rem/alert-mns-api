package com.alertmns.iam.infrastructure.adapter.incoming.web.auth;

import com.alertmns.iam.application.RegisterUserService;
import com.alertmns.iam.application.SuspendUserService;
import com.alertmns.iam.domain.model.RawPassword;
import com.alertmns.iam.domain.model.User;
import com.alertmns.iam.domain.model.UserRole;
import com.alertmns.iam.domain.port.incoming.command.RegisterUserCommand;
import com.alertmns.iam.domain.port.incoming.command.SuspendUserCommand;
import com.alertmns.iam.domain.port.outgoing.PasswordHasher;
import com.alertmns.iam.domain.port.outgoing.UserRepository;
import com.alertmns.iam.infrastructure.adapter.outgoing.persistence.UserJpaEntity;
import com.alertmns.iam.infrastructure.adapter.outgoing.persistence.UserJpaRepository;
import com.alertmns.shared.UserId;

/**
 * Factory de test créant des utilisateurs dans différents états via les use cases du domaine.
 *
 * <p>Reste DDD-pur autant que possible : on passe par les services applicatifs et les méthodes de l'agrégat. Si demain
 * les transitions d'état changent, les tests reflètent le vrai comportement métier.</p>
 *
 * <p><b>Exception assumée — promotion ADMIN.</b> Il n'existe pas de use case {@code PromoteToAdmin} côté IAM, les
 * rôles vivront à terme dans le BC Organisation. Pour pouvoir tester {@code @PreAuthorize("hasRole('ADMIN')")} dès
 * aujourd'hui sans introduire un use case temporaire, {@link #registerActiveAdmin(String, String)} mute
 * directement le rôle en base via {@link UserJpaRepository}. Ce raccourci sera supprimé quand le BC Organisation
 * exposera la promotion comme un use case métier.</p>
 */
public final class TestUserFactory {

    private static final String DEFAULT_ORGANISATION_ID = "00000000-0000-0000-0000-000000000001";
    private static final String DEFAULT_FIRST_NAME = "Test";
    private static final String DEFAULT_LAST_NAME = "User";

    private final RegisterUserService registerUserService;
    private final SuspendUserService suspendUserService;
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final UserJpaRepository userJpaRepository;

    public TestUserFactory(
            RegisterUserService registerUserService,
            SuspendUserService suspendUserService,
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            UserJpaRepository userJpaRepository
    ) {
        this.registerUserService = registerUserService;
        this.suspendUserService = suspendUserService;
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.userJpaRepository = userJpaRepository;
    }

    /**
     * Crée un utilisateur en état PENDING (état initial apres register).
     */
    public UserId registerPending(String email, String rawPassword) {
        return registerUserService.register(new RegisterUserCommand(
                email,
                rawPassword,
                DEFAULT_FIRST_NAME,
                DEFAULT_LAST_NAME,
                DEFAULT_ORGANISATION_ID
        ));
    }

    /**
     * Crée un utilisateur en état ACTIVE en simulant le redeem du magic-link (PENDING -> activateWithPassword).
     *
     * <p>Le mot de passe persisté final est le re-hash de {@code rawPassword}, de sorte que l'utilisateur peut
     * s'authentifier avec celui-ci (bcrypt produit un hash différent à chaque appel mais {@code matches()} reste
     * vrai).</p>
     */
    public UserId registerActive(String email, String rawPassword) {
        UserId id = registerPending(email, rawPassword);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("User just registered should exist in DB: " + id));
        user.activateWithPassword(passwordHasher.hash(RawPassword.of(rawPassword)));
        userRepository.save(user);
        return id;
    }

    /**
     * Crée un utilisateur en état SUSPENDED (PENDING -> activateWithPassword -> suspend).
     */
    public UserId registerSuspended(String email, String rawPassword) {
        UserId id = registerActive(email, rawPassword);
        suspendUserService.suspend(new SuspendUserCommand(id.value().toString()));
        return id;
    }

    /**
     * Crée un utilisateur ACTIVE puis le promeut au role ADMIN en mutant directement la ligne en base.
     *
     * <p>Voir le javadoc de la classe pour la justification du raccourci : pas de use case domain dédié tant que la
     * gestion des roles n'est pas portée par le BC Organisation.</p>
     */
    public UserId registerActiveAdmin(String email, String rawPassword) {
        UserId id = registerActive(email, rawPassword);
        UserJpaEntity entity = userJpaRepository.findById(id.value())
                .orElseThrow(() -> new IllegalStateException("User just registered should exist in DB: " + id));
        UserJpaEntity promoted = new UserJpaEntity(
                entity.getId(),
                entity.getOrganisationId(),
                entity.getEmail(),
                entity.getHashedPassword(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getAvatar(),
                entity.getAbsenceContent(),
                entity.getAbsenceActive(),
                UserRole.ADMIN,
                entity.getStatus(),
                entity.isAnonymized(),
                entity.getCreatedAt()
        );
        userJpaRepository.save(promoted);
        return id;
    }
}
