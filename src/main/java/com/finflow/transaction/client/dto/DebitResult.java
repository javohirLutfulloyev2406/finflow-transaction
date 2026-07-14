package com.finflow.transaction.client.dto;

public record DebitResult(

        boolean success,

        String operationId,

        String message
) {
}
