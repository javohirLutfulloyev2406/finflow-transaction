# Claude Code promtlari — finflow-transaction

> Tartib bilan yuboriladi. Har birining natijasini ko'rib, tasdiqlab, keyingisiga o'tasiz.
> `CLAUDE.md` loyiha root'ida turgan bo'lishi shart — u avtomatik o'qiladi.

---

## Bosqich 0 — Sessiyani ochish

```
CLAUDE.md ni o'qi. Loyiha kontekstini o'zlashtirganingni tasdiqla:
1) Bu qanday service va nima uchun eng murakkabi?
2) Money uchun qaysi tip ishlatiladi va nega?
3) Idempotency nechta qatlamda va nega?
4) Package bog'liqlik yo'nalishi qanday?

Faqat javob ber. Hech qanday fayl yaratma.
```

Javobi to'g'ri bo'lsa — davom eting. Noto'g'ri bo'lsa — `CLAUDE.md` ni aniqlashtiring.

---

## Bosqich 1 — Package skeleti

```
Vazifa: package strukturasini yaratish. FAQAT papkalar, hech qanday .java fayl emas
(bo'sh papka git'da saqlanmaydi — har biriga .gitkeep qo'y).

Base: src/main/java/com/finflow/transaction/

Quyidagi package'larni yarat:
config, config/listener, controller, controller/admin, grpc,
domain, domain/base, domain/vo, enums,
dto/request, dto/response, dto/filter, dto/specification, dto/command,
mapper, repository, service, service/impl,
idempotency, saga, saga/step, saga/compensation, outbox,
messaging, messaging/event, messaging/event/incoming, messaging/producer, messaging/consumer,
fraud, fraud/rule, client, client/grpc, client/fallback,
scheduler/job, security, exception, validation, util

Shuningdek:
src/main/resources/db/migration/
src/main/proto/
src/test/java/com/finflow/transaction/{architecture,integration,unit}/

Oxirida `tree src/` chiqarib ko'rsat.
```

---

## Bosqich 2 — Enum'lar (eng past qatlam, hech narsaga bog'liq emas)

```
Vazifa: com.finflow.transaction.enums package'ida enum'larni yarat.

1. Currency — UZS, USD, EUR, RUB. Har birida: kod, nomi, minor unit (scale).
2. TransactionType — TRANSFER, DEPOSIT, WITHDRAW, REFUND.
3. EntryType — DEBIT, CREDIT. Har birida `EntryType opposite()` metodi.
4. TransactionStatus — INITIATED, PENDING, COMPLETED, FAILED, CANCELLED, REFUNDED.
   MUHIM: status machine enum ICHIDA bo'lsin.
   - `Set<TransactionStatus> allowedTransitions()` yoki shunga o'xshash
   - `boolean canTransitionTo(TransactionStatus target)`
   - `boolean isTerminal()`
   Ruxsat etilgan o'tishlar:
     INITIATED  -> PENDING, FAILED
     PENDING    -> COMPLETED, FAILED, CANCELLED
     COMPLETED  -> REFUNDED
     FAILED, CANCELLED, REFUNDED -> terminal (hech qayerga)
   Enum konstruktorida boshqa enum konstantasiga murojaat qilib bo'lmasligini hisobga ol
   (EnumSet'ni lazy holder yoki switch orqali qil — qaysi yechim tanlaganingni izohla).
5. SagaStatus — STARTED, DEBITED, CREDITED, COMPLETED, COMPENSATING, COMPENSATED, FAILED.
6. SagaStep — DEBIT_SOURCE, CREDIT_TARGET, FINALIZE, COMPENSATE_REFUND.
7. OutboxStatus — NEW, PUBLISHED, FAILED.
8. FraudRuleCode — VELOCITY, DAILY_LIMIT, MONTHLY_LIMIT, NIGHT_LARGE_AMOUNT, NEW_DEVICE, ML_SCORE.
9. FraudDecisionType — ALLOW, REVIEW, BLOCK.
10. ScheduleFrequency — ONCE, DAILY, WEEKLY, MONTHLY.
11. ExportFormat — CSV, PDF.

Har bir enum uchun 1 gap izoh ber: nega shu qiymatlar.
Boshqa hech narsa yaratma.
```

---

## Bosqich 3 — Base entity'lar va Money value object

```
Vazifa: domain/base va domain/vo.

1. AbstractBaseEntity<ID extends Serializable>
   - @MappedSuperclass, @Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
   - id (generic), deleted (boolean, @Builder.Default false), version (@Version Long — optimistic lock)
   - soft delete uchun `isDeleted()`

2. AbstractAuditEntity<ID> extends AbstractBaseEntity<ID>
   - @EntityListeners(AuditingEntityListener.class)
   - createdAt, createdBy, updatedAt, updatedBy
   - Instant ishlat, LocalDateTime emas — sabab: microservicelar turli TZ'da.
     (Agar user-service LocalDateTime ishlatgan bo'lsa, menga ayt — muhokama qilamiz.)

3. Money — @Embeddable value object
   - BigDecimal amount (precision 19, scale 4), Currency currency
   - IMMUTABLE: faqat getter, setter yo'q
   - static factory: `Money.of(BigDecimal, Currency)`, `Money.zero(Currency)`
   - `add`, `subtract`, `multiply` — yangi Money qaytaradi
   - `isNegative()`, `isZero()`, `isGreaterThan(Money)`
   - Har bir amalda valyuta mosligini tekshir — mos kelmasa IllegalArgumentException
   - Barcha natijalar `setScale(4, HALF_EVEN)`
   - equals/hashCode: BigDecimal.compareTo orqali (equals emas! 10.00 != 10.0 muammosi)

4. HistoryEntity — user-service'dagi bilan bir xil, lekin schema `tx_sch` yoki `history`.

Money uchun to'liq test yozma, lekin equals/hashCode qarorini izohla.
```

---

## Bosqich 4 — Asosiy entity'lar

```
Vazifa: domain/ package'ida entity'lar. Schema: tx_sch.

1. TransactionEntity — aggregate root
   - PK: UUID (v7). Qanday generatsiya qilishni tanla va izohla
     (Hibernate 6 `@UuidGenerator(style = TIME)` bormi — tekshir).
   - reference: String, unique, TXN-YYYYMMDD-XXXXXX
   - type: TransactionType
   - status: TransactionStatus
   - @Embedded Money amount  (@AttributeOverrides bilan ustun nomlari)
   - sourceAccountId, targetAccountId (Long, nullable — deposit/withdraw uchun)
   - sourceUserId, targetUserId
   - idempotencyKey (String)
   - description, failureReason
   - initiatedAt, completedAt (Instant)
   - deviceId, ipAddress (fraud uchun)
   - @Version optimistic lock (base'dan keladi)

   MUHIM METOD: `void transitionTo(TransactionStatus target)` —
   ichida `canTransitionTo` tekshiradi, bo'lmasa InvalidStatusTransitionException.
   Status'ga setter YO'Q. Faqat shu metod orqali.

2. TransactionEntryEntity — double-entry ledger
   - PK Long
   - @ManyToOne(LAZY) TransactionEntity transaction
   - accountId, entryType (DEBIT/CREDIT), @Embedded Money amount
   - balanceAfter (Money) — snapshot
   - Immutable: yaratilgandan keyin o'zgarmaydi (updatable=false)

3. OutboxEventEntity
   - PK Long, aggregateType, aggregateId, eventType, payload (jsonb), headers
   - status: OutboxStatus, attempts, lastError, createdAt, publishedAt

4. ProcessedEventEntity (inbox)
   - PK Long, eventId (unique!), eventType, source, processedAt

5. IdempotencyRecordEntity
   - PK Long, userId, idempotencyKey, requestHash, responseBody, httpStatus, createdAt, expiresAt
   - UNIQUE(user_id, idempotency_key)

6. SagaStateEntity
   - PK Long, transactionId (UUID), status: SagaStatus, currentStep: SagaStep
   - payload (jsonb), retryCount, lastError, startedAt, updatedAt

7. ScheduledPaymentEntity
   - PK Long, userId, sourceAccountId, targetAccountId, @Embedded Money amount
   - frequency, nextExecutionAt, lastExecutionAt, executionCount, active, quartzJobKey

8. FraudCheckEntity
   - PK Long, transactionId (UUID), ruleCode, decision, score (BigDecimal), reason, checkedAt

Barchasi @EntityListeners(HistoryListener.class) — TransactionEntry va Outbox'dan tashqari
(sabab: hajm juda katta bo'ladi — rozimisan?).

Har bir entity'dan keyin to'xta va menga ko'rsat. Hammasi bir vaqtda emas.
```

---

## Bosqich 5 — DTO'lar

```
Vazifa: dto/ package'i.

dto/request/ — class, Lombok @Getter @Setter, Bean Validation:
- TransferRequest: sourceAccountId(@NotNull), targetAccountId(@NotNull),
  amount(@NotNull @DecimalMin("0.0001") @Digits(integer=15, fraction=4)),
  currency(@NotNull), description(@Size(max=255))
  + custom validator: source != target
- DepositRequest, WithdrawRequest
- RefundRequest: reason(@NotBlank)
- CancelRequest
- ScheduledPaymentRequest: frequency, startAt(@Future), endAt
- ExportRequest: format, dateFrom, dateTo

dto/filter/
- TransactionFilterRequest: status, type, currency, minAmount, maxAmount,
  dateFrom, dateTo, accountId

dto/response/ — hammasi Java `record` (immutable):
- TransactionResponse
- TransactionEntryResponse
- CursorPageResponse<T>(List<T> items, String nextCursor, boolean hasNext)
  static factory: CursorPageResponse.of(...)
- ScheduledPaymentResponse
- BalanceResponse
- FraudCheckResponse

dto/command/ — service qatlamining internal modeli (record):
- TransferCommand(userId, sourceAccountId, targetAccountId, Money amount,
  description, idempotencyKey, deviceId, ipAddress)
- Nega alohida? Controller DTO'si service'ga kirmaydi — service HTTP'dan mustaqil bo'lsin.

Muhim: response'da BigDecimal qanday serialize bo'lishi kerak? String sifatida
(JS'da precision yo'qoladi). Jackson sozlamasini taklif qil.
```

---

## Bosqich 6 — Mapper

```
Vazifa: mapper/ — MapStruct.

- TransactionMapper: Entity -> TransactionResponse, TransferRequest -> TransferCommand
- ScheduledPaymentMapper
- @Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
  (ERROR — chunki bank loyihasida "unutilgan maydon" jimgina null bo'lib qolmasin)
- Money -> String/BigDecimal mapping uchun @Named metodlar

build.gradle.kts ga mapstruct qo'shish kerak bo'lsa — avval menga ayt, o'zim qo'shaman.
```

---

## Umumiy maslahatlar (promt yozish texnikasi)

1. **Bir promt = bir bosqich.** "Hammasini yarat" desangiz, Claude yarim yodidan yozadi.
2. **"To'xta va ko'rsat"** deb yozing. Har 3-5 fayldan keyin ko'rib chiqing.
3. **"Nega shunday?" so'rang.** Senior sifatida o'sish shu yerda, kod nusxalashda emas.
4. **Noaniqlikni oldindan yoping.** "Agar X bo'yicha ishonching komil bo'lmasa — so'ra, taxmin qilma."
5. **`/clear`** — bosqichlar orasida kontekstni tozalang, `CLAUDE.md` baribir qoladi.
6. **Git commit har bosqichdan keyin.** Claude noto'g'ri yo'lga ketsa, `git reset` — 1 soniya.
7. **Refaktoring promti:** "Bu fayldagi eng zaif joyni top va nega zaif ekanini tushuntir.
   Tuzatishdan oldin ruxsat so'ra."
