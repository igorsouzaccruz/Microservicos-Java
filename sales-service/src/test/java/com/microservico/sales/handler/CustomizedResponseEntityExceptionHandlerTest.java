//package com.microservico.sales.handler;
//
//import com.microservico.account.exceptions.ExceptionResponse;
//import com.microservico.account.exceptions.ResourceNotFoundException;
//import com.microservico.account.exceptions.handler.CustomizedResponseEntityExceptionHandler;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.http.HttpStatus;
//import org.springframework.mock.web.MockHttpServletRequest;
//import org.springframework.web.context.request.ServletWebRequest;
//
//import java.util.Objects;
//
//import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
//
//class CustomizedResponseEntityExceptionHandlerTest {
//
//    private CustomizedResponseEntityExceptionHandler handler;
//
//    @BeforeEach
//    void setUp() {
//        handler = new CustomizedResponseEntityExceptionHandler();
//    }
//
//    @Test
//    void handleAllExceptions_ShouldReturnInternalServerError() {
//        // Arrange
//        Exception ex = new Exception("Generic error");
//        var request = new ServletWebRequest(new MockHttpServletRequest());
//
//        // Act
//        var response = handler.handleAllExceptions(ex, request);
//
//        // Assert
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
//        ExceptionResponse body = response.getBody();
//        assertThat(body).isNotNull();
//        assertThat(Objects.requireNonNull(body).message()).isEqualTo("Generic error");
//        assertThat(body.error()).isEqualTo("Internal server error");
//        assertThat(body.status()).isEqualTo(500);
//    }
//
//    @Test
//    void handlerNotFoundExceptions_ShouldReturnNotFound() {
//        // Arrange
//        var ex = new ResourceNotFoundException(1L, "Product");
//        var request = new ServletWebRequest(new MockHttpServletRequest());
//
//        // Act
//        var response = handler.handlerNotFoundExceptions(ex, request);
//
//        // Assert
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
//        assertThat(response.getBody()).isNotNull();
//        assertThat(Objects.requireNonNull(response.getBody()).message()).isEqualTo("Product not found with id: 1");
//        assertThat(response.getBody().error()).isEqualTo("Resource not found");
//    }
//}