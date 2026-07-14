package com.finflow.transaction.client.grpc;


import com.finflow.transaction.client.dto.AccountBalance;
import com.finflow.transaction.client.dto.AccountOperationResult;
import com.finflow.transaction.domain.vo.Money;

public interface AccountGrpcClient {

    AccountBalance getBalance(Long accountId);

    AccountOperationResult debit(Long accountId, Money amount);

    AccountOperationResult credit(Long accountId, Money amount);
}