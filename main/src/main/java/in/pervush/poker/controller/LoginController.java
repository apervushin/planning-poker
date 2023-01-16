package in.pervush.poker.controller;

import in.pervush.poker.exception.ErrorStatusException;
import in.pervush.poker.exception.ForbiddenException;
import in.pervush.poker.exception.InvalidConfirmationCodeException;
import in.pervush.poker.exception.InvalidEmailException;
import in.pervush.poker.exception.InvalidStepException;
import in.pervush.poker.exception.InvalidUserNameException;
import in.pervush.poker.exception.TooManyConfirmationAttemptsException;
import in.pervush.poker.model.ErrorResponse;
import in.pervush.poker.model.ErrorStatus;
import in.pervush.poker.model.login.LoginStep1Request;
import in.pervush.poker.model.login.LoginRequest;
import in.pervush.poker.model.login.LoginStep2Request;
import in.pervush.poker.model.login.LoginStep2ResponseView;
import in.pervush.poker.model.login.LoginStep3Request;
import in.pervush.poker.repository.UsersRepository;
import in.pervush.poker.service.UserService;
import in.pervush.poker.utils.auth.RequestHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.util.UUID;

@RestController
@RequestMapping(value = LoginController.PATH)
@Tag(name="Login")
@Validated
public class LoginController extends AuthenticationController {

    public static final String PATH = "/api/v1/login";

    private final UserService userService;
    private final RequestHelper requestHelper;

    public LoginController(final UsersRepository usersRepository, final RequestHelper requestHelper,
                           UserService userService) {
        super(usersRepository, requestHelper);
        this.userService = userService;
        this.requestHelper = requestHelper;
    }

    @Operation(
            summary = "Login",
            responses = {
                    @ApiResponse(responseCode = "201"),
                    @ApiResponse(responseCode = "403")
            }
    )
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Deprecated
    public ResponseEntity<Void> login(@RequestBody @Valid final LoginRequest request) {
        return login(request.email(), request.password());
    }

    @Operation(
            summary = "Login step 1",
            responses = {
                    @ApiResponse(responseCode = "201"),
                    @ApiResponse(responseCode = "400",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PostMapping(value = "/step1", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void step1(@RequestBody @Valid final LoginStep1Request request,
                      @RequestHeader(RequestHelper.DEVICE_UUID_HEADER_NAME) @Valid UUID deviceUuid) {
        try {
            userService.loginStep1(request.email(), deviceUuid);
        } catch (final InvalidEmailException ex) {
            throw new ErrorStatusException(ErrorStatus.INVALID_USER_EMAIL);
        }
    }

    @Operation(
            summary = "Login step 2",
            responses = {
                    @ApiResponse(responseCode = "201"),
                    @ApiResponse(responseCode = "400",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PostMapping(value = "/step2", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public LoginStep2ResponseView step2(@RequestBody @Valid final LoginStep2Request request,
                                        @RequestHeader(RequestHelper.DEVICE_UUID_HEADER_NAME) @Valid UUID deviceUuid) {
        try {
            final var user = userService.loginStep2(request.confirmationCode(), deviceUuid);
            user.ifPresent(dbUser -> requestHelper.setAuthCookie(dbUser.userUuid()));
            return new LoginStep2ResponseView(user.isPresent() ? LoginStep2ResponseView.LoginStep1Status.EXISTING_USER
                    : LoginStep2ResponseView.LoginStep1Status.NEW_USER);
        } catch (final InvalidStepException ex) {
            throw new ErrorStatusException(ErrorStatus.INVALID_STEP);
        } catch (final TooManyConfirmationAttemptsException ex) {
            throw new ErrorStatusException(ErrorStatus.TOO_MANY_CONFIRMATION_ATTEMPTS);
        } catch (final InvalidConfirmationCodeException ex) {
            throw new ErrorStatusException(ErrorStatus.INVALID_CONFIRMATION_CODE);
        }
    }

    @Operation(
            summary = "Login step 3",
            description = "Additional step for registration flow (if NEW_USER response received on step2)",
            responses = {
                    @ApiResponse(responseCode = "201"),
                    @ApiResponse(responseCode = "400",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PostMapping(value = "/step3", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void step3(@RequestBody @Valid final LoginStep3Request request,
                      @RequestHeader(RequestHelper.DEVICE_UUID_HEADER_NAME) @Valid UUID deviceUuid) {
        try {
            final var user = userService.loginStep3(request.name(), deviceUuid);
            requestHelper.setAuthCookie(user.userUuid());
        } catch (final InvalidStepException ex) {
            throw new ErrorStatusException(ErrorStatus.INVALID_STEP);
        } catch (final BadCredentialsException ex) {
            throw new ForbiddenException();
        } catch (final InvalidUserNameException ex) {
            throw new ErrorStatusException(ErrorStatus.INVALID_USER_NAME);
        }
    }

}