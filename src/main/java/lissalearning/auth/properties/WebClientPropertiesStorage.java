package lissalearning.auth.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "auth-lib")
public class WebClientPropertiesStorage {
    private Map<String, WebClientProperties> web;
}
