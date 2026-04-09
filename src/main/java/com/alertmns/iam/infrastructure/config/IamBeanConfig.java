package com.alertmns.iam.infrastructure.config;

import com.alertmns.iam.application.ActivateUserService;
import com.alertmns.iam.application.DisableUserService;
import com.alertmns.iam.application.ReactivateUserService;
import com.alertmns.iam.application.RegisterUserService;
import com.alertmns.iam.application.UpdateAbsenceMessageService;
import com.alertmns.iam.application.UpdateProfileService;
import com.alertmns.iam.domain.port.outgoing.AuthenticationPort;
import com.alertmns.iam.domain.port.outgoing.EventPublisher;
import com.alertmns.iam.domain.port.outgoing.UserRepository;
import com.alertmns.iam.infrastructure.adapter.outgoing.persistence.UserJpaRepository;
import com.alertmns.iam.infrastructure.adapter.outgoing.persistence.UserPersistenceAdapter;
import com.alertmns.iam.infrastructure.adapter.outgoing.security.SpringSecurityAdapter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class IamBeanConfig {

    // --- Ports sortants ---

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationPort authenticationPort(PasswordEncoder passwordEncoder) {
        return new SpringSecurityAdapter(passwordEncoder);
    }

    @Bean
    public UserRepository userRepository(UserJpaRepository userJpaRepository) {
        return new UserPersistenceAdapter(userJpaRepository);
    }

    @Bean
    public EventPublisher eventPublisher(ApplicationEventPublisher publisher) {
        return events -> events.forEach(publisher::publishEvent);
    }

    // --- Services ---

    @Bean
    public RegisterUserService registerUserService(
            UserRepository userRepository,
            AuthenticationPort authenticationPort,
            EventPublisher eventPublisher
    ) {
        return new RegisterUserService(userRepository, authenticationPort, eventPublisher);
    }

    @Bean
    public ActivateUserService activateUserService(
            UserRepository userRepository,
            EventPublisher eventPublisher
    ) {
        return new ActivateUserService(userRepository, eventPublisher);
    }

    @Bean
    public DisableUserService disableUserService(
            UserRepository userRepository,
            EventPublisher eventPublisher
    ) {
        return new DisableUserService(userRepository, eventPublisher);
    }

    @Bean
    public ReactivateUserService reactivateUserService(
            UserRepository userRepository,
            EventPublisher eventPublisher
    ) {
        return new ReactivateUserService(userRepository, eventPublisher);
    }

    @Bean
    public UpdateProfileService updateProfileService(
            UserRepository userRepository,
            EventPublisher eventPublisher
    ) {
        return new UpdateProfileService(userRepository, eventPublisher);
    }

    @Bean
    public UpdateAbsenceMessageService updateAbsenceMessageService(
            UserRepository userRepository,
            EventPublisher eventPublisher
    ) {
        return new UpdateAbsenceMessageService(userRepository, eventPublisher);
    }
}
