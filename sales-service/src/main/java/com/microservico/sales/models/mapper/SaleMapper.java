package com.microservico.sales.models.mapper;

import com.microservico.sales.models.Sale;
import com.microservico.sales.models.dtos.SaleRequest;
import com.microservico.sales.models.dtos.SaleResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

@Component
public class SaleMapper {


    public static Sale toEntity(SaleRequest request) {
        if (Objects.isNull(request))
            return null;

        Sale sale = new Sale();

        if (Objects.nonNull(request.getProductId())) {
            sale.setProductId(request.getProductId());
        }

        if (Objects.nonNull(request.getUserId())) {
            sale.setUserId(request.getUserId());
        }

        sale.setQuantity(request.getQuantity());
        return sale;
    }

    public static SaleResponse toDto(Sale sale) {
        if (Objects.isNull(sale))
            return null;

        return new SaleResponse(
                sale.getId(),
                sale.getProductId(),
                sale.getUserId(),
                sale.getQuantity(),
                sale.getSaleDate());
    }
}
