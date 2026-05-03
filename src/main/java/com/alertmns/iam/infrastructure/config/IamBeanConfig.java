package com.alertmns.iam.infrastructure.config;

import com.alertmns.iam.application.ActivateUserService;
import com.alertmns.iam.application.ReactivateUserService;
import com.alertmns.iam.application.RegisterUserService;
import com.alertmns.iam.application.SuspendUserService;
import com.alertmns.iam.application.UpdateAbsenceMessageService;
import com.alertmns.iam.application.UpdateProfileService;
import com.alertmns.iam.domain.port.outgoing.AuthenticationPort;
import com.alertmns.iam.domain.port.outgoing.UserRepository;
import com.alertmns.iam.infrastructure.adapter.incoming.web.security.DomainUserDetailsService;
import com.alertmns.iam.infrastructure.adapter.incoming.web.security.SpringSecurityCurrentUserAdapter;
import com.alertmns.iam.infrastructure.adapter.outgoing.persistence.UserJpaRepository;
import com.alertmns.iam.infrastructure.adapter.outgoing.persistence.UserPersistenceAdapter;
import com.alertmns.iam.infrastructure.adapter.outgoing.security.SpringSecurityAdapter;
import com.alertmns.shared.CurrentUserPort;
import com.alertmns.shared.EventPublisher;
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
    public UserRepository userRepository(UserJpaRepository jpaRepository) {
        return new UserPersistenceAdapter(jpaRepository);
    }

    @Bean
    public EventPublisher eventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        return events -> events.forEach(applicationEventPublisher::publishEvent);
    }

    // --- Spring Security ---

    @Bean
    public DomainUserDetailsService domainUserDetailsService(UserRepository userRepository) {
        return new DomainUserDetailsService(userRepository);
    }

    @Bean
    public CurrentUserPort currentUserPort() {
        return new SpringSecurityCurrentUserAdapter();
    }

    // --- Services ---

    @Bean
    public ActivateUserService activateUserService(UserRepository repository, EventPublisher publisher) {
        return new ActivateUserService(repository, publisher);
    }

    @Bean
    public SuspendUserService suspendUserServiceUserService(UserRepository repository, EventPublisher publisher) {
        return new SuspendUserService(repository, publisher);
    }

    @Bean
    public ReactivateUserService reactivateUserService(UserRepository repository, EventPublisher publisher) {
        return new ReactivateUserService(repository, publisher);
    }

    @Bean
    public RegisterUserService registerUserService(
            UserRepository repository,
            AuthenticationPort authenticationPort,
            EventPublisher publisher
    ) {
        return new RegisterUserService(repository, authenticationPort, publisher);
    }

    @Bean
    public UpdateAbsenceMessageService updateAbsenceMessageService(UserRepository repository, EventPublisher publisher) {
        return new UpdateAbsenceMessageService(repository, publisher);
    }

    @Bean
    public UpdateProfileService updateProfileService(UserRepository repository, EventPublisher publisher) {
        return new UpdateProfileService(repository, publisher);
    }
}
