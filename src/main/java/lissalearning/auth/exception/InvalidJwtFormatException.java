package lissalearning.auth.exception;


public class InvalidJwtFormatException extends RuntimeException {
    public InvalidJwtFormatException(String message) {
        super(message);
    }
}