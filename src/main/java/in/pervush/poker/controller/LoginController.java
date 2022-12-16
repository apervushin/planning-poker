package in.pervush.poker.controller;

import in.pervush.poker.model.login.LoginRequest;
import in.pervush.poker.utils.auth.RequestHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = LoginController.PATH)
@Tag(name="Login")
@Validated
public class LoginController extends AuthenticationController {

    public static final String PATH = "/api/v1/login";

    public LoginController(final AuthenticationManager authenticationManager, final RequestHelper requestHelper) {
        super(authenticationManager, requestHelper);
    }

    @Operation(
            summary = "Login",
            responses = {
                    @ApiResponse(responseCode = "201"),
                    @ApiResponse(responseCode = "403")
            }
    )
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> login(@RequestBody @Valid final LoginRequest request) {
        return login(request.email(), request.password());
    }

}
