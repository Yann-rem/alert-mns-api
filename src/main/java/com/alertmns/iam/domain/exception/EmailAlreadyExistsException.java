package com.alertmns.iam.domain.exception;

import com.alertmns.iam.domain.model.Email;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(Email email) {
        super("Email déjà existant :" + email.value());
    }
}
