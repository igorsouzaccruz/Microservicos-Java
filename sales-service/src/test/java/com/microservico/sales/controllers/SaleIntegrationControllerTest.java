package com.microservico.sales.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.microservico.sales.client.ProductClient;
import com.microservico.sales.models.dtos.ProductResponse;
import com.microservico.sales.models.dtos.SaleRequest;
import com.microservico.sales.models.dtos.SaleResponse;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "eureka.client.enabled=false",
                "spring.cloud.discovery.enabled=false"
        }
)
@TestMethodOrder(MethodOrderer.MethodName.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class SaleIntegrationControllerTest {

    private static ObjectMapper objectMapper;

    private SaleResponse saleResponse;

    @LocalServerPort
    private int port;

    private RequestSpecification specification;

    @Autowired
    private ProductClient productClient;

    @BeforeAll
    void initMapper() {
        objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        objectMapper.registerModule(new JavaTimeModule());
    }

    @BeforeEach
    void setupSpecification() {
        specification = new RequestSpecBuilder()
                .setBasePath("/sales")
                .setPort(port)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();

        when(productClient.getProductById(1L))
                .thenReturn(new ProductResponse(1L, "Mocked Product",  100.0));
        when(productClient.getProductById(2L))
                .thenReturn(new ProductResponse(2L, "Other Product", 50.0));
    }

    @TestConfiguration
    static class MockConfig {
        @Bean
        @Primary
        public ProductClient productClient() {
            return Mockito.mock(ProductClient.class);
        }
    }

    @Test
    @DisplayName("IntegrationTest — Should create a new Sale and return SaleResponse")
    void testA_CreateSale_ShouldReturnSaleResponse() throws JsonProcessingException {
        SaleRequest request = new SaleRequest();
        request.setProductId(1L);
        request.setQuantity(3);

        String content = given()
                .spec(specification)
                .header("X-User-Id", "10")
                .contentType("application/json")
                .body(request)
                .when()
                .post()
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString();

        SaleResponse created = objectMapper.readValue(content, SaleResponse.class);
        saleResponse = created;

        assertNotNull(created);
        assertTrue(created.id() > 0);
        assertEquals(1L, created.productId());
        assertEquals(10L, created.userId());
        assertEquals(3, created.quantity());
        assertNotNull(created.saleDate());
    }

    @Test
    @DisplayName("IntegrationTest — Should list all sales for a given userId")
    void testB_ListSalesByUser_ShouldReturnList() throws JsonProcessingException {
        assertNotNull(saleResponse, "A venda precisa ter sido criada no teste anterior");

        String content = given()
                .spec(specification)
                .pathParam("userId", saleResponse.userId())
                .when()
                .get("/user/{userId}")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        List<SaleResponse> sales = Arrays.asList(objectMapper.readValue(content, SaleResponse[].class));

        assertFalse(sales.isEmpty());
        assertTrue(sales.stream().anyMatch(s -> s.userId().equals(saleResponse.userId())));
        assertTrue(sales.stream().anyMatch(s -> s.productId().equals(saleResponse.productId())));
    }

    @Test
    @DisplayName("IntegrationTest — Should return 400 Bad Request when X-User-Id header is missing")
    void testC_CreateSaleWithoutHeader_ShouldReturnBadRequest() {
        SaleRequest invalidRequest = new SaleRequest();
        invalidRequest.setProductId(2L);
        invalidRequest.setQuantity(1);

        given()
                .spec(specification)
                .contentType("application/json")
                .body(invalidRequest)
                .when()
                .post()
                .then()
                .statusCode(400);
    }
}