package com.finflow.transaction.idempotency;

import java.util.Optional;

/**
 * Interface — Redis va DB implementatsiyalari alohida, testda fake qo'yish mumkin.
 *
 * Qaytarish qoidasi:
 *   empty   → lock olindi, operatsiyani davom ettir
 *   present → saqlangan javob (JSON: {"status":N,"body":"..."}), clientga qaytar
 *
 * Throw qoidasi:
 *   409 → IN_PROGRESS: birinchi so'rov hali tugamagan, ikkinchisi kutsin
 *   422 → bir xil kalit, boshqa payload — client xatosi, jimgina yashirilmaydi
 */
public interface IdempotencyStore {

    Optional<String> tryAcquire(Long userId, String key, String requestHash);

    void complete(Long userId, String key, String requestHash, int httpStatus, String responseBody);
}
