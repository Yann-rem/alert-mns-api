package com.alertmns.iam.infrastructure.adapter.outgoing.security;

import com.alertmns.iam.domain.port.outgoing.PasswordHasher;
import org.springframework.security.crypto.password.PasswordEncoder;

public final class SpringSecurityPasswordHasher implements PasswordHasher {

    private final PasswordEncoder passwordEncoder;

    public SpringSecurityPasswordHasher(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
