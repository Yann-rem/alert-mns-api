package com.alertmns.organisation.infrastructure.adapter.incoming.web.exception;

import com.alertmns.organisation.domain.exception.GroupMembershipNotFoundException;
import com.alertmns.organisation.domain.exception.GroupNameAlreadyExistsException;
import com.alertmns.organisation.domain.exception.GroupNotFoundException;
import com.alertmns.organisation.domain.exception.LastAdminCannotBeRemovedException;
import com.alertmns.organisation.domain.exception.MemberNotFoundException;
import com.alertmns.organisation.domain.exception.OrganisationMismatchException;
import com.alertmns.organisation.domain.exception.OrganisationNameAlreadyExistsException;
import com.alertmns.organisation.domain.exception.OrganisationNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class OrganisationExceptionHandler {

    // --- 404 NOT_FOUND ---

    @ExceptionHandler(OrganisationNotFoundException.class)
    public ProblemDetail handleOrganisationNotFound(OrganisationNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(GroupNotFoundException.class)
    public ProblemDetail handleGroupNotFound(GroupNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(MemberNotFoundException.class)
    public ProblemDetail handleMemberNotFound(MemberNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(GroupMembershipNotFoundException.class)
    public ProblemDetail handleGroupMembershipNotFound(GroupMembershipNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // --- 409 CONFLICT ---

    @ExceptionHandler(OrganisationNameAlreadyExistsException.class)
    public ProblemDetail handleOrganisationNameAlreadyExists(OrganisationNameAlreadyExistsException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(GroupNameAlreadyExistsException.class)
    public ProblemDetail handleGroupNameAlreadyExists(GroupNameAlreadyExistsException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(LastAdminCannotBeRemovedException.class)
    public ProblemDetail handleLastAdminCannotBeRemoved(LastAdminCannotBeRemovedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    // --- 403 FORBIDDEN ---

    @ExceptionHandler(OrganisationMismatchException.class)
    public ProblemDetail handleOrganisationMismatch(OrganisationMismatchException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    }
}
