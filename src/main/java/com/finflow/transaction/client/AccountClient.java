package com.finflow.transaction.client;

import com.finflow.transaction.client.dto.AccountBalance;
import com.finflow.transaction.client.dto.AccountOperationResult;
import com.finflow.transaction.domain.vo.Money;

import java.util.UUID;

/**
 * account-service bilan sinxron muloqot uchun port.
 * Transport tafsilotlari (RestClient, RestClientConfig) {@code client.rest} paketida.
 */
public interface AccountClient {

    AccountBalance getBalance(UUID accountId);

    AccountOperationResult debit(UUID accountId, Money amount);

    AccountOperationResult credit(UUID accountId, Money amount);
}
