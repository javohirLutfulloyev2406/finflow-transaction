package com.finflow.transaction.service;

import com.finflow.transaction.dto.request.ScheduledPaymentRequest;
import com.finflow.transaction.dto.response.ScheduledPaymentResponse;

import java.util.List;
public interface ScheduledPaymentService {

    ScheduledPaymentResponse create(
            Long userId,
            ScheduledPaymentRequest request
    );

    ScheduledPaymentResponse update(
            Long id,
            ScheduledPaymentRequest request
    );

    ScheduledPaymentResponse findById(Long id);

    List<ScheduledPaymentResponse> findByUser(Long userId);

    void activate(Long id);

    void deactivate(Long id);

    void delete(Long id);
}