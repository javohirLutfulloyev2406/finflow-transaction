package com.finflow.transaction.client.dto;

public record AccountOperationResult(

        boolean success,

        String operationId,

        String message
) {
}