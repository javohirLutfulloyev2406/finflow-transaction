package com.finflow.transaction.client.dto;

import java.util.Set;

public record UserStatus(
        Long id,
        boolean enabled,
        Set<String> roles
) {
}