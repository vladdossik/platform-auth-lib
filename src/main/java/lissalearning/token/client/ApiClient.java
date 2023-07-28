package lissalearning.token.client;

import lissalearning.token.exception.ApiClientException;
import lissalearning.token.exception.ApiError;
import lissalearning.token.models.RoleAccessPostRequest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import java.util.Objects;

import static org.springframework.http.HttpMethod.POST;

@Component
public class ApiClient extends AbstractApiClient {
    private static final String CHECK_ROLE_ACCESS_API_AUTH = "/api/auth/check-access";

    public ApiClient(WebClient webClient) {
        super(webClient);
    }

    public boolean checkAccessByTokenAndRole(String role, String jwtToken) throws ApiClientException {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        ParameterizedTypeReference<String> typeReference = new ParameterizedTypeReference<String>() {
        };
        RoleAccessPostRequest roleAccessPostRequest = new RoleAccessPostRequest(role);
        try {
            String response = exchangeBlocking(
                CHECK_ROLE_ACCESS_API_AUTH,
                POST,
                headers,
                typeReference,
                ApiError.UNEXPECTED_ERROR,
                roleAccessPostRequest
            );
            return Objects.equals(response, "Access granted");
        } catch (WebClientRequestException e) {
            throw new ApiClientException(ApiError.CLIENT_ERROR, "Error while sending request.");
        }
    }
}
