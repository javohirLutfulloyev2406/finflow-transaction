package com.finflow.transaction.dto.request;

import com.finflow.transaction.enums.ExportFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExportRequest {

    @NotNull
    private ExportFormat format;

    @NotNull
    private Instant dateFrom;

    @NotNull
    private Instant dateTo;
}
