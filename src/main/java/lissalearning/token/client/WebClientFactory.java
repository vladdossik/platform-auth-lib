package lissalearning.token.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lissalearning.token.exception.ApiClientException;
import lissalearning.token.exception.ApiError;
import lissalearning.token.properties.WebClientProperties;
import lissalearning.token.properties.WebClientPropertiesStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Component
@Slf4j
public class WebClientFactory {

    private static final int MAX_BODY_SIZE_FOR_LOG = 512;
    private static final int LIMITED_DATA_BUFFER_LIST_SIZE = 1024 * 1024;
    private final WebClientPropertiesStorage webClientPropertiesStorage;

    public WebClientFactory(WebClientPropertiesStorage webClientPropertiesStorage) {
        this.webClientPropertiesStorage = webClientPropertiesStorage;
    }

    public WebClient getWebClient(String clientProperty, ObjectMapper objectMapper) {
        WebClientProperties properties = webClientPropertiesStorage.getWeb().get(clientProperty);

        return WebClient.builder()
            .baseUrl(properties.getBaseUrl())
            .codecs(
                clientCodecConfigurer -> {
                    clientCodecConfigurer.defaultCodecs().maxInMemorySize(LIMITED_DATA_BUFFER_LIST_SIZE);
                }
            )
            .exchangeStrategies(ExchangeStrategies.builder()
                .codecs(c -> {
                    c.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper));
                    c.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper));
                }).build())
            .clientConnector(getClientConnector(properties))
            .filter(exceptionFilter())
            .build();

    }

    private ReactorClientHttpConnector getClientConnector(WebClientProperties properties) {
        return new ReactorClientHttpConnector(
            HttpClient.create()
                .responseTimeout(Duration.ofMillis(properties.getResponseTimeout()))
        );
    }

    public ExchangeFilterFunction exceptionFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().isError()) {
                return clientResponse.bodyToMono(String.class)
                    .flatMap(errorBody -> {
                        String reducedMessage = errorBody.substring(0, MAX_BODY_SIZE_FOR_LOG);
                        printMessage(clientResponse, reducedMessage);
                        return Mono.error(getResponseException(clientResponse.statusCode()));
                    });
            } else {
                return Mono.just(clientResponse);
            }
        });
    }

    private ApiClientException getResponseException(HttpStatus status) {
        return new ApiClientException(getApiError(status), status.getReasonPhrase() + ", status : " + status.value());
    }

    private ApiError getApiError(HttpStatus status) {
        if (status.is4xxClientError()) {
            return ApiError.CLIENT_ERROR;
        }
        if (status.is5xxServerError()) {
            return ApiError.SERVER_ERROR;
        }
        return ApiError.UNEXPECTED_ERROR;
    }

    private void printMessage(ClientResponse clientResponse, String message) {
        log.debug("\nRESPONSE status code: {}\n Status text: {}\n Headers: {}\n Body: {}",
            clientResponse.statusCode().value(),
            clientResponse.statusCode().getReasonPhrase(),
            clientResponse.headers().asHttpHeaders(),
            message);
    }
}
