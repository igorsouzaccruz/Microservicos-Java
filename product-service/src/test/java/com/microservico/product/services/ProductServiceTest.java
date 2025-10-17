package com.microservico.product.services;

import com.microservico.product.exceptions.ResourceNotFoundException;
import com.microservico.product.models.Product;
import com.microservico.product.models.dtos.ProductDTO;
import com.microservico.product.models.enums.CategoryEnum;
import com.microservico.product.models.mappers.ProductMapper;
import com.microservico.product.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    private ProductMapper mapper = new ProductMapper();

    private ProductService service;

    @Captor
    private ArgumentCaptor<Product> productArgumentCaptor;

    private ProductDTO productDTO;
    private Product productEntity;

    @BeforeEach
    void setUp() {
        service = new ProductService(repository, mapper);

        productDTO = new ProductDTO(
                null,
                "Some product",
                CategoryEnum.PERIPHERALS,
                new BigDecimal("1.0"));

        productEntity = mapper.toEntity(productDTO);
        productEntity.setId(1L);
    }

    @DisplayName("Given Product Object when Save Product then Return Product Object")
    @Test
    void testGivenProductObject_WhenSaveProduct_thenReturnPersonObject() {
        // Given / Arrange
        given(repository.save(any(Product.class))).willReturn(productEntity);

        // When / Act
        ProductDTO savedProductDTO = service.create(productDTO);

        // Then / Assert
        assertNotNull(savedProductDTO);
        assertEquals(1L, savedProductDTO.id());
        assertEquals("Some product", savedProductDTO.description());
        verify(repository).save(any(Product.class));
    }

    @DisplayName("Given Existing ID and Valid DTO when Update then Return Updated DTO")
    @Test
    void testGivenExistingId_WhenUpdate_thenReturnUpdatedDTO() {
        // Given / Arrange
        final Long existingId = 1L;
        Product existingProduct = new Product();
        existingProduct.setId(existingId);
        existingProduct.setDescription("Old Description");
        existingProduct.setCategory(CategoryEnum.PERIPHERALS);
        existingProduct.setPrice(new BigDecimal("50.00"));

        ProductDTO updateRequestDTO = new ProductDTO(
                null,
                "New Description",
                CategoryEnum.ACCESSORIES,
                new BigDecimal("99.99"));

        given(repository.findById(existingId)).willReturn(Optional.of(existingProduct));
        given(repository.save(any(Product.class))).willAnswer(invocation -> invocation.getArgument(0));

        // When / Act
        ProductDTO resultDTO = service.update(existingId, updateRequestDTO);

        // Then / Assert
        assertNotNull(resultDTO);
        assertEquals("New Description", resultDTO.description());
        assertEquals(new BigDecimal("99.99"), resultDTO.price());

        verify(repository).findById(existingId);

        verify(repository).save(productArgumentCaptor.capture());
        Product savedProduct = productArgumentCaptor.getValue();

        assertEquals(existingId, savedProduct.getId());
        assertEquals("New Description", savedProduct.getDescription());
    }

    @DisplayName("Unit Test: Given Non-Existing ID when Update then Throws RecordNotFoundException")
    @Test
    void testGivenNonExistingId_WhenUpdate_thenThrowsRecordNotFoundException() {
        // Given / Arrange
        final Long nonExistingId = 99L;
        ProductDTO anyDTO = new ProductDTO(null, "Any Data", CategoryEnum.COMPUTERS, BigDecimal.TEN);

        given(repository.findById(nonExistingId)).willReturn(Optional.empty());

        // When / Act & Then / Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            service.update(nonExistingId, anyDTO);
        });

        verify(repository, never()).save(any(Product.class));
    }


    @DisplayName("Given existing ID when Find By Id then Return ProductDTO")
    @Test
    void testGivenExistingId_WhenFindById_thenReturnProductDTO() {
        // Given
        given(repository.findById(1L)).willReturn(Optional.of(productEntity));

        // When
        ProductDTO foundProduct = service.findById(1L);

        // Then
        assertNotNull(foundProduct);
        assertEquals(1L, foundProduct.id());
    }

    @DisplayName("Given non-existing ID when Find By Id then Throws Exception")
    @Test
    void testGivenNonExistingId_WhenFindById_thenThrowsException() {
        // Given
        given(repository.findById(99L)).willReturn(Optional.empty());
        String expectedMessage = "Product not found with id: 99";

        // When & Then
        RuntimeException exception = assertThrows(ResourceNotFoundException.class, () -> {
            service.findById(99L);
        });
        assertEquals(expectedMessage, exception.getMessage());
    }

    @DisplayName("Given Product List when Find All Products then Return Product List")
    @Test
    void testGivenProductList_WhenFindAllProducts_thenReturnProductsList() {
        // Given / Arrange
        ProductDTO productDTO2 = new ProductDTO(
                null,
                "New Description",
                CategoryEnum.ACCESSORIES,
                new BigDecimal("99.99"));

        given(repository.findAll()).willReturn(List.of(productEntity, mapper.toEntity(productDTO2)));

        // When / Act
        List<ProductDTO> productList = service.list();

        // Then / Assert
        assertNotNull(productList);
        assertEquals(2, productList.size());
    }

    @DisplayName("Given Product Id when Delete Product then Do Nothing")
    @Test
    void testGivenProductId_WhenDeleteProduct_thenDoNothing() {
        // Given / Arrange
        productEntity.setId(1L);
        given(repository.findById(anyLong())).willReturn(Optional.of(productEntity));
        willDoNothing().given(repository).delete(productEntity);

        // When / Act
        service.delete(productEntity.getId());

        // Then / Assert
        verify(repository, times(1)).delete(productEntity);
    }
}