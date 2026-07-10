package com.finflow.transaction.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/** O'ziga o'zi pul o'tkazish — mantiqsiz, DTO darajasida to'xtatiladi. */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DifferentAccountsValidator.class)
public @interface DifferentAccounts {

    String message() default "sourceAccountId and targetAccountId must be different";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
