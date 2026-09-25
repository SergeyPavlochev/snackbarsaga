package ru.spavlochev.order.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${app.billing.base-url}")
    private String billingBaseUrl;
    @Value("${app.warehouse.base-url}")
    private String warehouseBaseUrl;
    @Value("${app.delivery.base-url}")
    private String deliveryBaseUrl;

    @Bean
    public WebClient billingWebClient() {
        return WebClient.builder()
                .baseUrl(billingBaseUrl)
                .build();
    }

    @Bean
    public WebClient warehouseWebClient() {
        return WebClient.builder()
                .baseUrl(warehouseBaseUrl)
                .build();
    }

    @Bean
    public WebClient deliveryWebClient() {
        return WebClient.builder()
                .baseUrl(deliveryBaseUrl)
                .build();
    }
}
