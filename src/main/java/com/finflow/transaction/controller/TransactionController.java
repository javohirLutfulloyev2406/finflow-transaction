package com.finflow.transaction.controller;

import com.finflow.transaction.dto.command.DepositCommand;
import com.finflow.transaction.dto.command.RefundCommand;
import com.finflow.transaction.dto.command.TransferCommand;
import com.finflow.transaction.dto.command.WithdrawCommand;
import com.finflow.transaction.dto.filter.TransactionFilterRequest;
import com.finflow.transaction.dto.request.CancelRequest;
import com.finflow.transaction.dto.request.DepositRequest;
import com.finflow.transaction.dto.request.RefundRequest;
import com.finflow.transaction.dto.request.TransferRequest;
import com.finflow.transaction.dto.request.WithdrawRequest;
import com.finflow.transaction.dto.response.CursorPageResponse;
import com.finflow.transaction.dto.response.TransactionResponse;
import com.finflow.transaction.mapper.TransactionMapper;
import com.finflow.transaction.security.SecurityUtil;
import com.finflow.transaction.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * userId hech qachon request body'dan olinmaydi — faqat SecurityUtil orqali
 * JWT'dan (SecurityContext). Aks holda foydalanuvchi boshqa birovning
 * userId'ini yuborib, uning nomidan tranzaksiya yaratishi mumkin bo'lardi.
 * Permission literal'lari finflow-user-service'dagi PermissionCodes bilan
 * qo'lda sinxronlanadi (bu servis o'sha modulga bog'liq emas): TRANSACTION:READ, TRANSACTION:CREATE.
 */
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;
    private final SecurityUtil securityUtil;

    @PostMapping("/deposit")
    @PreAuthorize("hasAuthority('TRANSACTION:CREATE')")
    public ResponseEntity<TransactionResponse> deposit(
            @Valid @RequestBody DepositRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
            HttpServletRequest httpRequest) {

        Long userId = securityUtil.getCurrentUserId();
        DepositCommand command = transactionMapper.toCommand(
                request, userId, idempotencyKey, deviceId, resolveIp(httpRequest));

        return ResponseEntity.ok(transactionService.deposit(command));
    }

    @PostMapping("/withdraw")
    @PreAuthorize("hasAuthority('TRANSACTION:CREATE')")
    public ResponseEntity<TransactionResponse> withdraw(
            @Valid @RequestBody WithdrawRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
            HttpServletRequest httpRequest) {

        Long userId = securityUtil.getCurrentUserId();
        WithdrawCommand command = transactionMapper.toCommand(
                request, userId, idempotencyKey, deviceId, resolveIp(httpRequest));

        return ResponseEntity.ok(transactionService.withdraw(command));
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasAuthority('TRANSACTION:CREATE')")
    public ResponseEntity<TransactionResponse> transfer(
            @Valid @RequestBody TransferRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
            HttpServletRequest httpRequest) {

        Long userId = securityUtil.getCurrentUserId();
        TransferCommand command = transactionMapper.toCommand(
                request, userId, idempotencyKey, deviceId, resolveIp(httpRequest));

        return ResponseEntity.ok(transactionService.transfer(command));
    }

    /**
     * TODO(javohir): TransactionServiceImpl.refund() hali "keyingi bosqich" TODO'si.
     * Refund har doim to'liq (RefundCommand'da amount yo'q) — implementatsiya
     * original tranzaksiyani transactionId orqali topib, uning summasini qaytarishi
     * va so'rovchi shu tranzaksiyaning egasi ekanini tekshirishi kerak.
     */
    @PostMapping("/{transactionId}/refund")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TransactionResponse> refund(
            @PathVariable UUID transactionId,
            @Valid @RequestBody RefundRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
            HttpServletRequest httpRequest) {

        Long userId = securityUtil.getCurrentUserId();
        RefundCommand command = transactionMapper.toCommand(
                request, userId, idempotencyKey, deviceId, resolveIp(httpRequest));

        return ResponseEntity.ok(transactionService.refund(transactionId, command));
    }

    /**
     * TODO(javohir): TransactionServiceImpl.cancel() hali "keyingi bosqich" TODO'si.
     * Implementatsiya bo'lganda shu yerda emas, service ichida tekshirilishi kerak:
     * so'rovchi tranzaksiyaning sourceUserId/targetUserId'iga tegishlimi (boshqa
     * foydalanuvchi ID'ni bilib olsa ham, faqat o'zinikini bekor qila olishi kerak).
     */
    @PostMapping("/{transactionId}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TransactionResponse> cancel(
            @PathVariable UUID transactionId,
            @Valid @RequestBody CancelRequest request) {

        return ResponseEntity.ok(transactionService.cancel(transactionId, request));
    }

    /**
     * TODO(javohir): TransactionServiceImpl.findById() hali "keyingi bosqich" TODO'si.
     * Implementatsiya bo'lganda service ichida ownership tekshiruvi qo'shilishi shart —
     * aks holda transactionId'ni bilgan har kim boshqa birovning tranzaksiyasini ko'radi.
     */
    @GetMapping("/{transactionId}")
    @PreAuthorize("hasAuthority('TRANSACTION:READ')")
    public ResponseEntity<TransactionResponse> findById(@PathVariable UUID transactionId) {
        return ResponseEntity.ok(transactionService.findById(transactionId));
    }

    /**
     * TODO(javohir): TransactionFilterRequest'ga userId maydoni qo'shildi, lekin
     * TransactionService.history() hali "keyingi bosqich" TODO'si — implementatsiya
     * bo'lganda specification shu maydon bo'yicha albatta filtrlashi kerak, aks holda
     * bu endpoint boshqa foydalanuvchilarning tranzaksiyalarini ham qaytarib yuboradi.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('TRANSACTION:READ')")
    public ResponseEntity<CursorPageResponse<TransactionResponse>> history(
            @ModelAttribute TransactionFilterRequest filter) {

        filter.setUserId(securityUtil.getCurrentUserId());
        return ResponseEntity.ok(transactionService.history(filter));
    }

    /** X-Forwarded-For bo'lsa (gateway ortida) — birinchi IP; bo'lmasa to'g'ridan-to'g'ri ulanish. */
    private String resolveIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
