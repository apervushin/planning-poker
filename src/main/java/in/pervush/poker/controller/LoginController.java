package in.pervush.poker.controller;

import in.pervush.poker.exception.UserNotFoundException;
import in.pervush.poker.model.ErrorResponse;
import in.pervush.poker.model.login.LoginRequest;
import in.pervush.poker.service.AuthenticationService;
import in.pervush.poker.utils.RequestHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/api/v1/login")
@Tag(name="Login")
@RequiredArgsConstructor
@Validated
public class LoginController {

    private final AuthenticationService service;
    private final RequestHelper requestHelper;

    @Operation(
            summary = "Login",
            responses = {
                    @ApiResponse(responseCode = "201"),
                    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> login(@RequestBody @Valid final LoginRequest request) {
        try {
            final var token = service.login(request.email(), request.password());
            requestHelper.setAuthCookie(token);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (UserNotFoundException ex) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

}
