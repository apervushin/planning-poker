package in.pervush.poker.controller;

import in.pervush.poker.exception.UnauthorizedException;
import in.pervush.poker.exception.UserNotFoundException;
import in.pervush.poker.model.user.UserDetailsImpl;
import in.pervush.poker.model.user.UserPrivateView;
import in.pervush.poker.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name="User")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Get my profile",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "401", content = @Content())
            }
    )
    @GetMapping(value = "/api/v1/user", produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "Authorization")
    public UserPrivateView getUser(@AuthenticationPrincipal final UserDetailsImpl user) {
        try {
            final var dbUser = userService.getUser(user.getUserUuid());
            return UserPrivateView.of(dbUser);
        } catch (UserNotFoundException ex) {
            throw new UnauthorizedException();
        }
    }

}
