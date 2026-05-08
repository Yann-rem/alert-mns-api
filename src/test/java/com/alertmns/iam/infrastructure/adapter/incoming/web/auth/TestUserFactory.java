package com.alertmns.iam.infrastructure.adapter.incoming.web.auth;

import com.alertmns.iam.application.ActivateUserService;
import com.alertmns.iam.application.RegisterUserService;
import com.alertmns.iam.application.SuspendUserService;
import com.alertmns.iam.domain.port.incoming.command.ActivateUserCommand;
import com.alertmns.iam.domain.port.incoming.command.RegisterUserCommand;
import com.alertmns.iam.domain.port.incoming.command.SuspendUserCommand;
import com.alertmns.shared.UserId;

/**
 * Factory de test créant des utilisateurs dans différents états via les use cases du domaine.
 *
 * <p>Reste DDD-pur : on n'écrit jamais directement en base, on passe par les services applicatifs et les méthodes de
 * l'agrégat. Si demain les transitions d'etat changent, les tests reflètent le vrai comportement metier.</p>
 */
public final class TestUserFactory {

    private static final String DEFAULT_ORGANISATION_ID = "00000000-0000-0000-0000-000000000001";
    private static final String DEFAULT_FIRST_NAME = "Test";
    private static final String DEFAULT_LAST_NAME = "User";

    private final RegisterUserService registerUserService;
    private final ActivateUserService activateUserService;
    private final SuspendUserService suspendUserService;

    public TestUserFactory(
            RegisterUserService registerUserService,
            ActivateUserService activateUserService,
            SuspendUserService suspendUserService
    ) {
        this.registerUserService = registerUserService;
        this.activateUserService = activateUserService;
        this.suspendUserService = suspendUserService;
    }

    /**
     * Cree un utilisateur en état PENDING (état initial apres register).
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
     * Cree un utilisateur en état ACTIVE (PENDING -> activate).
     */
    public UserId registerActive(String email, String rawPassword) {
        UserId id = registerPending(email, rawPassword);
        activateUserService.activate(new ActivateUserCommand(id.value().toString()));
        return id;
    }

    /**
     * Cree un utilisateur en état SUSPENDED (PENDING -> activate -> suspend).
     */
    public UserId registerSuspended(String email, String rawPassword) {
        UserId id = registerActive(email, rawPassword);
        suspendUserService.suspend(new SuspendUserCommand(id.value().toString()));
        return id;
    }
}
