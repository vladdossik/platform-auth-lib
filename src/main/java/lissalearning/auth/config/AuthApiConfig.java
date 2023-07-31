package lissalearning.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lissalearning.auth.client.WebClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AuthApiConfig {

    private static final String AUTH_PROPERTIES = "auth";

    private final WebClientFactory webClientFactory;
    private final ObjectMapper objectMapper;

    public AuthApiConfig(WebClientFactory webClientFactory, ObjectMapper objectMapper) {
        this.webClientFactory = webClientFactory;
        this.objectMapper = objectMapper;
    }

    @Bean
    public WebClient AuthWebClient() {
        return webClientFactory.getWebClient(AUTH_PROPERTIES, objectMapper);
    }
}