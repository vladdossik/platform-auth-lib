package lissalearning.token.properties;

import lombok.Data;

@Data
public class WebClientProperties {
    private String baseUrl;
    private Integer responseTimeout;
}
