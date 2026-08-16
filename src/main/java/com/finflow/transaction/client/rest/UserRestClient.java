package com.finflow.transaction.client.rest;

import com.finflow.transaction.client.UserClient;
import com.finflow.transaction.client.dto.UserStatus;
import com.finflow.transaction.client.rest.dto.UserServiceUserResponse;
import com.finflow.transaction.exception.UserServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * DIQQAT — user-service'dagi GET /api/v1/admin/users/{id} endpointi
 * {@code @PreAuthorize("hasAuthority('USER:READ')")} bilan himoyalangan. Oddiy
 * end-user tokenini forward qilish (AuthHeaderPropagationInterceptor) YETARLI EMAS —
 * bu chaqiruv faqat so'rovchi tokenida USER:READ authority bo'lsa ishlaydi (masalan
 * fraud/admin oqimida). Oddiy foydalanuvchi o'zi haqida ma'lumot uchun buni emas,
 * user-service'ning /api/v1/users/me endpointini chaqirishi kerak — bu klass
 * transactionId ichidagi boshqa userId'larni (masalan target user) tekshirish uchun.
 * TODO(javohir): USER:READ'ga ega bo'lmagan oqimlar uchun kontrakt kelishilishi kerak.
 */
@Component
@RequiredArgsConstructor
public class UserRestClient implements UserClient {

    private final RestClient userServiceRestClient;

    @Override
    public UserStatus getUserStatus(Long userId) {
        try {
            // Read-only, side-effect'siz — retry xavfsiz (RetrySupport javadoc'i).
            return RetrySupport.withRetry("getUserStatus", 3, 200, () -> {
                UserServiceUserResponse response = userServiceRestClient.get()
                        .uri("/api/v1/admin/users/{id}", userId)
                        .retrieve()
                        .body(UserServiceUserResponse.class);

                return new UserStatus(response.id(), response.enabled(), response.roles());
            });
        } catch (HttpClientErrorException.Forbidden | HttpClientErrorException.Unauthorized e) {
            // Forwarded token USER:READ'ga ega emas — user-service'ning o'zi rad etdi,
            // "unavailable" emas, aniq ruxsat xatosi. TODO(javohir): aniq exception turi
            // (masalan ForbiddenByUserServiceException) kontrakt kelishilgach qo'shiladi.
            throw new UserServiceUnavailableException();
        } catch (HttpServerErrorException | ResourceAccessException e) {
            throw new UserServiceUnavailableException();
        }
    }
}