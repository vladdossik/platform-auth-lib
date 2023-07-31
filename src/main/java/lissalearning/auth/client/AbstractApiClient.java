package lissalearning.auth.client;

import lissalearning.auth.exception.ApiClientException;
import lissalearning.auth.exception.ApiError;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

public abstract class AbstractApiClient {

    final private WebClient webClient;

    public AbstractApiClient(WebClient webClient) {
        this.webClient = webClient;
    }

    protected <T> Mono<T> exchange(
        String uri,
        HttpMethod httpMethod,
        HttpHeaders httpHeaders,
        ParameterizedTypeReference<T> responseType,
        ApiError timeoutExceedingError,
        Object body,
        Object... uriVariables
    ) throws ApiClientException {
        try {
            return buildRequest(httpMethod, httpHeaders, uri, body, uriVariables)
                .retrieve()
                .bodyToMono(responseType);
        } catch (WebClientRequestException e) {
            throw new ApiClientException(timeoutExceedingError, e.getMessage());
        }
    }

    protected <T> T exchangeBlocking(
        String uri,
        HttpMethod httpMethod,
        HttpHeaders httpHeaders,
        ParameterizedTypeReference<T> responseType,
        ApiError timeoutExceedingError,
        Object body,
        Object... uriVariables
    ) throws ApiClientException {
        return exchange(
            uri,
            httpMethod,
            httpHeaders,
            responseType,
            timeoutExceedingError,
            body,
            uriVariables
        )
            .block();
    }

    private WebClient.RequestHeadersSpec<?> buildRequest(
        HttpMethod httpMethod,
        HttpHeaders httpHeaders,
        String path,
        Object body,
        Object... params
    ) {
        Consumer<HttpHeaders> httpHeadersConsumer = it -> {
            if (httpHeaders != null) {
                it.addAll(httpHeaders);
            }
        };

        if (body == null) {
            return webClient
                .method(httpMethod)
                .uri(path, params)
                .headers(httpHeadersConsumer);
        }

        return webClient
            .method(httpMethod)
            .uri(path, params)
            .headers(httpHeadersConsumer)
            .bodyValue(body);
    }

}