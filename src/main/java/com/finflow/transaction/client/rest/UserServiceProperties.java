package com.finflow.transaction.client.rest;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** `finflow.user-service.*` — AccountServiceProperties bilan bir xil mantiq. */
@Getter
@Setter
@ConfigurationProperties(prefix = "finflow.user-service")
public class UserServiceProperties {

    private String baseUrl;
    private long connectTimeoutMs = 2000;
    private long readTimeoutMs = 3000;
}