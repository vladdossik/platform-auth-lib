package lissalearning.auth.client;

import lissalearning.auth.exception.ApiClientException;
import lissalearning.auth.exception.ApiError;
import lissalearning.auth.exception.InvalidJwtFormatException;
import lissalearning.auth.exception.MissingAuthorizationHeaderException;
import lissalearning.auth.models.UserAuthoritiesResponse;
import lissalearning.auth.models.UserDetailsImpl;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpMethod.POST;

@Component("authClient")
public class AuthClient extends AbstractApiClient {
    private static final String AUTHORIZE_USER_URL = "/api/auth/authorize";

    public AuthClient(WebClient webClient) {
        super(webClient);
    }

    public UserDetailsImpl getUserDetails(HttpServletRequest request) throws ApiClientException, InvalidJwtFormatException, MissingAuthorizationHeaderException {
        HttpHeaders headers = new HttpHeaders();
        if (request.getHeader(HttpHeaders.AUTHORIZATION) == null) {
            throw new MissingAuthorizationHeaderException("Authorization header is missing");
        }
        headers.set(HttpHeaders.AUTHORIZATION, request.getHeader(HttpHeaders.AUTHORIZATION));
        headers.setContentType(MediaType.APPLICATION_JSON);
        ParameterizedTypeReference<UserAuthoritiesResponse> typeReference = new ParameterizedTypeReference<UserAuthoritiesResponse>() {};
        try {
            UserAuthoritiesResponse response = exchangeBlocking(
                    AUTHORIZE_USER_URL,
                    POST,
                    headers,
                    typeReference,
                    ApiError.UNEXPECTED_ERROR,
                    null
            );
            return UserDetailsImpl.build(response);
        } catch (WebClientRequestException e) {
            if (e.getMessage() != null && e.getMessage().equals("Invalid JWT token format")) {
                throw new InvalidJwtFormatException(e.getMessage());
            } else {
                throw new ApiClientException(ApiError.CLIENT_ERROR, "Error while sending request.");
            }
        }
    }

    @Override
    protected void handleWebClientRequestException(Exception e) {
    }

    @Override
    protected void handleInvalidJwtFormatException() {
        String errorMessage = "Invalid JWT token format";
        throw new InvalidJwtFormatException(errorMessage);
    }

    @Override
    protected void handleMissingAuthorizationHeaderException() {
        String errorMessage = "Missing Authorization header";
        throw new MissingAuthorizationHeaderException(errorMessage);
    }
}