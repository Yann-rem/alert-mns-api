package com.alertmns.iam.infrastructure.adapter.incoming.web;

import com.alertmns.iam.domain.model.UserId;
import com.alertmns.iam.domain.port.incoming.ActivateUserUseCase;
import com.alertmns.iam.domain.port.incoming.DisableUserUseCase;
import com.alertmns.iam.domain.port.incoming.RegisterUserUseCase;
import com.alertmns.iam.domain.port.incoming.UpdateAbsenceMessageUseCase;
import com.alertmns.iam.domain.port.incoming.UpdateProfileUseCase;
import com.alertmns.iam.domain.port.incoming.command.ActivateUserCommand;
import com.alertmns.iam.domain.port.incoming.command.DisableUserCommand;
import com.alertmns.iam.infrastructure.adapter.incoming.web.dto.RegisterUserRequest;
import com.alertmns.iam.infrastructure.adapter.incoming.web.dto.RegisterUserResponse;
import com.alertmns.iam.infrastructure.adapter.incoming.web.dto.UpdateAbsenceMessageRequest;
import com.alertmns.iam.infrastructure.adapter.incoming.web.dto.UpdateProfileRequest;
import com.alertmns.iam.infrastructure.adapter.incoming.web.mapper.UserWebMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;
    private final ActivateUserUseCase activateUserUseCase;
    private final DisableUserUseCase disableUserUseCase;
    private final UpdateProfileUseCase updateProfileUseCase;
    private final UpdateAbsenceMessageUseCase updateAbsenceMessageUseCase;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterUserResponse register(@Valid @RequestBody RegisterUserRequest request) {
        UserId id = registerUserUseCase.register(UserWebMapper.toCommand(request));
        return new RegisterUserResponse(id.value().toString());
    }

    @PostMapping("/{userId}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activate(@PathVariable String userId) {
        activateUserUseCase.activate(new ActivateUserCommand(userId));
    }

    @PostMapping("/{userId}/disable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(@PathVariable String userId) {
        disableUserUseCase.disable(new DisableUserCommand(userId));
    }

    @PutMapping("/{userId}/profile")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateProfile(
            @PathVariable String userId,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        updateProfileUseCase.update(UserWebMapper.toCommand(userId, request));
    }

    @PutMapping("/{userId}/absence-message")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateAbsenceMessage(
            @PathVariable String userId,
            @Valid @RequestBody UpdateAbsenceMessageRequest request
    ) {
        updateAbsenceMessageUseCase.update(UserWebMapper.toCommand(userId, request));
    }
}
