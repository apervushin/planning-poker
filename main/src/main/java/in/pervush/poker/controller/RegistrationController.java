package in.pervush.poker.controller;

import in.pervush.poker.exception.EmailExistsException;
import in.pervush.poker.exception.ErrorStatusException;
import in.pervush.poker.exception.InvalidEmailException;
import in.pervush.poker.exception.InvalidUserNameException;
import in.pervush.poker.exception.TooWeakPasswordException;
import in.pervush.poker.model.ErrorResponse;
import in.pervush.poker.model.ErrorStatus;
import in.pervush.poker.model.registration.RegisterRequest;
import in.pervush.poker.repository.UsersRepository;
import in.pervush.poker.service.UserService;
import in.pervush.poker.utils.auth.RequestHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
@RequestMapping(value = RegistrationController.PATH)
@Tag(name="Registration")
@Validated
@Deprecated
public class RegistrationController extends AuthenticationController {

    public static final String PATH = "/api/v1/registration";
    private final UserService userService;


    public RegistrationController(final UsersRepository usersRepository, final RequestHelper requestHelper,
                                  final UserService userService) {
        super(usersRepository, requestHelper);
        this.userService = userService;
    }

    @Operation(
            summary = "Register",
            responses = {
                    @ApiResponse(responseCode = "201"),
                    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Deprecated
    public ResponseEntity<Void> register(@RequestBody @Valid final RegisterRequest request) {
        try {
            userService.createUser(request.email(), request.password(), request.name());
            return login(request.email(), request.password());
        } catch (final EmailExistsException ex) {
            throw new ErrorStatusException(ErrorStatus.USER_EMAIL_EXISTS);
        } catch (final InvalidEmailException ex) {
            throw new ErrorStatusException(ErrorStatus.INVALID_USER_EMAIL);
        } catch (final InvalidUserNameException ex) {
            throw new ErrorStatusException(ErrorStatus.INVALID_USER_NAME);
        } catch (final TooWeakPasswordException ex) {
            throw new ErrorStatusException(ErrorStatus.TOO_WEAK_USER_PASSWORD);
        }
    }
}
