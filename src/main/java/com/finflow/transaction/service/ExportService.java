package com.finflow.transaction.service;

import com.finflow.transaction.dto.request.ExportRequest;

public interface ExportService {

    byte[] export(
            Long userId,
            ExportRequest request
    );

}