package com.alertmns.organisation.infrastructure.adapter.incoming.web.exception;

import com.alertmns.organisation.domain.exception.GroupMembershipNotFoundException;
import com.alertmns.organisation.domain.exception.GroupNameAlreadyExistsException;
import com.alertmns.organisation.domain.exception.GroupNotFoundException;
import com.alertmns.organisation.domain.exception.InvitationAlreadyPendingException;
import com.alertmns.organisation.domain.exception.InvitationExpiredException;
import com.alertmns.organisation.domain.exception.InvitedUserAlreadyExistsException;
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

    /** Une invitation est déjà en attente pour cet e-mail : la réémettre créerait un doublon. */
    @ExceptionHandler(InvitationAlreadyPendingException.class)
    public ProblemDetail handleInvitationAlreadyPending(InvitationAlreadyPendingException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    /** Un compte existe déjà pour l'e-mail invité (déjà membre, ou déjà invité). */
    @ExceptionHandler(InvitedUserAlreadyExistsException.class)
    public ProblemDetail handleInvitedUserAlreadyExists(InvitedUserAlreadyExistsException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    // --- 410 GONE ---

    /**
     * Invitation d'adhésion expirée. Aligné sur le 410 déjà renvoyé pour un lien magique périmé :
     * la ressource a existé mais n'est plus exploitable, et un nouvel envoi est nécessaire.
     */
    @ExceptionHandler(InvitationExpiredException.class)
    public ProblemDetail handleInvitationExpired(InvitationExpiredException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.GONE, ex.getMessage());
    }

    // --- 403 FORBIDDEN ---

    @ExceptionHandler(OrganisationMismatchException.class)
    public ProblemDetail handleOrganisationMismatch(OrganisationMismatchException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    }
}
