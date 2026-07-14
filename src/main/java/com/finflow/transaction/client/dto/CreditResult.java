package com.finflow.transaction.client.dto;

public record CreditResult(

        boolean success,

        String operationId,

        String message
) {
}