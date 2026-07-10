package com.finflow.transaction.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder
                // Instant -> "2026-07-10T12:00:00Z", timestamp raqam emas
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                // BigDecimal 1E+2 emas, 100 bo'lib chiqsin
                .featuresToEnable(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN);
    }
}
