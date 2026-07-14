package com.finflow.transaction.service;

public interface OutboxService {

    void publish(
            String aggregateType,
            String aggregateId,
            String eventType,
            Object payload
    );

}