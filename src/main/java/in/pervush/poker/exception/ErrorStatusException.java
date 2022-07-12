package in.pervush.poker.exception;

import in.pervush.poker.model.ErrorStatus;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErrorStatusException extends RuntimeException {
    ErrorStatus status;
}
