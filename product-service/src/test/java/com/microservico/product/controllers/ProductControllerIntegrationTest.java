package com.microservico.product.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservico.product.models.dtos.ProductDTO;
import com.microservico.product.models.enums.CategoryEnum;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProductControllerIntegrationTest {

    private static ObjectMapper objectMapper;
    private static ProductDTO productDTO;

    @LocalServerPort
    private int port;

    private RequestSpecification specification;

    @BeforeEach
    public void setup() {

        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        }

        specification = new RequestSpecBuilder()
                .setBasePath("/products")
                .setPort(port)
                    .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                    .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
    }

    @Test
    @Order(1)
    @DisplayName("integrationTest Given Product object When Create One Product Should Return a Product Object")
    void integrationTest_When_CreateOneProduct_ShouldReturnAProductObject() throws JsonProcessingException {
        productDTO = new ProductDTO(
                null,
                "Teclado Mecânico RGB",
                CategoryEnum.PERIPHERALS,
                new BigDecimal("350.99"));

        var content = given().spec(specification)
                .contentType("application/json") // Usando a string diretamente
                .body(productDTO)
                .when()
                    .post()
                .then()
                    .statusCode(201)
                        .extract()
                            .body()
                                .asString();

        ProductDTO createdProduct = objectMapper.readValue(content, ProductDTO.class);
        productDTO = createdProduct;


        assertNotNull(createdProduct);
        assertTrue(createdProduct.id() > 0);
        assertEquals("Teclado Mecânico RGB", createdProduct.description());
    }

    @Test
    @Order(2)
    @DisplayName("integrationTest Given Product object When Update One Product Should Return an Updated Product Object")
    void integrationTest_When_UpdateOneProduct_ShouldReturnAnUpdatedProductObject() throws JsonProcessingException {

        assertNotNull(productDTO, "O DTO do produto não foi inicializado pelo teste anterior (Order 1)");

        ProductDTO updatedData = new ProductDTO(
                productDTO.id(),
                "Teclado Mecânico Gamer (Atualizado)",
                CategoryEnum.PERIPHERALS,
                new BigDecimal("399.00")
        );

        var content = given().spec(specification)
                .contentType("application/json")
                .pathParam("id", productDTO.id())
                .body(updatedData)
                .when()
                    .put("{id}")
                .then()
                    .statusCode(200)
                        .extract()
                            .body()
                                .asString();

        ProductDTO updatedProduct = objectMapper.readValue(content, ProductDTO.class);
        productDTO = updatedProduct;

        assertNotNull(updatedProduct);
        assertEquals("Teclado Mecânico Gamer (Atualizado)", updatedProduct.description());
        assertEquals(new BigDecimal("399.00"), updatedProduct.price());
    }

    @Test
    @Order(3)
    @DisplayName("integrationTest Given Product ID When FindById Should Return a Product Object")
    void integrationTest_When_FindById_ShouldReturnAProductObject() throws JsonProcessingException {
        assertNotNull(productDTO.id(), "O DTO do produto não foi inicializado pelos testes anteriores");

        var content = given().spec(specification)
                .pathParam("id", productDTO.id())
                .when()
                    .get("{id}")
                .then()
                    .statusCode(200)
                        .extract()
                            .body()
                                .asString();

        ProductDTO foundProduct = objectMapper.readValue(content, ProductDTO.class);

        assertNotNull(foundProduct);
        assertEquals(new BigDecimal("399.00"),foundProduct.price());
        assertEquals(productDTO.id(), foundProduct.id());
    }

    @Test
    @Order(4)
    @DisplayName("integrationTest When FindAll Should Return a Product List")
    void integrationTest_When_FindAll_ShouldReturnAProductsList() throws JsonProcessingException {
        assertNotNull(productDTO.id(), "O DTO do produto não foi inicializado pelos testes anteriores");

        var content = given().spec(specification)
                .when()
                    .get()
                .then()
                    .statusCode(200)
                        .extract()
                        .body()
                        .asString();

        List<ProductDTO> products = Arrays.asList(objectMapper.readValue(content, ProductDTO[].class));
        assertTrue(products.stream().anyMatch(p -> p.id().equals(productDTO.id())));
        assertEquals(1, products.size());
    }

    @Test
    @Order(5)
    @DisplayName("integrationTest Given Product ID When Delete Should Return a NoContent")
    void integrationTest_When_Delete_ShouldReturnANoContent() {
        assertNotNull(productDTO.id(), "O DTO do produto não foi inicializado pelos testes anteriores");

        given().spec(specification)
                .pathParam("id", productDTO.id())
                .when()
                    .delete("{id}")
                        .then()
                            .statusCode(204);
    }
}