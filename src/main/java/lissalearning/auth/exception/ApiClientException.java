package lissalearning.auth.exception;


public class ApiClientException extends Throwable {
    private final ApiError apiError;

    public ApiClientException(ApiError apiError, String message) {
        super(message);
        this.apiError = apiError;
    }
}
