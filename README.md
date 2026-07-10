# finflow-transaction

FinFlow Personal Finance Management System — Transaction Service.
Money transfer, transaction history, fraud detection.

## Ishga tushirish

```bash
# 1. Infratuzilma
docker compose up -d

# 2. Postgres tayyor bo'lishini kut (~5 soniya), keyin:
./gradlew bootRun
```

Gradle wrapper JAR repo'da yo'q. Birinchi marta:

```bash
gradle wrapper --gradle-version 8.12
```

yoki IntelliJ'da loyihani ochib, Gradle sync bosing — IntelliJ o'zi yuklaydi.

## Tekshirish

```bash
curl http://localhost:8083/actuator/health
open http://localhost:8083/swagger-ui.html
```

Startup muvaffaqiyatli bo'lsa:
- Flyway V1..V9 migration'larni qo'llaydi
- Hibernate `ddl-auto=validate` — entity'lar schema'ga mos ekanini tasdiqlaydi
- Agar mos kelmasa, application START BO'LMAYDI. Bu ataylab.

## Hozirgi holat

| Qatlam | Holat |
|---|---|
| Package skeleton | ✅ |
| Enums (status machine) | ✅ |
| Base entity + Money VO | ✅ |
| Domain entities (8 ta) | ✅ |
| DTO (request/response/command/filter) | ✅ |
| MapStruct mappers | ✅ |
| Flyway migrations V1–V9 | ✅ |
| Repository | ⬜ keyingi |
| Service (command/query) | ⬜ |
| Idempotency (Redis + DB) | ⬜ |
| Outbox poller | ⬜ |
| Saga choreography | ⬜ |
| Fraud engine | ⬜ |
| Controller + Security | ⬜ |
| gRPC client (account-service) | ⬜ |
| Quartz scheduled payments | ⬜ |

## Java versiyasi

`build.gradle.kts` da toolchain **21** ga qo'yilgan (kafolatlangan Boot 3.5 mosligi).
TZ Java 25 talab qiladi. O'tish:

```kotlin
languageVersion = JavaLanguageVersion.of(25)
```

va `./gradlew clean build`. Lombok + MapStruct annotation processor'lari
yangi JDK'da muammo bersa, avval ularning versiyasini ko'taring.
