package in.pervush.poker.controller;

import in.pervush.poker.model.ErrorResponse;
import in.pervush.poker.model.push.SetPushTokenRequest;
import in.pervush.poker.model.user.UserDetailsImpl;
import in.pervush.poker.service.push.PushNotificationsService;
import in.pervush.poker.utils.auth.RequestHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/notifications/push")
@Tag(name="Push notifications")
@Validated
@SecurityRequirement(name = "Authorization")
public class PushNotificationsController {

    private final PushNotificationsService pushNotificationsService;

    public PushNotificationsController(PushNotificationsService pushNotificationsService) {
        this.pushNotificationsService = pushNotificationsService;
    }

    @Operation(
            summary = "Set push token",
            responses = {
                    @ApiResponse(responseCode = "201"),
                    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", content = @Content())
            }
    )
    @PutMapping(value = "/token", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void setPushToken(@RequestBody @Valid SetPushTokenRequest request,
                             @RequestHeader(RequestHelper.DEVICE_UUID_HEADER_NAME) @Valid UUID deviceUuid,
                             @AuthenticationPrincipal final UserDetailsImpl user) {
        pushNotificationsService.setPushToken(user.userUuid(), deviceUuid, request.token());
    }
}
