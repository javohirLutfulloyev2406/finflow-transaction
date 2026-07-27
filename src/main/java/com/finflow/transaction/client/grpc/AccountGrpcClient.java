package com.finflow.transaction.client.grpc;


import com.finflow.transaction.client.dto.AccountBalance;
import com.finflow.transaction.client.dto.AccountOperationResult;
import com.finflow.transaction.domain.vo.Money;

import java.util.UUID;

public interface AccountGrpcClient {

    AccountBalance getBalance(UUID accountId);

    AccountOperationResult debit(UUID accountId, Money amount);

    AccountOperationResult credit(UUID accountId, Money amount);
}