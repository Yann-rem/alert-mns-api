package com.alertmns.identity.infrastructure.config;

import com.alertmns.identity.infrastructure.adapter.incoming.web.security.DomainUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("userAccess")
@RequiredArgsConstructor
public class UserAccessChecker {
    public boolean canEditOwnProfile(UUID targetUserId, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof DomainUserDetails dud)) {
            return false;
        }
        return dud.userId().value().equals(targetUserId);
    }
}
