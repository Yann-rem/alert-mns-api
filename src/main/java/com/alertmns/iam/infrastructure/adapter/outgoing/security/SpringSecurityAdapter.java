package com.alertmns.iam.infrastructure.adapter.outgoing.security;

import com.alertmns.iam.domain.port.outgoing.AuthenticationPort;
import org.springframework.security.crypto.password.PasswordEncoder;

public class SpringSecurityAdapter implements AuthenticationPort {

    private final PasswordEncoder passwordEncoder;

    public SpringSecurityAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
