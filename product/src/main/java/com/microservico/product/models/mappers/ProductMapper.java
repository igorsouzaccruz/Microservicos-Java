package com.microservico.product.models.mappers;

import com.microservico.product.models.Product;
import com.microservico.product.models.dtos.ProductDTO;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ProductMapper {

    public ProductDTO toDTO(Product product) {

        if (Objects.isNull(product))
            return null;

        return new ProductDTO(
                product.getId(),
                product.getDescription(),
                product.getCategory(),
                product.getPrice());
    }

    public Product toEntity(ProductDTO dto) {

        if (Objects.isNull(dto))
            return null;

        Product product = new Product();

        if (Objects.nonNull(dto.id())) {
            product.setId(dto.id());
        }

        product.setDescription(dto.description());
        product.setCategory(dto.category());
        product.setPrice(dto.price());

        return product;
    }
}
