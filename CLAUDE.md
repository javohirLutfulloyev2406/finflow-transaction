# CLAUDE.md — finflow-transaction

> Bu fayl loyiha root'ida turadi. Claude Code har sessiyada avtomatik o'qiydi.
> Bu yerdagi qoidalar — **majburiy**. Har bir promtda takrorlash shart emas.

---

## 1. Loyiha haqida

**FinFlow** — Personal Finance Management System, microservice arxitekturasi.
Bu repo: `finflow-transaction` — pul o'tkazish, tranzaksiya tarixi, fraud detection.
Butun tizimdagi eng murakkab service: **Saga**, **Outbox/Inbox**, **Idempotency**, **CQRS**.

Bu **bank loyihasi**. Pul yo'qolishi yoki ikki marta o'tishi — qabul qilinmaydigan xato.
Har bir qaror shu prizmadan o'tadi.

### Sherik servicelar
- `finflow-user` — auth, JWT, roles/permissions
- `finflow-account` — hisob, balans, REST client: `debit` / `credit` / `getBalance` (`AccountClient` → `client/rest/AccountRestClient`)
- `finflow-gateway`, `finflow-notification`, `finflow-audit`

---

## 2. Texnologiyalar

| Komponent | Versiya / Tanlov |
|---|---|
| Java | 25 (LTS) — Records, Pattern Matching, Virtual Threads |
| Spring Boot | 3.5.x |
| Build | Gradle 8.x, **Kotlin DSL** |
| DB | PostgreSQL 17, schema: `tx_sch` |
| Migration | Flyway (majburiy, `ddl-auto: validate`) |
| Cache/Lock | Redis 7.4 (Lettuce) |
| Broker | Kafka + Schema Registry |
| Internal RPC | REST client — `RestClient` (account-service bilan). gRPC EMAS |
| Scheduler | Quartz (clustered, JDBC job store) |
| Mapping | MapStruct (qo'lda mapper yozilmaydi) |
| Boilerplate | Lombok |
| Docs | springdoc-openapi 3.1 |
| Test | JUnit 5, Mockito, Testcontainers, ArchUnit |

---

## 3. Package konvensiyasi

Base package: `com.finflow.transaction`

```
config/          config/listener/
controller/      controller/admin/
domain/          domain/base/    domain/vo/
enums/
dto/             dto/request/  dto/response/  dto/filter/  dto/specification/  dto/command/
mapper/
repository/
service/         service/impl/
idempotency/
saga/            saga/step/   saga/compensation/
outbox/
messaging/       messaging/event/  messaging/event/incoming/  messaging/producer/  messaging/consumer/
fraud/           fraud/rule/
client/          client/rest/  client/fallback/
scheduler/       scheduler/job/
security/
exception/
validation/
util/
```

**Bog'liqlik yo'nalishi (ArchUnit tekshiradi):**
`controller → service → repository → domain`
`domain` hech kimga bog'liq emas. `controller` hech qachon `repository`'ga to'g'ridan-to'g'ri murojaat qilmaydi.

---

## 4. Kod uslubi — `finflow-user` bilan bir xil bo'lishi shart

Bu qoidalar user-service'dagi mavjud koddan olingan. Chetga chiqilmaydi.

### Entity
- Nomlanish: `XxxEntity` (masalan `TransactionEntity`, `OutboxEventEntity`)
- Barchasi `AbstractAuditEntity<ID>` dan meros oladi
- Annotatsiyalar: `@Entity @Table(name="...", schema="tx_sch") @Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor`
- `implements Serializable` + `private static final long serialVersionUID = 1L;`
- Har bir `@Column` da `name`, kerak bo'lsa `nullable`, `updatable`, `length`
- `@UniqueConstraint` va `@ForeignKey` **nomlanadi**: `uq_...`, `fk_...`
- Collection'lar: `@Builder.Default private Set<X> field = new LinkedHashSet<>();`
- Enum'lar: `@Enumerated(EnumType.STRING)` — **hech qachon ORDINAL emas**
- `FetchType.LAZY` default, `EAGER` yozilmaydi
- Audit trail kerak bo'lsa: `@EntityListeners(HistoryListener.class)`

### DTO
- Request: `XxxRequest` — `dto/request/`
- Response: `XxxResponse` — `dto/response/`, **Java `record`** (immutable)
- Har bir Request'da Bean Validation: `@NotNull`, `@NotBlank`, `@DecimalMin`, `@Digits`
- DTO ichida entity **hech qachon** bo'lmaydi
- Entity ↔ DTO: faqat MapStruct mapper orqali

### Service
- Interface + `impl/` da implementatsiya (`UserService` / `UserServiceImpl` uslubi)
- `@Slf4j @Service @RequiredArgsConstructor`
- Faqat `final` field'lar, constructor injection. `@Autowired` field'ga qo'yilmaydi
- `@Transactional` — yozish metodlarida; `@Transactional(readOnly = true)` — o'qishda
- `org.springframework.transaction.annotation.Transactional` import qilinadi

### Controller
- `@RestController @RequiredArgsConstructor`
- `ResponseEntity<T>` qaytaradi
- Security: `@PreAuthorize("hasAuthority('TRANSACTION:CREATE')")` — permission-based
  (`ROLE_` emas, `RESOURCE:ACTION` formatda — user-service'dagi `PermissionAction` enum bilan mos)
- Biznes mantiq controller'da yo'q. Faqat delegatsiya

### Exception
- `ExceptionWithStatusCode(int status, String message)` — user-service'dan bir xil
- Domen xatolari: `InsufficientFundsException`, `InvalidStatusTransitionException`, ...
- `GlobalExceptionHandler` → RFC 7807 `ProblemDetail`

---

## 5. Domen qoidalari (buzilmaydi)

1. **Money** — har doim `BigDecimal`, `precision=19, scale=4`, `RoundingMode.HALF_EVEN`.
   `double`/`float` pul uchun **taqiqlangan**. `Money` — `@Embeddable` value object (amount + currency).

2. **Double-entry** — har bir tranzaksiya kamida 2 ta `TransactionEntryEntity` yaratadi
   (bitta DEBIT, bitta CREDIT). `SUM(debit) == SUM(credit)` doim.

3. **Status machine** — `TransactionStatus` enum **o'zi** biladi qaysi holatga o'tish mumkinligini:
   `INITIATED → PENDING → COMPLETED | FAILED | CANCELLED | REFUNDED`
   `canTransitionTo(TransactionStatus)` metodi enum ichida. `if (status == X)` service'da tarqamaydi.

4. **Idempotency ikki qatlamli** — Redis (SETNX, TTL 24h) + DB `UNIQUE(user_id, idempotency_key)`.
   Redis o'chsa ham pul ikki marta ketmaydi. DB — haqiqiy manba.

5. **Outbox + Inbox** — event publish `@Transactional` ichida `outbox_events` jadvaliga yoziladi.
   Consume tarafda `processed_events` (inbox) bilan dedup. At-least-once → effectively-once.

6. **Soft delete** — moliyaviy yozuvlar hech qachon hard delete qilinmaydi.

7. **PK strategiyasi** — `TransactionEntity` PK = **UUID v7** (vaqt bo'yicha tartiblangan).
   Sabab: sequential `Long` ID tashqariga chiqsa, kunlik hajm oshkor bo'ladi.
   Yonida odam o'qiydigan `reference` (masalan `TXN-20260709-A7F3K2`).
   Ichki texnik jadvallar (outbox, inbox, saga_state) — `Long` sequence PK, bu yetarli.

8. **Cursor pagination** — `(created_at, id)` juftligi, base64 kodlangan cursor. `OFFSET` ishlatilmaydi.

---

## 6. Claude Code uchun ish qoidalari

- **Bir vaqtda bitta bosqich.** Men aytmagan fayllarni yaratma.
- Har bir fayl yaratilgandan keyin — **qisqa izoh**: nima uchun shunday qaror qabul qilinganini 1-2 gapda ayt.
- Kod ichida **izoh (comment) faqat "nega" uchun**, "nima qilyapti" uchun emas.
  Yomon: `// user'ni topadi`  Yaxshi: `// SKIP LOCKED: bir nechta poller instance parallel ishlaydi`
- Noaniqlik bo'lsa — **taxmin qilma, so'ra.** Ayniqsa account-service kontrakti bo'yicha.
- `TODO` qoldirsang, formatda: `// TODO(javohir): ...`
- Test yozishni so'ramagunimcha yozma.
- `build.gradle.kts` ni o'zgartirishdan oldin menga ayt.
- Har bosqich oxirida `./gradlew compileJava` ishlashiga ishonch hosil qil.

---

## 7. Hal qilingan va hozircha ochiq bandlar

### Tasdiqlangan (2026-08-16)

- **`user_id` tipi — `Long`.** Uchala servisning haqiqiy kodi tekshirilib tasdiqlandi:
  `finflow-user-service` (`UserEntity extends AbstractAuditEntity<Long>`), `account-service`
  (`Account.userId: Long`), `finflow-transaction` (`TransactionEntity.sourceUserId/targetUserId: Long`).
  Bundan buyon taxmin emas, qaror sifatida ishlatiladi.
- **account-service bilan aloqa — REST client, gRPC EMAS.** `client/rest/AccountRestClient`
  (`org.springframework.web.client.RestClient`) orqali amalga oshirilgan, real account-service
  endpointlariga mos: `GET /api/v1/accounts/{id}`, `POST /api/v1/accounts/deposit/{id}`,
  `POST /api/v1/accounts/withdraw/{id}`. gRPC proto fayli bandi shu sabab olib tashlandi — kerak emas.

### Hal qilingan (2026-08-17)

- **Auth propagation.** `AccountRestClientConfig`'ga `RestClient` request interceptor qo'shildi —
  kiruvchi HTTP so'rovning `Authorization` header'i (`RequestContextHolder` orqali) account-service'ga
  chaqiruvda avtomatik forward qilinadi. Quartz job / Kafka consumer thread'ida so'rov konteksti
  yo'q — bunday holatda hozircha token forward qilinmaydi (quyidagi ochiq band).

### Hali ochiq

- **Currency mismatch (RUB).** transaction-service: `UZS, USD, EUR, RUB`. account-service:
  faqat `UZS, USD, EUR` — `RUB` yo'q. account-service'ga `RUB` qo'shilmaguncha, RUB bilan
  deposit/withdraw so'rovi account-service'da `Currency.valueOf("RUB")` xatosi bilan qulaydi.
- **Service-to-service auth (Quartz/Kafka konteksti).** Auth propagation faqat HTTP so'rov
  ichida ishlaydi (asl foydalanuvchi tokeni bor joyda). ScheduledPaymentJob yoki Kafka consumer
  orqali chaqirilgan deposit/withdraw'da HTTP konteksti yo'q — bu holat uchun alohida
  service-to-service token strategiyasi kelishilishi kerak.
- **Xato kontrakti strukturasiz.** account-service xatolarni turli status kod bilan qaytaradi
  (404/403/401/400 — `ex.getCode()`ga qarab), lekin body har doim oddiy matn (`ex.getMessage()`),
  JSON emas. Shu sabab transaction-service `INSUFFICIENT_FUNDS`ni boshqa 400 xatolardan
  ajrata olmaydi (`TransactionServiceImpl.withdraw()`dagi TODO shu haqida).
- **`operationId` yo'q.** account-service'ning `BalanceResponse`'ida audit ID chiqmaydi
  (`BalanceAudit.id` bor, javobga qo'shilmagan) — `AccountOperationResult.operationId()` doim null.
- **Refund summasi.** Qaror qilindi: refund har doim **to'liq** (original tranzaksiya summasi bo'yicha,
  qisman qaytarish yo'q). `RefundCommand`da shuning uchun `amount` maydoni yo'q — service
  implementatsiyasi original tranzaksiyani topib, undan summani olishi kerak.
- **TRANSACTION:CANCEL / TRANSACTION:REFUND permission kodi yo'q.** user-service'da faqat
  `TRANSACTION:READ` va `TRANSACTION:CREATE` bor. Hozircha cancel/refund endpointlari faqat
  `isAuthenticated()` bilan himoyalangan (permission tekshiruvisiz) — kod qo'shilsa `@PreAuthorize`
  ham yangilanishi kerak.
