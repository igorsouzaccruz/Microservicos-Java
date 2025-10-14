package com.microservico.product.services;

import com.microservico.product.exceptions.RecordNotFoundException;
import com.microservico.product.models.Product;
import com.microservico.product.models.dtos.ProductDTO;
import com.microservico.product.models.mappers.ProductMapper;
import com.microservico.product.repositories.ProductRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@Service
public class ProductService {

    private final ProductRepository repository;

    private final ProductMapper productMapper;

    public ProductService(ProductRepository repository, ProductMapper productMapper) {
        this.repository = repository;
        this.productMapper = productMapper;
    }

    public List<ProductDTO> list() {
        return repository.findAll().stream().map(productMapper::toDTO).toList();
    }

    public ProductDTO findById(@NotNull @Positive Long id) {
        return repository.findById(id)
                .map(productMapper::toDTO)
                .orElseThrow(() -> new RecordNotFoundException(id, Product.class.getSimpleName()));
    }

    public ProductDTO create(@Valid @NotNull ProductDTO productDTO) {
        return productMapper.toDTO(
                repository.save(productMapper.toEntity(productDTO))
        );
    }

    public ProductDTO update(@NotNull @Positive Long id, @Valid @NotNull ProductDTO productDTO) {
        return repository.findById(id)
                .map(recordFound -> {

                    Product product = productMapper.toEntity(productDTO);
                    recordFound.setDescription(product.getDescription());
                    recordFound.setCategory(product.getCategory());
                    recordFound.setPrice(product.getPrice());

                    return productMapper.toDTO(repository.save(recordFound));
                }).orElseThrow(() -> new RecordNotFoundException(id, Product.class.getSimpleName()));
    }

    public void delete(@NotNull @Positive Long id) {
        repository.delete(repository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException(id, Product.class.getSimpleName())));
    }

}
