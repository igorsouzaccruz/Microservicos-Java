package com.microservico.sales.services;

import com.microservico.sales.client.ProductClient;
import com.microservico.sales.models.Sale;
import com.microservico.sales.models.dtos.ProductResponse;
import com.microservico.sales.models.dtos.SaleRequest;
import com.microservico.sales.models.dtos.SaleResponse;
import com.microservico.sales.models.mapper.SaleMapper;
import com.microservico.sales.repositories.SaleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SaleService {

    private final SaleRepository repository;
    private final ProductClient productClient;


    public SaleService(SaleRepository repository, ProductClient productClient) {
        this.repository = repository;
        this.productClient = productClient;
    }


    public SaleResponse createSale(SaleRequest request) {
        ProductResponse product = productClient.getProductById(request.getProductId());
        if (product == null) {
            throw new RuntimeException("Produto não encontrado no Product Service");
        }

        Sale entity = SaleMapper.toEntity(request);
        repository.save(entity);

        return SaleMapper.toDto(entity);
    }

    public List<SaleResponse> listByUser(Long userId) {
        return repository.findByUserId(userId)
                .stream()
                .map(SaleMapper::toDto)
                .collect(Collectors.toList());
    }
}
