package com.finflow.transaction.client.grpc;


import com.finflow.transaction.client.dto.AccountBalance;
import com.finflow.transaction.client.dto.AccountOperationResult;
import com.finflow.transaction.domain.vo.Money;
import com.finflow.transaction.exception.AccountServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountGrpcClientImpl implements AccountGrpcClient {

    @Override
    public AccountBalance getBalance(Long accountId) {

        // TODO():
        // proto kelgach AccountServiceGrpc orqali chaqiriladi

        throw new AccountServiceUnavailableException();
    }

    @Override
    public AccountOperationResult debit(Long accountId, Money amount) {

        // TODO():
        // proto kelgach implementatsiya qilinadi

        throw new AccountServiceUnavailableException();
    }

    @Override
    public AccountOperationResult credit(Long accountId, Money amount) {

        // TODO():
        // proto kelgach implementatsiya qilinadi

        throw new AccountServiceUnavailableException();
    }
}