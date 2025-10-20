package com.microservico.account.controllers;

import com.microservico.account.models.dto.LoginDTO;
import com.microservico.account.models.dto.RegisterDTO;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.cloud.discovery.enabled=false",
                "eureka.client.enabled=false",
                "eureka.client.register-with-eureka=false",
                "eureka.client.fetch-registry=false",
                "jwt.private-key-path=classpath:keys/private.pem"
        }
)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AccountIntegrationControllerTest {

    private static final String UNIQUE_EMAIL = "integration.test@example.com";
    private static final String PASSWORD = "password123";
    private static final String ADDRESS = "123 Integration Test St";

    @LocalServerPort
    private Integer port;
    private RequestSpecification specification;

    @BeforeEach
    void setUp() {
        specification = new RequestSpecBuilder()
                .setBaseUri("http://localhost")
                .setPort(port)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
    }

    @Test
    @DisplayName("Deve registrar um novo usuário com sucesso e retornar 201 CREATED")
    void testRegister_whenValidUser_shouldReturnCreated() {
        var registerDto = new RegisterDTO(UNIQUE_EMAIL, PASSWORD, ADDRESS, false);

        given().spec(specification)
                .contentType(ContentType.JSON)
                .body(registerDto)
                .when()
                .post("/accounts/register")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("message", equalTo("Usuário registrado com sucesso"));
    }

    @Test
    @DisplayName("Deve falhar ao registrar um usuário com email já existente e retornar 409 CONFLICT")
    void testRegister_whenUserAlreadyExists_shouldReturnConflict() {
        var registerDto = new RegisterDTO(UNIQUE_EMAIL, PASSWORD, ADDRESS, false);

        given().spec(specification)
                .contentType(ContentType.JSON)
                .body(registerDto)
                .when()
                .post("/accounts/register");

        given().spec(specification)
                .contentType(ContentType.JSON)
                .body(registerDto)
                .when()
                .post("/accounts/register")
                .then()
                .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    @DisplayName("Deve logar um usuário existente e retornar 200 OK com token")
    void testLogin_whenValidCredentials_shouldReturnOkAndToken() {
        var registerDto = new RegisterDTO(UNIQUE_EMAIL, PASSWORD, ADDRESS, false);
        given().spec(specification).contentType(ContentType.JSON).body(registerDto).when().post("/accounts/register");

        var loginDto = new LoginDTO(UNIQUE_EMAIL, PASSWORD);

        given().spec(specification)
                .contentType(ContentType.JSON)
                .body(loginDto)
                .when()
                .post("/accounts/login")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("access_token", notNullValue());
    }

    @Test
    @DisplayName("Deve falhar ao logar com senha incorreta e retornar 401 UNAUTHORIZED")
    void testLogin_whenInvalidPassword_shouldReturnUnauthorized() {
        var registerDto = new RegisterDTO(UNIQUE_EMAIL, PASSWORD, ADDRESS, false);
        given().spec(specification).contentType(ContentType.JSON).body(registerDto).when().post("/accounts/register");

        var loginDto = new LoginDTO(UNIQUE_EMAIL, "wrong-password");

        given().spec(specification)
                .contentType(ContentType.JSON)
                .body(loginDto)
                .when()
                .post("/accounts/login")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @DisplayName("Deve falhar ao registrar com email inválido e retornar 400 BAD REQUEST")
    void testRegister_whenInvalidEmail_shouldReturnBadRequest() {
        var registerDto = new RegisterDTO("not-an-email", PASSWORD, ADDRESS, false);

        given().spec(specification)
                .contentType(ContentType.JSON)
                .body(registerDto)
                .when()
                .post("/accounts/register")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }
}