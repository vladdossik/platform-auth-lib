package lissalearning.auth.client;

import lissalearning.auth.exception.ApiClientException;
import lissalearning.auth.exception.ApiError;
import lissalearning.auth.exception.InvalidJwtFormatException;
import lissalearning.auth.exception.MissingAuthorizationHeaderException;
import lissalearning.auth.models.AuthenticationContextHolder;
import lissalearning.auth.models.UserAuthoritiesResponse;
import lissalearning.auth.models.UserDetailsImpl;
import lissalearning.auth.models.UserInfo;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import javax.servlet.http.HttpServletRequest;

import java.util.UUID;

import static org.springframework.http.HttpMethod.POST;

@Component("authClient")
public class AuthClient extends AbstractApiClient {
    private static final String AUTHORIZE_USER_URL = "/api/auth/authorize";

    public AuthClient(WebClient webClient) {
        super(webClient);
    }

    public UserDetailsImpl getUserDetails(HttpServletRequest request) throws ApiClientException, InvalidJwtFormatException, MissingAuthorizationHeaderException {
        HttpHeaders headers = new HttpHeaders();
        if (request.getHeader(HttpHeaders.AUTHORIZATION) == null || request.getHeader(HttpHeaders.AUTHORIZATION).isEmpty()) {
            throw new MissingAuthorizationHeaderException("Authorization header is missing");
        }
        headers.set(HttpHeaders.AUTHORIZATION, request.getHeader(HttpHeaders.AUTHORIZATION));
        headers.setContentType(MediaType.APPLICATION_JSON);
        ParameterizedTypeReference<UserAuthoritiesResponse> typeReference = new ParameterizedTypeReference<UserAuthoritiesResponse>() {
        };
        try {
            AuthenticationContextHolder.cleanup();
            UserAuthoritiesResponse response = exchangeBlocking(
                    AUTHORIZE_USER_URL,
                    POST,
                    headers,
                    typeReference,
                    ApiError.UNEXPECTED_ERROR,
                    null
            );
            extractAndSaveUserInfo(request.getHeader("Authorization"), "JWT", response);
            return UserDetailsImpl.build(response);
        } catch (WebClientRequestException e) {
            if (e.getMessage() != null && e.getMessage().equals("Invalid JWT token format")) {
                throw new InvalidJwtFormatException(e.getMessage());
            } else {
                throw new ApiClientException(ApiError.CLIENT_ERROR, "Error while sending request.");
            }
        }
    }

    private void extractAndSaveUserInfo(String token, String tokenType, UserAuthoritiesResponse response) {
        UserInfo info = extractUserInfo(response);
        AuthenticationContextHolder.setToken(token);
        AuthenticationContextHolder.setTokenType(tokenType);
        AuthenticationContextHolder.setUserInfo(info);
    }

    private UserInfo extractUserInfo(UserAuthoritiesResponse response) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(response.getExternalId());
        userInfo.setEmail(response.getEmail());
        userInfo.setUsername(response.getUsername());
        return userInfo;
    }
}