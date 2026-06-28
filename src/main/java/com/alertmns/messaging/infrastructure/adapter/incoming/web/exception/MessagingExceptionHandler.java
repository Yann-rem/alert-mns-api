package com.alertmns.messaging.infrastructure.adapter.incoming.web.exception;

import com.alertmns.messaging.domain.exception.ConversationNotFoundException;
import com.alertmns.messaging.domain.exception.InvalidReplyTargetException;
import com.alertmns.messaging.domain.exception.NotAConversationParticipantException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MessagingExceptionHandler {

    // --- 404 NOT_FOUND ---

    @ExceptionHandler(ConversationNotFoundException.class)
    public ProblemDetail handleConversationNotFound(ConversationNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // --- 403 FORBIDDEN ---

    @ExceptionHandler(NotAConversationParticipantException.class)
    public ProblemDetail handleNotAParticipant(NotAConversationParticipantException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    // --- 400 BAD_REQUEST ---

    @ExceptionHandler(InvalidReplyTargetException.class)
    public ProblemDetail handleInvalidReplyTarget(InvalidReplyTargetException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
}
