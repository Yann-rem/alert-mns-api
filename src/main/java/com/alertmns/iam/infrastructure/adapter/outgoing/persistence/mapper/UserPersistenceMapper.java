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

    public static UserJpaEntity toEntity(User user) {
        return new UserJpaEntity(
                user.id().value(),
                user.organisationId().value(),
                user.email().value(),
                user.hashedPassword().value(),
                user.profile().firstName().value(),
                user.profile().lastName().value(),
                user.profile().avatar().orElse(null),
                user.profile().absenceMessage().map(AbsenceMessage::content).orElse(null),
                user.profile().absenceMessage().map(AbsenceMessage::active).orElse(null),
                user.role(),
                user.status(),
                user.createdAt()
        );
    }
}
