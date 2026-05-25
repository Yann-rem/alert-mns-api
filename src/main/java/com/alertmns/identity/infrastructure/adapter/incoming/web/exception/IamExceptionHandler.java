package com.alertmns.identity.infrastructure.adapter.incoming.web.exception;

import com.alertmns.identity.domain.exception.ActivationTokenExpiredException;
import com.alertmns.identity.domain.exception.ActivationTokenNotFoundException;
import com.alertmns.identity.domain.exception.BannedUserCannotBeReactivatedException;
import com.alertmns.identity.domain.exception.EmailAlreadyExistsException;
import com.alertmns.identity.domain.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class IamExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ProblemDetail handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFound(UserNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ActivationTokenNotFoundException.class)
    public ProblemDetail handleActivationTokenNotFound(ActivationTokenNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.GONE, "Activation link is no longer valid");
    }

    @ExceptionHandler(ActivationTokenExpiredException.class)
    public ProblemDetail handleActivationTokenExpired(ActivationTokenExpiredException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.GONE, "Activation link is no longer valid");
    }

    @ExceptionHandler(BannedUserCannotBeReactivatedException.class)
    public ProblemDetail handleBannedUserCannotBeReactivated(BannedUserCannotBeReactivatedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }

    @ExceptionHandler(DisabledException.class)
    public ProblemDetail handleDisabled(DisabledException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Account is disabled");
    }

    @ExceptionHandler(LockedException.class)
    public ProblemDetail handleLocked(LockedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Account is locked");
    }
}
