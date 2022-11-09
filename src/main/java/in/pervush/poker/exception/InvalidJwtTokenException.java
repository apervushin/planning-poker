package in.pervush.poker.exception;


public class InvalidJwtTokenException extends Exception {

    public InvalidJwtTokenException(final String token, final Exception ex) {
        super(String.format("Invalid jwt token %s", token), ex);
    }
}
