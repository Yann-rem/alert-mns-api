package com.alertmns.iam.domain.port.outgoing;

import com.alertmns.iam.domain.model.Email;
import com.alertmns.iam.domain.model.User;
import com.alertmns.iam.domain.model.UserId;

import java.util.Optional;

/**
 * Port sortant pour la persistance des utilisateurs.
 */
public interface UserRepository {

    void save(User user);

    Optional<User> findById(UserId id);

    Optional<User> findByEmail(Email email);

    boolean existsByEmail(Email email);
}
