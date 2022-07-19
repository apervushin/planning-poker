package in.pervush.poker.controller;

import in.pervush.poker.exception.UnauthorizedException;
import in.pervush.poker.exception.UserNotFoundException;
import in.pervush.poker.model.user.UserView;
import in.pervush.poker.service.UserService;
import in.pervush.poker.utils.RequestHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/user")
@Tag(name="User")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;
    private final RequestHelper requestHelper;

    @Operation(
            summary = "Get my profile",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "401", content = @Content())
            }
    )
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "Authorization")
    public UserView getUser() {
        try {
            final var userUuid = requestHelper.getUserUuidCookie();
            final var dbUser = userService.getUser(userUuid);
            return UserView.of(dbUser);
        } catch (UserNotFoundException ex) {
            throw new UnauthorizedException();
        }
    }

}
