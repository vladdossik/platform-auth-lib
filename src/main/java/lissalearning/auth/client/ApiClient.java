package lissalearning.auth.client;

import lissalearning.auth.exception.ApiClientException;
import lissalearning.auth.exception.ApiError;
import lissalearning.auth.models.RoleAccessPostRequest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

import static org.springframework.http.HttpMethod.POST;

@Component("apiClient")
public class ApiClient extends AbstractApiClient {
    private static final String CHECK_ROLE_ACCESS_API_AUTH = "/api/auth/check-access";

    public ApiClient(WebClient webClient) {
        super(webClient);
    }

    public boolean checkAccessByTokenAndRole(String role, HttpServletRequest request) throws ApiClientException {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(request.getHeader("Authorization"));
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
