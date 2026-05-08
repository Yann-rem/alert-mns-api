package com.alertmns.iam.infrastructure.adapter.incoming.web.auth;

import com.alertmns.iam.application.ActivateUserService;
import com.alertmns.iam.application.RegisterUserService;
import com.alertmns.iam.application.SuspendUserService;
import com.alertmns.iam.domain.model.UserRole;
import com.alertmns.iam.domain.port.incoming.command.ActivateUserCommand;
import com.alertmns.iam.domain.port.incoming.command.RegisterUserCommand;
import com.alertmns.iam.domain.port.incoming.command.SuspendUserCommand;
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
    private final ActivateUserService activateUserService;
    private final SuspendUserService suspendUserService;
    private final UserJpaRepository userJpaRepository;

    public TestUserFactory(
            RegisterUserService registerUserService,
            ActivateUserService activateUserService,
            SuspendUserService suspendUserService,
            UserJpaRepository userJpaRepository
    ) {
        this.registerUserService = registerUserService;
        this.activateUserService = activateUserService;
        this.suspendUserService = suspendUserService;
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
     * Crée un utilisateur en état ACTIVE (PENDING -> activate).
     */
    public UserId registerActive(String email, String rawPassword) {
        UserId id = registerPending(email, rawPassword);
        activateUserService.activate(new ActivateUserCommand(id.value().toString()));
        return id;
    }

    /**
     * Crée un utilisateur en état SUSPENDED (PENDING -> activate -> suspend).
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
