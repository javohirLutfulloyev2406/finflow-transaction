package com.finflow.transaction.service;

import java.util.UUID;

public interface SagaService {

    void start(UUID transactionId);

    void complete(UUID transactionId);

    void fail(UUID transactionId, String reason);

}