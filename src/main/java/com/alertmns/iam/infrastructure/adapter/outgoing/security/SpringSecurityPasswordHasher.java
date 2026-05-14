package com.alertmns.iam.infrastructure.adapter.outgoing.security;

import com.alertmns.iam.domain.model.HashedPassword;
import com.alertmns.iam.domain.model.RawPassword;
import com.alertmns.iam.domain.port.outgoing.PasswordHasher;
import org.springframework.security.crypto.password.PasswordEncoder;

public final class SpringSecurityPasswordHasher implements PasswordHasher {

    private final PasswordEncoder passwordEncoder;

    public SpringSecurityPasswordHasher(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public HashedPassword hash(RawPassword rawPassword) {
        return HashedPassword.of(passwordEncoder.encode(rawPassword.value()));
    }
}
