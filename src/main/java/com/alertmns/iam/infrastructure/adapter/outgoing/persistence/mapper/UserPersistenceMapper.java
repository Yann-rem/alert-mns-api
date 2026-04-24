package com.alertmns.iam.infrastructure.adapter.outgoing.persistence.mapper;

import com.alertmns.iam.domain.model.AbsenceMessage;
import com.alertmns.iam.domain.model.Email;
import com.alertmns.iam.domain.model.FirstName;
import com.alertmns.iam.domain.model.HashedPassword;
import com.alertmns.iam.domain.model.LastName;
import com.alertmns.iam.domain.model.Profile;
import com.alertmns.iam.domain.model.User;
import com.alertmns.iam.domain.model.UserId;
import com.alertmns.iam.infrastructure.adapter.outgoing.persistence.UserJpaEntity;
import com.alertmns.shared.OrganisationId;

public class UserPersistenceMapper {

    private UserPersistenceMapper() {
    }

    public static User toDomain(UserJpaEntity entity) {
        return User.reconstitute(
                UserId.from(entity.getId()),
                OrganisationId.from(entity.getOrganisationId()),
                Email.of(entity.getEmail()),
                HashedPassword.of(entity.getHashedPassword()),
                buildProfile(entity),
                entity.getRole(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }

    private static Profile buildProfile(UserJpaEntity entity) {
        Profile profile = Profile.of(
                FirstName.of(entity.getFirstName()),
                LastName.of(entity.getLastName()),
                entity.getAvatar()
        );

        if (entity.getAbsenceContent() != null) {
            profile = profile.withAbsenceMessage(
                    AbsenceMessage.of(entity.getAbsenceContent(), entity.getAbsenceActive()));
        }

        return profile;
    }

    public static UserJpaEntity toEntity(User domain) {
        return new UserJpaEntity(
                domain.id().value(),
                domain.organisationId().value(),
                domain.email().value(),
                domain.hashedPassword().value(),
                domain.profile().firstName().value(),
                domain.profile().lastName().value(),
                domain.profile().avatar().orElse(null),
                domain.profile().absenceMessage().map(AbsenceMessage::content).orElse(null),
                domain.profile().absenceMessage().map(AbsenceMessage::active).orElse(null),
                domain.role(),
                domain.status(),
                domain.createdAt()
        );
    }
}
