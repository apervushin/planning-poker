package in.pervush.poker.exception;

import in.pervush.poker.model.ErrorStatus;

public class ErrorStatusException extends RuntimeException {
    public final ErrorStatus status;

    public ErrorStatusException(ErrorStatus status) {
        this.status = status;
    }

    @Override
    public String getMessage() {
        return String.format("Error status: %s", status);
    }
}
