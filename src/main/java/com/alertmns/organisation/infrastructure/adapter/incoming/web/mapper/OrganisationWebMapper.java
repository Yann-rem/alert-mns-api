package com.alertmns.organisation.infrastructure.adapter.incoming.web.mapper;

import com.alertmns.organisation.domain.port.incoming.command.CreateOrganisationCommand;
import com.alertmns.organisation.infrastructure.adapter.incoming.web.dto.CreateOrganisationRequest;

public final class OrganisationWebMapper {

    private OrganisationWebMapper() {}

    public static CreateOrganisationCommand toCreateOrganisationCommand(CreateOrganisationRequest request) {
        return new CreateOrganisationCommand(request.name());
    }
}
