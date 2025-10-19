package com.microservico.sales.services;

import com.microservico.sales.models.mapper.SaleMapper;
import com.microservico.sales.repositories.SaleRepository;
import org.springframework.stereotype.Service;

@Service
public class SaleService {

    private final SaleRepository repository;
    private final SaleMapper mapper;

    public SaleService(SaleRepository repository, SaleMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }
}
