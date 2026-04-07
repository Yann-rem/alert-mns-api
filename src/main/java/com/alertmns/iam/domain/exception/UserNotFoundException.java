package com.alertmns.iam.domain.exception;

import com.alertmns.iam.domain.model.UserId;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(UserId id) {
        super("User not found: " + id.value());
    }
}
