package com.microservico.sales.services;

import com.microservico.sales.client.ProductClient;
import com.microservico.sales.exceptions.ResourceNotFoundException;
import com.microservico.sales.models.Sale;
import com.microservico.sales.models.dtos.ProductResponse;
import com.microservico.sales.models.dtos.SaleRequest;
import com.microservico.sales.models.dtos.SaleResponse;
import com.microservico.sales.models.mapper.SaleMapper;
import com.microservico.sales.repositories.SaleRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Validated
@Service
public class SaleService {

    private final SaleRepository repository;
    private final ProductClient productClient;


    public SaleService(SaleRepository repository, ProductClient productClient) {
        this.repository = repository;
        this.productClient = productClient;
    }


    public SaleResponse createSale(@Valid @NotNull SaleRequest saleRequest) {
        ProductResponse product = productClient.getProductById(saleRequest.getProductId());

        if (Objects.isNull(product)) {
            throw new ResourceNotFoundException(saleRequest.getProductId(), "Product not found in Product Service");
        }

        Sale sale = SaleMapper.toEntity(saleRequest);

        repository.save(Objects.requireNonNull(sale));

        return SaleMapper.toDto(sale);
    }

    public List<SaleResponse> listByUser(Long userId) {
        return repository.findByUserId(userId)
                .stream()
                .map(SaleMapper::toDto)
                .collect(Collectors.toList());
    }
}
