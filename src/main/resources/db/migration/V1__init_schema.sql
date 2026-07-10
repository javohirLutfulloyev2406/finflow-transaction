-- FinFlow Transaction Service — schema init.
CREATE SCHEMA IF NOT EXISTS tx_sch;

-- UUID v7 Hibernate tomonida generatsiya qilinadi (@UuidGenerator style=TIME),
-- shuning uchun pgcrypto/uuid-ossp SHART EMAS.
-- Extension faqat DB tomonida default kerak bo'lsa qo'shiladi.
