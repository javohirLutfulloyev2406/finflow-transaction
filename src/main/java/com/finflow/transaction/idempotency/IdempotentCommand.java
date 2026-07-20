package com.finflow.transaction.idempotency;

public interface IdempotentCommand {
    Long userId();
    String idempotencyKey();
}