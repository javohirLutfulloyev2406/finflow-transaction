package com.finflow.transaction.dto.response;

import java.util.List;

/**
 * Cursor-based pagination. OFFSET ishlatilmaydi:
 * 1) katta offset'da PostgreSQL baribir barcha satrni skanerlaydi
 * 2) yangi tranzaksiya qo'shilsa, sahifalar siljib, satr takrorlanadi yoki yo'qoladi
 * Cursor = base64((created_at, id)) — barqaror, tez.
 */
public record CursorPageResponse<T>(
        List<T> items,
        String nextCursor,
        boolean hasNext
) {
    public static <T> CursorPageResponse<T> of(List<T> items, String nextCursor, boolean hasNext) {
        return new CursorPageResponse<>(items, nextCursor, hasNext);
    }

    public static <T> CursorPageResponse<T> empty() {
        return new CursorPageResponse<>(List.of(), null, false);
    }
}
