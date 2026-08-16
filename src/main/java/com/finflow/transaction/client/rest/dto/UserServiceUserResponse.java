package com.finflow.transaction.client.rest.dto;

import java.util.Set;

/** user-service GET /api/v1/admin/users/{id} javobi (UserResponse bilan bir xil maydonlar). */
public record UserServiceUserResponse(
        Long id,
        String fullName,
        String username,
        String email,
        String phoneNumber,
        boolean enabled,
        Set<String> roles,
        Set<String> authorities
) {
}