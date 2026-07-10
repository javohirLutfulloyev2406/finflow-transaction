package com.finflow.transaction.domain.base;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Audit + soft delete + optimistic lock.
 *
 * MUHIM: bu yerda @Id YO'Q. Sabab: TransactionEntity UUID PK ishlatadi,
 * texnik jadvallar (outbox, saga) BIGINT identity. Generic <ID> maydonga
 * @Id qo'yilsa Hibernate uni Object deb ko'radi va mapping yiqiladi.
 * Shuning uchun id har bir entity'da o'zida e'lon qilinadi.
 *
 * Instant ishlatiladi, LocalDateTime emas: microservicelar turli time-zone'da
 * ishlashi mumkin, tranzaksiya vaqti esa mutlaq nuqta bo'lishi shart.
 */
@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractAuditEntity {

    /** Moliyaviy yozuvlar hech qachon hard delete qilinmaydi (TZ 11-bo'lim). */
    @lombok.Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    /** Optimistic lock — parallel status o'zgarishini bloklaydi. */
    @Version
    @Column(name = "version", nullable = false)
    @lombok.Builder.Default
    private Long version = 0L;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 64)
    private String createdBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by", length = 64)
    private String updatedBy;
}
