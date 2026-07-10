package com.finflow.transaction.util;

import com.finflow.transaction.exception.ExceptionWithStatusCode;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/**
 * Cursor = base64("<epochMilli>:<uuid>").
 * (created_at, id) juftligi — created_at takrorlanishi mumkin, id esa unique.
 * Ikkalasi birga qat'iy tartib beradi.
 *
 * Opaque bo'lishi shart: client cursor ichini o'qiy olmasin, aks holda
 * uni "hack" qilishga urinadi va biz formatni hech qachon o'zgartira olmaymiz.
 */
public final class CursorCodec {

    private static final String SEPARATOR = ":";

    private CursorCodec() {
    }

    public static String encode(Instant createdAt, UUID id) {
        String raw = createdAt.toEpochMilli() + SEPARATOR + id;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static Cursor decode(String cursor) {
        try {
            String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = raw.split(SEPARATOR, 2);
            return new Cursor(Instant.ofEpochMilli(Long.parseLong(parts[0])), UUID.fromString(parts[1]));
        } catch (RuntimeException e) {
            throw new ExceptionWithStatusCode(400, "Malformed cursor");
        }
    }

    public record Cursor(Instant createdAt, UUID id) {
    }
}
