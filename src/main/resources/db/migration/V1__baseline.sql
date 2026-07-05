-- ============================================================================
-- V1 — Baseline du schéma alert-mns
-- ----------------------------------------------------------------------------
-- Capture le schéma jusqu'ici généré par Hibernate (ddl-auto=create). À partir
-- de cette migration, le schéma est versionné par Flyway et Hibernate se
-- contente de le valider (ddl-auto=validate).
--
-- Types alignés sur le dialecte PostgreSQL d'Hibernate 6 :
--   UUID       -> uuid
--   String     -> varchar(n)  (n = longueur @Column, 255 par défaut)
--   enum STRING-> varchar(20)
--   Instant    -> timestamp(6) with time zone
--   boolean    -> boolean
--
-- Aucune foreign key : les agrégats (et les bounded contexts) se référencent
-- uniquement par identifiant. Ce découplage est volontaire — il évite tout
-- couplage physique entre Identity, Organisation et Messaging.
-- ============================================================================


-- ============================================================================
-- Bounded context : Identity
-- ============================================================================

CREATE TABLE users (
    id               uuid                        NOT NULL,
    email            varchar(254)                NOT NULL,
    hashed_password  varchar(255)                NOT NULL,
    first_name       varchar(100)                NOT NULL,
    last_name        varchar(100)                NOT NULL,
    avatar           varchar(500),
    absence_content  varchar(500),
    absence_active   boolean,
    status           varchar(20)                 NOT NULL,
    is_anonymized    boolean                     NOT NULL,
    created_at       timestamp(6) with time zone NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE activation_tokens (
    id          uuid                        NOT NULL,
    user_id     uuid                        NOT NULL,
    hash        varchar(64)                 NOT NULL,
    created_at  timestamp(6) with time zone NOT NULL,
    expires_at  timestamp(6) with time zone NOT NULL,
    CONSTRAINT pk_activation_tokens PRIMARY KEY (id)
);

CREATE UNIQUE INDEX idx_activation_tokens_hash ON activation_tokens (hash);
CREATE INDEX idx_activation_tokens_user_id ON activation_tokens (user_id);


-- ============================================================================
-- Bounded context : Organisation
-- ============================================================================

CREATE TABLE organisations (
    id          uuid                        NOT NULL,
    name        varchar(150)                NOT NULL,
    created_at  timestamp(6) with time zone NOT NULL,
    CONSTRAINT pk_organisations PRIMARY KEY (id),
    CONSTRAINT uk_organisations_name UNIQUE (name)
);

CREATE TABLE members (
    id               uuid                        NOT NULL,
    organisation_id  uuid                        NOT NULL,
    user_id          uuid                        NOT NULL,
    role             varchar(20)                 NOT NULL,
    status           varchar(20)                 NOT NULL,
    joined_at        timestamp(6) with time zone NOT NULL,
    CONSTRAINT pk_members PRIMARY KEY (id),
    CONSTRAINT uk_member_org_user UNIQUE (organisation_id, user_id)
);

CREATE TABLE organisation_groups (
    id               uuid                        NOT NULL,
    organisation_id  uuid                        NOT NULL,
    name             varchar(150)                NOT NULL,
    kind             varchar(20)                 NOT NULL,
    created_at       timestamp(6) with time zone NOT NULL,
    CONSTRAINT pk_organisation_groups PRIMARY KEY (id),
    CONSTRAINT uk_group_org_name UNIQUE (organisation_id, name)
);

CREATE TABLE group_memberships (
    id               uuid                        NOT NULL,
    organisation_id  uuid                        NOT NULL,
    group_id         uuid                        NOT NULL,
    member_id        uuid                        NOT NULL,
    joined_at        timestamp(6) with time zone NOT NULL,
    CONSTRAINT pk_group_memberships PRIMARY KEY (id),
    CONSTRAINT uk_group_membership_group_member UNIQUE (group_id, member_id)
);

CREATE TABLE membership_invitations (
    id               uuid                        NOT NULL,
    organisation_id  uuid                        NOT NULL,
    invited_email    varchar(254)                NOT NULL,
    role             varchar(20)                 NOT NULL,
    status           varchar(20)                 NOT NULL,
    created_at       timestamp(6) with time zone NOT NULL,
    expires_at       timestamp(6) with time zone NOT NULL,
    CONSTRAINT pk_membership_invitations PRIMARY KEY (id)
);

CREATE INDEX idx_membership_invitations_email_status
    ON membership_invitations (invited_email, status);


-- ============================================================================
-- Bounded context : Messaging
-- ============================================================================

CREATE TABLE conversations (
    id                uuid                        NOT NULL,
    organisation_id   uuid                        NOT NULL,
    group_id          uuid,
    kind              varchar(20)                 NOT NULL,
    name              varchar(150),
    participant_low   uuid,
    participant_high  uuid,
    created_at        timestamp(6) with time zone NOT NULL,
    CONSTRAINT pk_conversations PRIMARY KEY (id),
    CONSTRAINT uk_conversation_group UNIQUE (group_id),
    CONSTRAINT uk_conversation_direct_pair UNIQUE (participant_low, participant_high)
);

CREATE TABLE messages (
    id               uuid                        NOT NULL,
    conversation_id  uuid                        NOT NULL,
    author_id        uuid                        NOT NULL,
    content          varchar(4000)               NOT NULL,
    reply_to         uuid,
    sent_at          timestamp(6) with time zone NOT NULL,
    CONSTRAINT pk_messages PRIMARY KEY (id)
);

CREATE INDEX idx_message_conversation ON messages (conversation_id);
