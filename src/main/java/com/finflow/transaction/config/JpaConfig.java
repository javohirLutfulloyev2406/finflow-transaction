package com.finflow.transaction.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaConfig {

    /**
     * Hozircha "system". Security qo'shilgach:
     *   SecurityContextHolder.getContext().getAuthentication().getName()
     * Kafka consumer / Quartz job kontekstida authentication bo'lmaydi —
     * shuning uchun fallback har doim kerak.
     */
    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.of("system");
    }
}
