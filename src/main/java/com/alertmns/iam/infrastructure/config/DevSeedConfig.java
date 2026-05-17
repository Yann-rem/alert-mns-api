package com.alertmns.iam.infrastructure.config;

import com.alertmns.iam.domain.model.Email;
import com.alertmns.iam.domain.model.FirstName;
import com.alertmns.iam.domain.model.HashedPassword;
import com.alertmns.iam.domain.model.LastName;
import com.alertmns.iam.domain.model.Profile;
import com.alertmns.iam.domain.model.User;
import com.alertmns.iam.domain.port.outgoing.UserRepository;
import com.alertmns.shared.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Seed temporaire pour permettre de tester /auth/login pendant le développement, en attendant la mise en place de
 * l'endpoint d'inscription. Actif uniquement avec le profil {@code dev}.
 *
 * <p><strong>À supprimer</strong> dès que l'endpoint POST /auth/register sera disponible.</p>
 */
@Configuration
@org.springframework.context.annotation.Profile("dev")
public class DevSeedConfig {

    private static final Logger log = LoggerFactory.getLogger(DevSeedConfig.class);

    private static final String DEV_EMAIL = "dev@alertmns.local";
    private static final String DEV_PASSWORD = "dev123456789";

    @Bean
    public CommandLineRunner seedDevUser(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EventPublisher eventPublisher
    ) {
        return args -> {
            Email email = Email.of(DEV_EMAIL);
            if (userRepository.existsByEmail(email)) {
                log.info("Dev user already exists: {}", DEV_EMAIL);
                return;
            }

            User user = User.register(
                    email,
                    HashedPassword.of(passwordEncoder.encode(DEV_PASSWORD)),
                    Profile.of(FirstName.of("Dev"), LastName.of("User"))
            );
            user.activateWithPassword(HashedPassword.of(passwordEncoder.encode(DEV_PASSWORD)));

            userRepository.save(user);
            eventPublisher.publish(user.pullDomainEvents());

            log.info("Dev user seeded: {} / {}", DEV_EMAIL, DEV_PASSWORD);
        };
    }
}
