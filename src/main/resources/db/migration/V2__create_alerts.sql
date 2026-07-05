-- ============================================================================
-- V2 — Table des alertes (BC Alerting)
-- ----------------------------------------------------------------------------
-- Une alerte est une annonce one-to-many diffusée par un membre autorisé
-- (ADMIN ou MANAGER) vers une audience : toute l'organisation (audience_kind =
-- ORGANISATION, group_id nul) ou un groupe précis (audience_kind = GROUP,
-- group_id renseigné).
--
-- Pas de foreign key (cohérent avec la baseline V1) : les agrégats et les BC
-- se référencent par identifiant. Les index servent la lecture « mes alertes »
-- (union école ∪ groupes) à venir.
-- ============================================================================

CREATE TABLE alerts (
    id               uuid                        NOT NULL,
    organisation_id  uuid                        NOT NULL,
    issuer_id        uuid                        NOT NULL,
    content          varchar(4000)               NOT NULL,
    audience_kind    varchar(20)                 NOT NULL,
    group_id         uuid,
    level            varchar(20)                 NOT NULL,
    issued_at        timestamp(6) with time zone NOT NULL,
    CONSTRAINT pk_alerts PRIMARY KEY (id)
);

CREATE INDEX idx_alert_organisation ON alerts (organisation_id);
CREATE INDEX idx_alert_group ON alerts (group_id);
