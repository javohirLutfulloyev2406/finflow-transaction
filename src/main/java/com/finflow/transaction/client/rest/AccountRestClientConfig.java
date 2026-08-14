package com.finflow.transaction.client.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AccountRestClientConfig {

    // TODO: base-url hozircha hardcoded qiymatdan o'qiladi (application.yml).
    // Service discovery (Eureka/Consul) qo'shilsa shu joy o'zgaradi.
    @Bean
    public RestClient accountServiceRestClient(
            RestClient.Builder builder,
            @Value("${finflow.account-service.base-url}") String baseUrl
    ) {
        return builder.baseUrl(baseUrl).build();
    }
}
