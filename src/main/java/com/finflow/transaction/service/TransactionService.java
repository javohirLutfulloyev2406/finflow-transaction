package com.finflow.transaction.service;

import com.finflow.transaction.dto.command.DepositCommand;
import com.finflow.transaction.dto.command.RefundCommand;
import com.finflow.transaction.dto.command.TransferCommand;
import com.finflow.transaction.dto.command.WithdrawCommand;
import com.finflow.transaction.dto.filter.TransactionFilterRequest;
import com.finflow.transaction.dto.request.CancelRequest;
import com.finflow.transaction.dto.response.CursorPageResponse;
import com.finflow.transaction.dto.response.TransactionResponse;

import java.util.UUID;

public interface TransactionService {

    TransactionResponse transfer(TransferCommand command);

    TransactionResponse deposit(DepositCommand command);

    TransactionResponse withdraw(WithdrawCommand command);

    TransactionResponse refund(UUID transactionId, RefundCommand command);

    TransactionResponse cancel(UUID transactionId, CancelRequest request);

    TransactionResponse findById(UUID transactionId);

    CursorPageResponse<TransactionResponse> history(TransactionFilterRequest filter);

}