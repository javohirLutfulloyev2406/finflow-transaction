package com.finflow.transaction.client.rest;

import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.http.client.ClientHttpRequestFactory;

import java.time.Duration;

/** connect/read timeout'ni har ikkala RestClient config'da bir xil tarzda sozlash uchun. */
final class RestClientFactorySupport {

    private RestClientFactorySupport() {
    }

    static ClientHttpRequestFactory timeoutFactory(long connectTimeoutMs, long readTimeoutMs) {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.defaults()
                .withConnectTimeout(Duration.ofMillis(connectTimeoutMs))
                .withReadTimeout(Duration.ofMillis(readTimeoutMs));
        return ClientHttpRequestFactoryBuilder.detect().build(settings);
    }
}