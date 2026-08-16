package com.finflow.transaction.client.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(UserServiceProperties.class)
@RequiredArgsConstructor
public class UserRestClientConfig {

    private final UserServiceProperties properties;
    private final AuthHeaderPropagationInterceptor authHeaderPropagationInterceptor;

    @Bean
    public RestClient userServiceRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl(properties.getBaseUrl())
                .requestFactory(RestClientFactorySupport.timeoutFactory(
                        properties.getConnectTimeoutMs(), properties.getReadTimeoutMs()))
                .requestInterceptor(authHeaderPropagationInterceptor)
                .build();
    }
}