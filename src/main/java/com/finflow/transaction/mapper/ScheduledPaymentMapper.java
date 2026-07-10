package com.finflow.transaction.mapper;

import com.finflow.transaction.domain.ScheduledPaymentEntity;
import com.finflow.transaction.dto.response.ScheduledPaymentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ScheduledPaymentMapper {

    @Mapping(target = "amount", source = "amount.amount")
    @Mapping(target = "currency", source = "amount.currency")
    ScheduledPaymentResponse toResponse(ScheduledPaymentEntity entity);

    List<ScheduledPaymentResponse> toResponseList(List<ScheduledPaymentEntity> entities);
}
