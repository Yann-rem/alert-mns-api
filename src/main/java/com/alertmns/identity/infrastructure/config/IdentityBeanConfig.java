package com.alertmns.identity.infrastructure.config;

import com.alertmns.identity.application.AnonymizeUserService;
import com.alertmns.identity.application.IssueActivationTokenService;
import com.alertmns.identity.application.ReactivateUserService;
import com.alertmns.identity.application.RedeemActivationTokenService;
import com.alertmns.identity.application.RegisterPendingUserService;
import com.alertmns.identity.application.SuspendUserService;
import com.alertmns.identity.application.UpdateAbsenceMessageService;
import com.alertmns.identity.application.UpdateProfileService;
import com.alertmns.identity.application.ValidateActivationTokenService;
import com.alertmns.identity.domain.port.incoming.IssueActivationTokenUseCase;
import com.alertmns.identity.domain.port.outgoing.ActivationTokenRepository;
import com.alertmns.identity.domain.port.outgoing.MailerPort;
import com.alertmns.identity.domain.port.outgoing.PasswordHasher;
import com.alertmns.identity.domain.port.outgoing.UserAuthoritiesProvider;
import com.alertmns.identity.domain.port.outgoing.UserRepository;
import com.alertmns.identity.infrastructure.adapter.incoming.event.IssueActivationTokenOnUserRegisteredListener;
import com.alertmns.identity.infrastructure.adapter.incoming.event.WebSocketRevocationListener;
import com.alertmns.identity.infrastructure.adapter.incoming.web.security.DomainUserDetailsService;
import com.alertmns.identity.infrastructure.adapter.incoming.web.security.SpringSecurityCurrentUserAdapter;
import com.alertmns.identity.infrastructure.adapter.outgoing.mailer.LoggingMailerAdapter;
import com.alertmns.identity.infrastructure.adapter.outgoing.persistence.ActivationTokenJpaRepository;
import com.alertmns.identity.infrastructure.adapter.outgoing.persistence.ActivationTokenPersistenceAdapter;
import com.alertmns.identity.infrastructure.adapter.outgoing.persistence.UserJpaRepository;
import com.alertmns.identity.infrastructure.adapter.outgoing.persistence.UserPersistenceAdapter;
import com.alertmns.identity.infrastructure.adapter.outgoing.security.SpringSecurityPasswordHasher;
import com.alertmns.organisation.domain.port.outgoing.MemberRepository;
import com.alertmns.organisation.infrastructure.adapter.outgoing.authorities.MemberUserAuthoritiesAdapter;
import com.alertmns.shared.CurrentUserPort;
import com.alertmns.shared.EventPublisher;
import com.alertmns.shared.web.ws.WebSocketSessionRegistry;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.security.web.session.HttpSessionEventPublisher;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;

@Configuration
public class IdentityBeanConfig {

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
    public ActivationTokenRepository activationTokenRepository(ActivationTokenJpaRepository jpaRepository) {
        return new ActivationTokenPersistenceAdapter(jpaRepository);
    }

    @Bean
    public MailerPort mailerPort() {
        return new LoggingMailerAdapter();
    }

    @Bean
    public EventPublisher eventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        return events -> events.forEach(applicationEventPublisher::publishEvent);
    }

    // --- Ports exposés vers d'autres BCs ---

    @Bean
    public UserAuthoritiesProvider userAuthoritiesProvider(MemberRepository memberRepository) {
        return new MemberUserAuthoritiesAdapter(memberRepository);
    }

    // --- Spring Security ---

    @Bean
    public DomainUserDetailsService domainUserDetailsService(
            UserRepository userRepository,
            UserAuthoritiesProvider userAuthoritiesProvider
    ) {
        return new DomainUserDetailsService(userRepository, userAuthoritiesProvider);
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
    public RegisterPendingUserService registerPendingUserService(
            UserRepository repository,
            EventPublisher publisher,
            Clock clock
    ) {
        return new RegisterPendingUserService(repository, publisher, clock);
    }

    @Bean
    public IssueActivationTokenService issueActivationTokenService(
            ActivationTokenRepository tokenRepository,
            UserRepository userRepository,
            MailerPort mailer,
            @Value("${alertmns.identity.activation.ttl}") Duration ttl,
            Clock clock,
            @Value("${alertmns.identity.activation.frontend-base-url}") URI frontendBaseUrl
    ) {
        return new IssueActivationTokenService(tokenRepository, userRepository, mailer, ttl, clock, frontendBaseUrl);
    }

    @Bean
    public ValidateActivationTokenService validateActivationTokenService(
            ActivationTokenRepository tokenRepository,
            UserRepository userRepository,
            Clock clock
    ) {
        return new ValidateActivationTokenService(tokenRepository, userRepository, clock);
    }

    @Bean
    public RedeemActivationTokenService redeemActivationTokenService(
            ActivationTokenRepository tokenRepository,
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            EventPublisher publisher,
            Clock clock
    ) {
        return new RedeemActivationTokenService(tokenRepository, userRepository, passwordHasher, publisher, clock);
    }

    @Bean
    public UpdateProfileService updateProfileService(
            UserRepository repository,
            EventPublisher publisher,
            Clock clock
    ) {
        return new UpdateProfileService(repository, publisher, clock);
    }

    @Bean
    public UpdateAbsenceMessageService updateAbsenceMessageService(
            UserRepository repository,
            EventPublisher publisher,
            Clock clock
    ) {
        return new UpdateAbsenceMessageService(repository, publisher, clock);
    }

    @Bean
    public SuspendUserService suspendUserService(UserRepository repository, EventPublisher publisher, Clock clock) {
        return new SuspendUserService(repository, publisher, clock);
    }

    @Bean
    public ReactivateUserService reactivateUserService(
            UserRepository repository,
            EventPublisher publisher,
            Clock clock
    ) {
        return new ReactivateUserService(repository, publisher, clock);
    }

    @Bean
    public AnonymizeUserService anonymizeUserService(UserRepository repository, EventPublisher publisher, Clock clock) {
        return new AnonymizeUserService(repository, publisher, clock);
    }

    // --- Event listeners ---

    @Bean
    public IssueActivationTokenOnUserRegisteredListener issueActivationTokenOnUserRegisteredListener(
            IssueActivationTokenUseCase issueActivationTokenUseCase
    ) {
        return new IssueActivationTokenOnUserRegisteredListener(issueActivationTokenUseCase);
    }

    /**
     * Publie les événements de cycle de vie des sessions HTTP (création / destruction) vers le contexte Spring — requis
     * pour que {@link WebSocketRevocationListener} détecte les logouts et expirations.
     */
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public WebSocketRevocationListener webSocketRevocationListener(WebSocketSessionRegistry sessionRegistry) {
        return new WebSocketRevocationListener(sessionRegistry);
    }
}
