package com.finflow.transaction.client.rest;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * `finflow.account-service.*` — application.yml'dan o'qiladi, hardcoded emas.
 * base-url hozircha to'g'ridan-to'g'ri hostname (Docker Compose service nomi yoki
 * localhost). Service Discovery (Eureka/Consul) yoki Kubernetes DNS qo'shilganda ham
 * bu struktura o'zgarmaydi — faqat `base-url` qiymati discovery client orqali
 * resolve qilingan manzilga almashadi (masalan `http://account-service` — K8s Service
 * nomi o'ziyoq DNS orqali ishlaydi, kod o'zgarishi shart emas).
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "finflow.account-service")
public class AccountServiceProperties {

    private String baseUrl;
    private long connectTimeoutMs = 2000;
    private long readTimeoutMs = 5000;
}
