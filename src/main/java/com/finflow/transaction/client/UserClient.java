package com.finflow.transaction.client;

import com.finflow.transaction.client.dto.UserStatus;

/**
 * user-service bilan sinxron muloqot uchun port (AccountClient bilan bir xil naqsh).
 * Transport tafsilotlari (RestClient, RestClientConfig) {@code client.rest} paketida.
 */
public interface UserClient {

    UserStatus getUserStatus(Long userId);
}