package in.pervush.poker.controller;

import in.pervush.poker.exception.ErrorStatusException;
import in.pervush.poker.exception.ForbiddenException;
import in.pervush.poker.exception.MembershipNotFoundException;
import in.pervush.poker.exception.TaskNotFoundException;
import in.pervush.poker.exception.TeamNotFoundException;
import in.pervush.poker.exception.UnauthorizedException;
import in.pervush.poker.exception.UserNotFoundException;
import in.pervush.poker.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionController {

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Void> handleUnauthorizedException() {
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ErrorStatusException.class)
    public ResponseEntity<ErrorResponse> handleErrorStatusException(final ErrorStatusException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getStatus()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<Void> handleTaskNotFoundException() {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Void> handleUserNotFoundException() {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MembershipNotFoundException.class)
    public ResponseEntity<Void> handleMembershipNotFoundException() {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TeamNotFoundException.class)
    public ResponseEntity<Void> handleTeamNotFoundException() {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Void> handleForbiddenException() {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

}
