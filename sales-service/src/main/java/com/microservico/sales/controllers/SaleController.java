package com.microservico.sales.controllers;

import com.microservico.sales.controllers.interfaces.ISaleControllerDocs;
import com.microservico.sales.models.dtos.SaleRequest;
import com.microservico.sales.models.dtos.SaleResponse;
import com.microservico.sales.services.SaleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sales")
public class SaleController implements ISaleControllerDocs {
    private final SaleService service;

    public SaleController(SaleService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SaleResponse create(
            @Valid @RequestBody SaleRequest request,
            @NotNull @Positive @RequestHeader(value = "X-User-Id", required = true) String userIdHeader
    ) {
        request.setUserId(Long.parseLong(userIdHeader));
        return service.createSale(request);
    }

    @GetMapping("/user/{userId}")
    public List<SaleResponse> listByUser(@PathVariable Long userId) {
        return service.listByUser(userId);
    }
}
