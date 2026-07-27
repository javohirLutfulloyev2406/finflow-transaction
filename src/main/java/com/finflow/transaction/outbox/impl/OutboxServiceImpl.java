package com.finflow.transaction.outbox.impl;

import com.finflow.transaction.domain.OutboxEventEntity;
import com.finflow.transaction.enums.OutboxStatus;
import com.finflow.transaction.messaging.event.TransactionEvent;
import com.finflow.transaction.repository.OutboxEventRepository;
import com.finflow.transaction.service.OutboxService;
import com.finflow.transaction.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * MUHIM: bu yerda ATAYLAB @Transactional yo'q. publish() chaqiruvchi metodning
 * ambient tranzaksiyasi (masalan TransactionServiceImpl'dagi transactionTemplate.execute())
 * ICHIDA ishlashi SHART — Outbox pattern'ning butun ma'nosi shu: biznes
 * o'zgarish va event yozuvi BITTA tranzaksiyada birga commit/rollback bo'ladi.
 * Agar bu yerga REQUIRES_NEW qo'yilsa, "balans o'zgardi, lekin event yo'qoldi"
 * (yoki aksincha) holati yana paydo bo'lishi mumkin edi.
 */
@Service
@RequiredArgsConstructor
public class OutboxServiceImpl implements OutboxService {

    private final OutboxEventRepository repository;
    private final JsonUtil jsonUtil;

    @Override
    public void publish(String aggregateType, String aggregateId, TransactionEvent event) {
        OutboxEventEntity outboxEvent = OutboxEventEntity.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(event.getEventType())
                .eventId(event.getEventId().toString())
                .payload(jsonUtil.toJson(event))
                .status(OutboxStatus.NEW)
                .createdAt(Instant.now())
                .build();

        repository.save(outboxEvent);
    }
}