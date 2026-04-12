package com.alertmns.organisation.domain.exception;

import com.alertmns.organisation.domain.model.MemberId;

/**
 * Exception de domaine représentant l'absence d'un membre recherché par son identifiant.
 */
public class MemberNotFoundException extends RuntimeException {
    public MemberNotFoundException(MemberId id) {
        super("Member not found: " + id.value());
    }
}
