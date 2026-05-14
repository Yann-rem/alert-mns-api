package com.alertmns.iam.infrastructure.config;

import com.alertmns.iam.application.ReactivateUserService;
import com.alertmns.iam.application.RegisterUserService;
import com.alertmns.iam.application.SuspendUserService;
import com.alertmns.iam.application.UpdateAbsenceMessageService;
import com.alertmns.iam.application.UpdateProfileService;
import com.alertmns.iam.domain.port.outgoing.PasswordHasher;
import com.alertmns.iam.domain.port.outgoing.UserRepository;
import com.alertmns.iam.infrastructure.adapter.incoming.web.security.DomainUserDetailsService;
import com.alertmns.iam.infrastructure.adapter.incoming.web.security.SpringSecurityCurrentUserAdapter;
import com.alertmns.iam.infrastructure.adapter.outgoing.persistence.UserJpaRepository;
import com.alertmns.iam.infrastructure.adapter.outgoing.persistence.UserPersistenceAdapter;
import com.alertmns.iam.infrastructure.adapter.outgoing.security.SpringSecurityPasswordHasher;
import com.alertmns.shared.CurrentUserPort;
import com.alertmns.shared.EventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
public class IamBeanConfig {

    // --- Ports sortants ---

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public PasswordHasher passwordHasher(PasswordEncoder passwordEncoder) {
        return new SpringSecurityPasswordHasher(passwordEncoder);
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

    @Bean
    public AuthenticationManager authenticationManager(
            DomainUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    // --- Services ---

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
            PasswordHasher passwordHasher,
            EventPublisher publisher
    ) {
        return new RegisterUserService(repository, passwordHasher, publisher);
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
