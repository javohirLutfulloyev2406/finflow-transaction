package com.finflow.transaction.service.impl;

import com.finflow.transaction.client.AccountClient;
import com.finflow.transaction.client.dto.AccountOperationResult;
import com.finflow.transaction.domain.SystemAccounts;
import com.finflow.transaction.domain.TransactionEntity;
import com.finflow.transaction.domain.TransactionEntryEntity;
import com.finflow.transaction.domain.vo.Money;
import com.finflow.transaction.dto.command.DepositCommand;
import com.finflow.transaction.dto.command.RefundCommand;
import com.finflow.transaction.dto.command.TransferCommand;
import com.finflow.transaction.dto.command.WithdrawCommand;
import com.finflow.transaction.dto.filter.TransactionFilterRequest;
import com.finflow.transaction.dto.request.CancelRequest;
import com.finflow.transaction.dto.response.CursorPageResponse;
import com.finflow.transaction.dto.response.TransactionResponse;
import com.finflow.transaction.enums.EntryType;
import com.finflow.transaction.enums.TransactionStatus;
import com.finflow.transaction.enums.TransactionType;
import com.finflow.transaction.idempotency.IdempotencyService;
import com.finflow.transaction.idempotency.RequestHasher;
import com.finflow.transaction.mapper.TransactionMapper;
import com.finflow.transaction.messaging.event.TransactionCompletedEvent;
import com.finflow.transaction.messaging.event.TransactionCreatedEvent;
import com.finflow.transaction.messaging.event.TransactionFailedEvent;
import com.finflow.transaction.repository.TransactionRepository;
import com.finflow.transaction.service.OutboxService;
import com.finflow.transaction.service.SagaService;
import com.finflow.transaction.service.TransactionService;
import com.finflow.transaction.util.JsonUtil;
import com.finflow.transaction.util.ReferenceGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * MUHIM: bu klass ATAYLAB @Transactional emas.
 * REST client chaqiruvi (tarmoq, sekin) ochiq DB connection/lock bilan bir vaqtda
 * yuz bermasligi kerak. Shu sabab TransactionTemplate orqali 2 ta QISQA
 * tranzaksiya ochiladi (PENDING yozish, keyin COMPLETED/FAILED yozish),
 * ular orasida REST client chaqiruvi hech qanday tranzaksiyasiz ishlaydi.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final SagaService sagaService;
    private final AccountClient accountClient;
    private final IdempotencyService idempotencyService;
    private final RequestHasher requestHasher;
    private final JsonUtil jsonUtil;
    private final OutboxService outboxService;
    private final ReferenceGenerator referenceGenerator;
    private final TransactionTemplate transactionTemplate;



    @Override
    public TransactionResponse deposit(DepositCommand command) {
        String requestHash = requestHasher.hash(jsonUtil.toJson(command));

        // 1) Idempotency — REQUIRES_NEW ichida, o'z holicha commit bo'ladi.
        Optional<String> cached = idempotencyService.tryAcquire(
                command.userId(), command.idempotencyKey(), requestHash);
        if (cached.isPresent()) {
            return extractCachedResponse(cached.get());
        }

        // 2) TX-1 (qisqa): INITIATED -> PENDING, outbox'ga "created" event.
        TransactionEntity transaction = transactionTemplate.execute(status -> {
            TransactionEntity tx = TransactionEntity.builder()
                    .reference(referenceGenerator.generate())
                    .type(TransactionType.DEPOSIT)
                    .status(TransactionStatus.INITIATED)
                    .amount(command.amount())
                    .targetAccountId(command.targetAccountId())
                    .sourceUserId(command.userId())
                    .targetUserId(command.userId())
                    .idempotencyKey(command.idempotencyKey())
                    .description(command.description())
                    .initiatedAt(Instant.now())
                    .deviceId(command.deviceId())
                    .ipAddress(command.ipAddress())
                    .build();

            tx.transitionTo(TransactionStatus.PENDING);
            transactionRepository.save(tx);
            publishCreatedEvent(tx);
            return tx;
        });

        // 3) Tashqi chaqiruv — HECH QANDAY DB tranzaksiyasiz.
        AccountOperationResult result;
        try {
            result = accountClient.credit(command.targetAccountId(), command.amount());
        } catch (RuntimeException e) {
            log.error("Account service credit failed for transaction={}, targetAccount={}",
                    transaction.getId(), command.targetAccountId(), e);
            TransactionResponse response = finalizeFailed(transaction.getId(), "ACCOUNT_SERVICE_TIMEOUT", e.getMessage());
            completeIdempotency(command.userId(), command.idempotencyKey(), requestHash, 502, response);
            throw e;
        }

        // 4) TX-2 (qisqa): natijaga qarab COMPLETED yoki FAILED, ledger, outbox event.
        TransactionResponse response;
        if (result.success()) {
            response = finalizeCompleted(transaction.getId(), command.targetAccountId(), EntryType.CREDIT, command.amount());            completeIdempotency(command.userId(), command.idempotencyKey(), requestHash, 502, response);        } else {
            response = finalizeFailed(transaction.getId(), "ACCOUNT_CREDIT_FAILED", result.message());
            completeIdempotency(command.userId(), command.idempotencyKey(), requestHash, 502, response);        }

        return response;
    }

    @Override
    public TransactionResponse withdraw(WithdrawCommand command) {
        String requestHash = requestHasher.hash(jsonUtil.toJson(command));

        Optional<String> cached = idempotencyService.tryAcquire(
                command.userId(), command.idempotencyKey(), requestHash);
        if (cached.isPresent()) {
            return extractCachedResponse(cached.get());
        }

        TransactionEntity transaction = transactionTemplate.execute(status -> {
            TransactionEntity tx = TransactionEntity.builder()
                    .reference(referenceGenerator.generate())
                    .type(TransactionType.WITHDRAW)
                    .status(TransactionStatus.INITIATED)
                    .amount(command.amount())
                    .sourceAccountId(command.sourceAccountId())
                    .sourceUserId(command.userId())
                    .targetUserId(command.userId())
                    .idempotencyKey(command.idempotencyKey())
                    .description(command.description())
                    .initiatedAt(Instant.now())
                    .deviceId(command.deviceId())
                    .ipAddress(command.ipAddress())
                    .build();

            tx.transitionTo(TransactionStatus.PENDING);
            transactionRepository.save(tx);
            publishCreatedEvent(tx);
            return tx;
        });

        // Diqqat: balansni oldindan getBalance() bilan tekshirmaymiz — check-then-act
        // ikki servis chegarasida race condition beradi (tekshiruv va debit orasida
        // boshqa so'rov balansni o'zgartirishi mumkin). debit() o'zi atomik tekshiradi.
        AccountOperationResult result;
        try {
            result = accountClient.debit(command.sourceAccountId(), command.amount());
        } catch (RuntimeException e) {
            log.error("Account service debit failed for transaction={}, sourceAccount={}",
                    transaction.getId(), command.sourceAccountId(), e);
            TransactionResponse response = finalizeFailed(transaction.getId(), "ACCOUNT_SERVICE_TIMEOUT", e.getMessage());
            completeIdempotency(command.userId(), command.idempotencyKey(), requestHash, 502, response);
            throw e;
        }

        TransactionResponse response;
        if (result.success()) {
            response = finalizeCompleted(transaction.getId(), command.sourceAccountId(),
                    EntryType.DEBIT, command.amount());
            completeIdempotency(command.userId(), command.idempotencyKey(), requestHash, 200, response);
        } else {
            // TODO(javohir): account-service hozircha aniq failure kodi (masalan
            // INSUFFICIENT_FUNDS vs ACCOUNT_FROZEN) qaytarmaydi — AccountOperationResult'da
            // faqat erkin matnli message bor. Proto kelgach failureCode structured bo'ladi,
            // shunda InsufficientFundsException shu yerda aniq throw qilinadi.
            response = finalizeFailed(transaction.getId(), "ACCOUNT_DEBIT_FAILED", result.message());
            completeIdempotency(command.userId(), command.idempotencyKey(), requestHash, 422, response);
        }

        return response;
    }


    @Override
    public TransactionResponse transfer(TransferCommand command) {
        String requestHash = requestHasher.hash(jsonUtil.toJson(command));

        Optional<String> cached = idempotencyService.tryAcquire(
                command.userId(), command.idempotencyKey(), requestHash);
        if (cached.isPresent()) {
            return extractCachedResponse(cached.get());
        }

        // TX-1: PENDING + saga(STARTED/DEBIT_SOURCE) + CreatedEvent — bittada commit.
        TransactionEntity transaction = transactionTemplate.execute(status -> {
            TransactionEntity tx = TransactionEntity.builder()
                    .reference(referenceGenerator.generate())
                    .type(TransactionType.TRANSFER)
                    .status(TransactionStatus.INITIATED)
                    .amount(command.amount())
                    .sourceAccountId(command.sourceAccountId())
                    .targetAccountId(command.targetAccountId())
                    .sourceUserId(command.userId())
                    .idempotencyKey(command.idempotencyKey())
                    .description(command.description())
                    .initiatedAt(Instant.now())
                    .deviceId(command.deviceId())
                    .ipAddress(command.ipAddress())
                    .build();

            tx.transitionTo(TransactionStatus.PENDING);
            transactionRepository.save(tx);
            sagaService.start(tx.getId(), command.sourceAccountId(), command.targetAccountId(), command.amount());
            publishCreatedEvent(tx);
            return tx;
        });

        UUID transactionId = transaction.getId();

        // 1-QADAM: DEBIT_SOURCE — DB tranzaksiyasidan tashqarida.
        AccountOperationResult debitResult;
        try {
            debitResult = accountClient.debit(command.sourceAccountId(), command.amount());
        } catch (RuntimeException e) {
            log.error("Account service debit failed (transfer), transaction={}", transactionId, e);
            TransactionResponse response = finalizeTransferFailed(transactionId, "ACCOUNT_SERVICE_TIMEOUT_DEBIT", e.getMessage());
            completeIdempotency(command.userId(), command.idempotencyKey(), requestHash, 502, response);
            throw e;
        }

        if (!debitResult.success()) {
            // Hech narsa hali ko'chmadi — kompensatsiya shart emas.
            TransactionResponse response = finalizeTransferFailed(
                    transactionId, "SOURCE_DEBIT_FAILED", debitResult.message());
            completeIdempotency(command.userId(), command.idempotencyKey(), requestHash, 422, response);
            return response;
        }

        // TX-2: saga -> DEBITED. CREDIT'dan OLDIN commit qilinadi: agar shu yerdan keyin
        // process qulasa, saga_states "pul source'dan chiqqan, hali targetga tushmagan"
        // holatini aniq ko'rsatadi — recovery job buni topib davom ettiradi.
        transactionTemplate.executeWithoutResult(status -> sagaService.markDebited(transactionId));

        // 2-QADAM: CREDIT_TARGET.
        AccountOperationResult creditResult;
        try {
            creditResult = accountClient.credit(command.targetAccountId(), command.amount());
        } catch (RuntimeException e) {
            log.warn("Account service credit failed (transfer), kompensatsiya boshlanadi. transaction={}", transactionId, e);
            return compensate(transactionId, command, requestHash, "ACCOUNT_SERVICE_TIMEOUT_CREDIT: " + e.getMessage());
        }

        if (!creditResult.success()) {
            log.warn("Target credit rad etildi, kompensatsiya boshlanadi. transaction={}", transactionId);
            return compensate(transactionId, command, requestHash, "TARGET_CREDIT_FAILED: " + creditResult.message());
        }

        // Ikkalasi ham o'tdi — TX-3: ledger (haqiqiy double-entry, suspense account kerak EMAS,
        // chunki pul bizning ikkita real account'imiz orasida ko'chdi), COMPLETED, saga COMPLETED.
        TransactionResponse response = transactionTemplate.execute(status -> {
            TransactionEntity tx = transactionRepository.findByIdForUpdate(transactionId)
                    .orElseThrow(() -> new IllegalStateException("Transaction disappeared mid-flight: " + transactionId));

            TransactionEntryEntity.book(tx, command.sourceAccountId(), EntryType.DEBIT, command.amount());
            TransactionEntryEntity.book(tx, command.targetAccountId(), EntryType.CREDIT, command.amount());

            tx.transitionTo(TransactionStatus.COMPLETED);
            transactionRepository.save(tx);
            sagaService.complete(transactionId);
            publishCompletedEvent(tx);

            return transactionMapper.toResponse(tx);
        });

        completeIdempotency(command.userId(), command.idempotencyKey(), requestHash, 200, response);
        return response;
    }

    /** DEBIT o'tgan, CREDIT tushib qolgan — pulni source'ga qaytaramiz. */
    private TransactionResponse compensate(UUID transactionId, TransferCommand command,
                                           String requestHash, String reason) {
        transactionTemplate.executeWithoutResult(status -> sagaService.startCompensating(transactionId, reason));

        AccountOperationResult refundResult;
        try {
            refundResult = accountClient.credit(command.sourceAccountId(), command.amount());
        } catch (RuntimeException e) {
            return handleCompensationFailure(transactionId,
                    "ACCOUNT_SERVICE_TIMEOUT_ON_COMPENSATION: " + e.getMessage());
        }

        if (!refundResult.success()) {
            return handleCompensationFailure(transactionId, "COMPENSATION_REJECTED: " + refundResult.message());
        }

        // Kompensatsiya o'tdi: pul source'da qoldi, ledger yozuvi YO'Q
        // (net harakat nol — hech narsa haqiqatan ko'chmadi, faqat status/audit yozuvi qoladi).
        TransactionResponse response = transactionTemplate.execute(status -> {
            TransactionEntity tx = transactionRepository.findByIdForUpdate(transactionId)
                    .orElseThrow(() -> new IllegalStateException("Transaction disappeared mid-flight: " + transactionId));

            tx.markFailed(reason + " (compensated)");
            transactionRepository.save(tx);
            sagaService.markCompensated(transactionId);
            publishFailedEvent(tx, "TRANSFER_COMPENSATED", reason);

            return transactionMapper.toResponse(tx);
        });

        completeIdempotency(command.userId(), command.idempotencyKey(), requestHash, 422, response);
        return response;
    }

    /**
     * ENG OG'IR HOLAT: debit o'tdi, credit(target) tushib qoldi, VA kompensatsion
     * credit(source) HAM tushib qoldi. Pul hech qayerda ko'rinmaydi ("osilib qolgan").
     */
    private TransactionResponse handleCompensationFailure(UUID transactionId, String reason) {
        TransactionResponse response = transactionTemplate.execute(status -> {
            TransactionEntity tx = transactionRepository.findByIdForUpdate(transactionId)
                    .orElseThrow(() -> new IllegalStateException("Transaction disappeared mid-flight: " + transactionId));

            // Status ATAYLAB PENDING qoladi (FAILED/COMPLETED emas) — bu yolg'on signal
            // bermaslik uchun: pul haqiqatan "yo'lda" turibdi, holat hali yopilmagan.
            sagaService.markCompensationFailed(transactionId, reason);
            publishFailedEvent(tx, "COMPENSATION_FAILED", reason);

            return transactionMapper.toResponse(tx);
        });

        // Idempotency ATAYLAB "complete" qilinmaydi: holat hali yakunlanmagan.
        // Agar shu yerda complete() chaqirilsa, retry so'rov keshlangan (yolg'on) javobni
        // olib qo'yadi. IN_PROGRESS holatida qoldiramiz — operator tuzatgach qo'lda hal bo'ladi.
        log.error("CRITICAL: transfer kompensatsiyasi muvaffaqiyatsiz, pul osilib qolgan bo'lishi mumkin. " +
                "transactionId={}, reason={}", transactionId, reason);

        return response;
    }

    private TransactionResponse finalizeTransferFailed(UUID transactionId, String failureCode, String failureReason) {
        return transactionTemplate.execute(status -> {
            TransactionEntity tx = transactionRepository.findByIdForUpdate(transactionId)
                    .orElseThrow(() -> new IllegalStateException("Transaction disappeared mid-flight: " + transactionId));

            tx.markFailed(failureReason);
            transactionRepository.save(tx);
            sagaService.fail(transactionId, failureReason);
            publishFailedEvent(tx, failureCode, failureReason);

            return transactionMapper.toResponse(tx);
        });
    }

    // ---------------------------------------------------------------
    // Yordamchi metodlar
    // ---------------------------------------------------------------

    private TransactionResponse finalizeCompleted(UUID transactionId, UUID accountId, EntryType entryType, Money amount) {

        return transactionTemplate.execute(status -> {

            TransactionEntity tx = transactionRepository.findByIdForUpdate(transactionId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Transaction disappeared mid-flight: " + transactionId));

            TransactionEntryEntity.book(tx, accountId, entryType, amount);
            TransactionEntryEntity.book(tx, SystemAccounts.suspenseAccountFor(amount.getCurrency()), entryType.opposite(), amount);
            tx.transitionTo(TransactionStatus.COMPLETED);
            transactionRepository.save(tx);
            publishCompletedEvent(tx);

            return transactionMapper.toResponse(tx);
        });
    }

    private TransactionResponse finalizeFailed(UUID transactionId, String failureCode, String failureReason) {
        return transactionTemplate.execute(status -> {
            TransactionEntity tx = transactionRepository.findByIdForUpdate(transactionId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Transaction disappeared mid-flight: " + transactionId));

            tx.markFailed(failureReason);
            transactionRepository.save(tx);
            publishFailedEvent(tx, failureCode, failureReason);

            return transactionMapper.toResponse(tx);
        });
    }

    private void completeIdempotency(Long userId, String idempotencyKey, String requestHash, int httpStatus, TransactionResponse response) {
        idempotencyService.complete(userId, idempotencyKey, requestHash, httpStatus, jsonUtil.toJson(response));
    }

    /** IdempotencyService.tryAcquire() qaytargan {"status":N,"body":"..."} qopqog'ini ochamiz. */
    private TransactionResponse extractCachedResponse(String envelopeJson) {
        CachedEnvelope envelope = jsonUtil.fromJson(envelopeJson, CachedEnvelope.class);
        return jsonUtil.fromJson(envelope.body(), TransactionResponse.class);
    }

    private record CachedEnvelope(int status, String body) {
    }

    private void publishCreatedEvent(TransactionEntity tx) {
        TransactionCreatedEvent event = TransactionCreatedEvent.builder()
                .eventId(UUID.randomUUID())
                .transactionId(tx.getId())
                .userId(tx.getSourceUserId())
                .occurredAt(Instant.now())
                .sourceAccountId(tx.getSourceAccountId())
                .targetAccountId(tx.getTargetAccountId())
                .amount(tx.getAmount())
                .transactionType(tx.getType())
                .idempotencyKey(tx.getIdempotencyKey())
                .build();
        outboxService.publish("Transaction", tx.getId().toString(), event);
    }

    private void publishCompletedEvent(TransactionEntity tx) {
        TransactionCompletedEvent event = TransactionCompletedEvent.builder()
                .eventId(UUID.randomUUID())
                .transactionId(tx.getId())
                .userId(tx.getSourceUserId())
                .occurredAt(Instant.now())
                .sourceAccountId(tx.getSourceAccountId())
                .targetAccountId(tx.getTargetAccountId())
                .amount(tx.getAmount())
                .transactionType(tx.getType())
                .attemptCount(0)
                .build();
        outboxService.publish("Transaction", tx.getId().toString(), event);
    }

    private void publishFailedEvent(TransactionEntity tx, String failureCode, String failureReason) {
        TransactionFailedEvent event = TransactionFailedEvent.builder()
                .eventId(UUID.randomUUID())
                .transactionId(tx.getId())
                .userId(tx.getSourceUserId())
                .occurredAt(Instant.now())
                .sourceAccountId(tx.getSourceAccountId())
                .targetAccountId(tx.getTargetAccountId())
                .amount(tx.getAmount())
                .transactionType(tx.getType())
                .failureCode(failureCode)
                .failureReason(failureReason)
                .build();
        outboxService.publish("Transaction", tx.getId().toString(), event);    }

    // ---------------------------------------------------------------
    // Keyingi bosqichlarda to'ldiriladi
    // ---------------------------------------------------------------





    @Override
    public TransactionResponse refund(UUID transactionId, RefundCommand command) {
        throw new UnsupportedOperationException("TODO(javohir): keyingi bosqich");
    }

    @Override
    public TransactionResponse cancel(UUID transactionId, CancelRequest request) {
        throw new UnsupportedOperationException("TODO(javohir): keyingi bosqich");
    }

    @Override
    public TransactionResponse findById(UUID transactionId) {
        throw new UnsupportedOperationException("TODO(javohir): keyingi bosqich");
    }

    @Override
    public CursorPageResponse<TransactionResponse> history(TransactionFilterRequest filter) {
        throw new UnsupportedOperationException("TODO(javohir): keyingi bosqich");
    }
}