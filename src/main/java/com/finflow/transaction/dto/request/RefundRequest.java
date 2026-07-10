package com.finflow.transaction.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {

    /** Refund sababisiz bo'lmaydi — audit talabi. */
    @NotBlank(message = "reason is required")
    @Size(max = 512)
    private String reason;
}
