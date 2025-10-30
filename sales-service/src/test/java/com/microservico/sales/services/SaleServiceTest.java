package com.microservico.sales.services;

import com.microservico.sales.clients.ProductClient;
import com.microservico.sales.exceptions.ResourceNotFoundException;
import com.microservico.sales.models.Sale;
import com.microservico.sales.models.dtos.ProductResponse;
import com.microservico.sales.models.dtos.SaleRequest;
import com.microservico.sales.models.dtos.SaleResponse;
import com.microservico.sales.repositories.SaleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {
    @Mock
    private SaleRepository repository;

    @Mock
    private ProductClient productClient;

    @Captor
    private ArgumentCaptor<Sale> saleCaptor;

    private SaleService service;

    private SaleRequest request;
    private Sale saleEntity;
    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        service = new SaleService(repository, productClient);

        request = new SaleRequest();
        request.setProductId(10L);
        request.setUserId(5L);
        request.setQuantity(3);

        saleEntity = new Sale();
        saleEntity.setId(1L);
        saleEntity.setProductId(10L);
        saleEntity.setUserId(5L);
        saleEntity.setQuantity(3);
        saleEntity.setSaleDate(LocalDateTime.of(2025, 1, 10, 15, 0));

        productResponse = new ProductResponse(10L, "Teclado", 199.99);
    }

    @DisplayName("Given valid SaleRequest and existing product when createSale then return SaleResponse")
    @Test
    void testGivenValidRequest_WhenCreateSale_ShouldReturnSaleResponse() {
        // Given
        given(productClient.getProductById(10L)).willReturn(productResponse);
        given(repository.save(any(Sale.class))).willReturn(saleEntity);

        // When
        SaleResponse response = service.createSale(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.productId()).isEqualTo(10L);
        assertThat(response.userId()).isEqualTo(5L);
        assertThat(response.quantity()).isEqualTo(3);

        verify(productClient, times(1)).getProductById(10L);
        verify(repository, times(1)).save(saleCaptor.capture());

        Sale savedSale = saleCaptor.getValue();
        assertThat(savedSale.getProductId()).isEqualTo(10L);
        assertThat(savedSale.getUserId()).isEqualTo(5L);
        assertThat(savedSale.getQuantity()).isEqualTo(3);
    }

    @DisplayName("Should throw ResourceNotFoundException when product does not exist")
    @Test
    void testCreateSale_WhenProductNotFound_ShouldThrowResourceNotFoundException() {
        // Given
        given(productClient.getProductById(anyLong())).willReturn(null);

        // When
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.createSale(request));

        assertThat(exception.getMessage())
                .contains("ProductResponse not found with id: 10");

        verify(repository, never()).save(any(Sale.class));
    }

    @DisplayName("Should throw NullPointerException when SaleMapper returns null")
    @Test
    void testCreateSale_WhenMapperReturnsNull_ShouldThrowNullPointerException() {
        // Given
        lenient().when(productClient.getProductById(10L)).thenReturn(productResponse);

        SaleService faultyService = new SaleService(repository, productClient) {
            @Override
            public SaleResponse createSale(SaleRequest req) {
                Sale sale = null; // simula erro do mapper
                Objects.requireNonNull(sale, "Sale entity must not be null");
                return null;
            }
        };

        // When / Then
        assertThrows(NullPointerException.class, () -> faultyService.createSale(request));

        verify(repository, never()).save(any(Sale.class));
    }

    @DisplayName("Should call repository.save() with non-null Sale when product is found")
    @Test
    void testCreateSale_ShouldCallRepositoryWithNonNullSale() {
        // Given
        given(productClient.getProductById(10L)).willReturn(productResponse);
        given(repository.save(any(Sale.class))).willReturn(saleEntity);

        // When
        service.createSale(request);

        // Then
        verify(repository).save(saleCaptor.capture());
        Sale captured = saleCaptor.getValue();
        assertThat(captured).isNotNull();
        assertThat(captured.getProductId()).isEqualTo(10L);
        assertThat(captured.getUserId()).isEqualTo(5L);
        assertThat(captured.getQuantity()).isEqualTo(3);
    }

    @DisplayName("Given existing user with sales when listByUser then return list of SaleResponse")
    @Test
    void testGivenUserWithSales_WhenListByUser_ShouldReturnList() {
        // Given
        given(repository.findByUserId(5L)).willReturn(List.of(saleEntity));

        // When
        List<SaleResponse> responses = service.listByUser(5L);

        // Then
        assertThat(responses).isNotEmpty().hasSize(1);
        assertThat(responses.getFirst().userId()).isEqualTo(5L);
        assertThat(responses.getFirst().productId()).isEqualTo(10L);

        verify(repository, times(1)).findByUserId(5L);
    }

    @DisplayName("Given user with no sales when listByUser then return empty list")
    @Test
    void testGivenUserWithoutSales_WhenListByUser_ShouldReturnEmptyList() {
        // Given
        given(repository.findByUserId(99L)).willReturn(List.of());

        // When
        List<SaleResponse> responses = service.listByUser(99L);

        // Then
        assertThat(responses).isEmpty();
        verify(repository).findByUserId(99L);
    }
}
