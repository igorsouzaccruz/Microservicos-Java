package com.microservico.product.controllers;

import com.microservico.product.controllers.interfaces.IProductController;
import com.microservico.product.models.dtos.ProductDTO;
import com.microservico.product.services.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/products")
public class ProductController implements IProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProductDTO> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public ProductDTO findById(@PathVariable(value = "id") @NotNull @Positive Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public ProductDTO create(@RequestBody @Valid @NotNull ProductDTO product) {
        return service.create(product);
    }

    @PutMapping("/{id}")
    public ProductDTO update(@PathVariable(value = "id") @NotNull @Positive Long id, @RequestBody @Valid @NotNull ProductDTO product) {
        return service.update(id, product);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @NotNull @Positive Long id) {
        service.delete(id);
    }
}
