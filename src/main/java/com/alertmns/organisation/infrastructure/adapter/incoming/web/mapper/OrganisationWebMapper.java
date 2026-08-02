package com.alertmns.organisation.infrastructure.adapter.incoming.web.mapper;

import com.alertmns.organisation.domain.model.Organisation;
import com.alertmns.organisation.domain.port.incoming.command.CreateOrganisationCommand;
import com.alertmns.organisation.infrastructure.adapter.incoming.web.dto.CreateOrganisationRequest;
import com.alertmns.organisation.infrastructure.adapter.incoming.web.dto.OrganisationResponse;

public final class OrganisationWebMapper {

    private OrganisationWebMapper() {}

    public static CreateOrganisationCommand toCreateOrganisationCommand(CreateOrganisationRequest request) {
        return new CreateOrganisationCommand(request.name());
    }

    public static OrganisationResponse toOrganisationResponse(Organisation organisation) {
        return new OrganisationResponse(organisation.id().value(), organisation.name().value());
    }
}
