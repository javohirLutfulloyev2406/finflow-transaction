package com.finflow.transaction.dto.specification;

import com.finflow.transaction.domain.TransactionEntity;
import com.finflow.transaction.dto.filter.TransactionFilterRequest;
import com.finflow.transaction.enums.Currency;
import com.finflow.transaction.enums.TransactionStatus;
import com.finflow.transaction.enums.TransactionType;
import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;

public final class TransactionSpecification {

    private TransactionSpecification() {}

    public static Specification<TransactionEntity> hasStatus(TransactionStatus status) {
        return (root, query, cb) ->
                status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<TransactionEntity> hasType(TransactionType type) {
        return (root, query, cb) ->
                type == null ? cb.conjunction() : cb.equal(root.get("type"), type);
    }

    // Money embeddable: root.get("amount") → Money, .get("currency") → Currency enum
    public static Specification<TransactionEntity> hasCurrency(Currency currency) {
        return (root, query, cb) ->
                currency == null ? cb.conjunction() : cb.equal(root.get("amount").get("currency"), currency);
    }

    public static Specification<TransactionEntity> amountBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return cb.conjunction();
            // Ikkinchi get() da explicit type — aks holda Path<Object> infer bo'lib cb.between() type mismatch beradi
            Path<BigDecimal> path = root.get("amount").<BigDecimal>get("amount");
            if (min != null && max != null) return cb.between(path, min, max);
            if (min != null) return cb.greaterThanOrEqualTo(path, min);
            return cb.lessThanOrEqualTo(path, max);
        };
    }

    public static Specification<TransactionEntity> createdBetween(Instant from, Instant to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return cb.conjunction();
            Path<Instant> path = root.<Instant>get("createdAt");
            if (from != null && to != null) return cb.between(path, from, to);
            if (from != null) return cb.greaterThanOrEqualTo(path, from);
            return cb.lessThanOrEqualTo(path, to);
        };
    }

    // source yoki target accountni qamraydi — foydalanuvchi o'z barcha tranzaksiyalarini ko'radi
    public static Specification<TransactionEntity> involvesAccount(Long accountId) {
        return (root, query, cb) -> {
            if (accountId == null) return cb.conjunction();
            return cb.or(
                    cb.equal(root.get("sourceAccountId"), accountId),
                    cb.equal(root.get("targetAccountId"), accountId)
            );
        };
    }

    public static Specification<TransactionEntity> from(TransactionFilterRequest filter) {
        if (filter == null) return (root, query, cb) -> cb.conjunction();
        // Specification.where(Specification) deprecated — har bir metod null uchun conjunction() qaytaradi,
        // shuning uchun to'g'ridan-to'g'ri chain qilsa bo'ladi
        return hasStatus(filter.getStatus())
                .and(hasType(filter.getType()))
                .and(hasCurrency(filter.getCurrency()))
                .and(amountBetween(filter.getMinAmount(), filter.getMaxAmount()))
                .and(createdBetween(filter.getDateFrom(), filter.getDateTo()))
                .and(involvesAccount(filter.getAccountId()));
    }
}