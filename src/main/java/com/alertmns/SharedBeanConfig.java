package com.alertmns;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Beans transverses aux BCs (cross-cutting concerns).
 */
@Configuration
public class SharedBeanConfig {

    /**
     * Horloge système utilisée par les services applicatifs pour générer des timestamps métier de manière injectable
     * et testable.
     *
     * @return l'horloge système (UTC)
     */
    @Bean
    public Clock systemClock() {
        return Clock.systemUTC();
    }
}
