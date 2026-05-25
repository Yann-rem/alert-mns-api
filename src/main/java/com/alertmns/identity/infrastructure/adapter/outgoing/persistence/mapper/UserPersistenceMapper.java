package com.alertmns.identity.infrastructure.adapter.outgoing.persistence.mapper;

import com.alertmns.identity.domain.model.AbsenceMessage;
import com.alertmns.identity.domain.model.Email;
import com.alertmns.identity.domain.model.FirstName;
import com.alertmns.identity.domain.model.HashedPassword;
import com.alertmns.identity.domain.model.LastName;
import com.alertmns.identity.domain.model.Profile;
import com.alertmns.identity.domain.model.User;
import com.alertmns.identity.infrastructure.adapter.outgoing.persistence.UserJpaEntity;
import com.alertmns.shared.UserId;

public final class UserPersistenceMapper {

    private UserPersistenceMapper() {}

    public static User toDomain(UserJpaEntity entity) {
        return User.reconstitute(
                UserId.from(entity.getId()),
                Email.of(entity.getEmail()),
                HashedPassword.of(entity.getHashedPassword()),
                buildProfile(entity),
                entity.getStatus(),
                entity.isAnonymized(),
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
                domain.email().value(),
                domain.hashedPassword().value(),
                domain.profile().firstName().value(),
                domain.profile().lastName().value(),
                domain.profile().avatar().orElse(null),
                domain.profile().absenceMessage().map(AbsenceMessage::content).orElse(null),
                domain.profile().absenceMessage().map(AbsenceMessage::active).orElse(null),
                domain.status(),
                domain.isAnonymized(),
                domain.createdAt()
        );
    }
}
