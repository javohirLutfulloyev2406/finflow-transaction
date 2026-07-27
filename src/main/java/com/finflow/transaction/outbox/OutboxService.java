package com.finflow.transaction.outbox;

import com.finflow.transaction.messaging.event.TransactionEvent;

public interface OutboxService {

    void publish(String aggregateType, String aggregateId, TransactionEvent event);
}