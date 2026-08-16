package com.finflow.transaction.security;

import com.finflow.transaction.exception.ExceptionWithStatusCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * JwtAuthenticationFilter principal sifatida userId'ni (Long) qo'yadi —
 * bu yerda uni SecurityContext'dan qayta o'qiydi, service qatlami DB'ga
 * murojaat qilmasdan "kim so'ramoqda" degan savolga javob oladi.
 */
@Component
public class SecurityUtil {

    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
            throw new ExceptionWithStatusCode(401, "Foydalanuvchi autentifikatsiyadan o'tmagan");
        }
        return userId;
    }
}