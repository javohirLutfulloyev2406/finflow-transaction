package com.finflow.transaction.mapper;

import com.finflow.transaction.domain.TransactionEntity;
import com.finflow.transaction.domain.TransactionEntryEntity;
import com.finflow.transaction.domain.vo.Money;
import com.finflow.transaction.dto.command.DepositCommand;
import com.finflow.transaction.dto.command.TransferCommand;
import com.finflow.transaction.dto.command.WithdrawCommand;
import com.finflow.transaction.dto.request.DepositRequest;
import com.finflow.transaction.dto.request.TransferRequest;
import com.finflow.transaction.dto.request.WithdrawRequest;
import com.finflow.transaction.dto.response.TransactionEntryResponse;
import com.finflow.transaction.dto.response.TransactionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * unmappedTargetPolicy = ERROR — ataylab.
 * Bank loyihasida "unutilgan maydon jimgina null bo'lib qoldi" — eng qimmat xato.
 * Kompilyator to'xtatib qolsin.
 *
 * Money -> (amount, currency) uchun @Named metod SHART EMAS: MapStruct nested
 * source path'ni ("amount.amount") to'g'ridan-to'g'ri tushunadi. Kamroq kod, kamroq xato.
 *
 * Teskari yo'nalish (BigDecimal + Currency -> Money) bir nechta source'dan bitta
 * obyekt yasaydi — buni @Named qo'llab-quvvatlamaydi (u faqat bitta argument oladi),
 * shuning uchun `expression` ishlatilgan.
 */
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        imports = Money.class)
public interface TransactionMapper {

    // ----------------- Entity -> Response -----------------

    @Mapping(target = "amount", source = "amount.amount")
    @Mapping(target = "currency", source = "amount.currency")
    TransactionResponse toResponse(TransactionEntity entity);

    List<TransactionResponse> toResponseList(List<TransactionEntity> entities);

    @Mapping(target = "amount", source = "amount.amount")
    @Mapping(target = "currency", source = "amount.currency")
    @Mapping(target = "balanceAfter", source = "balanceAfter.amount")
    TransactionEntryResponse toEntryResponse(TransactionEntryEntity entity);

    // ----------------- Request -> Command -----------------

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "idempotencyKey", source = "idempotencyKey")
    @Mapping(target = "deviceId", source = "deviceId")
    @Mapping(target = "ipAddress", source = "ipAddress")
    @Mapping(target = "sourceAccountId", source = "request.sourceAccountId")
    @Mapping(target = "targetAccountId", source = "request.targetAccountId")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "amount",
            expression = "java(Money.of(request.getAmount(), request.getCurrency()))")
    TransferCommand toCommand(TransferRequest request, Long userId, String idempotencyKey,
                              String deviceId, String ipAddress);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "idempotencyKey", source = "idempotencyKey")
    @Mapping(target = "deviceId", source = "deviceId")
    @Mapping(target = "ipAddress", source = "ipAddress")
    @Mapping(target = "targetAccountId", source = "request.targetAccountId")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "amount",
            expression = "java(Money.of(request.getAmount(), request.getCurrency()))")
    DepositCommand toCommand(DepositRequest request, Long userId, String idempotencyKey,
                             String deviceId, String ipAddress);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "idempotencyKey", source = "idempotencyKey")
    @Mapping(target = "deviceId", source = "deviceId")
    @Mapping(target = "ipAddress", source = "ipAddress")
    @Mapping(target = "sourceAccountId", source = "request.sourceAccountId")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "amount",
            expression = "java(Money.of(request.getAmount(), request.getCurrency()))")
    WithdrawCommand toCommand(WithdrawRequest request, Long userId, String idempotencyKey,
                              String deviceId, String ipAddress);
}
