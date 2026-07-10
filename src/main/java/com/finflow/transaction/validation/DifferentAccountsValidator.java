package com.finflow.transaction.validation;

import com.finflow.transaction.dto.request.TransferRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Objects;

public class DifferentAccountsValidator implements ConstraintValidator<DifferentAccounts, TransferRequest> {

    @Override
    public boolean isValid(TransferRequest request, ConstraintValidatorContext context) {
        if (request == null || request.getSourceAccountId() == null || request.getTargetAccountId() == null) {
            // null holatini @NotNull hal qiladi — bu yerda ikki marta xato bermaymiz
            return true;
        }
        return !Objects.equals(request.getSourceAccountId(), request.getTargetAccountId());
    }
}
