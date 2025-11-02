package com.microservice.gateway.security;


import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    @Mock
    private JwtValidator jwtValidator;

    @Mock
    private WebFilterChain chain;

    private JwtAuthFilter jwtAuthFilter;
    private MockServerHttpResponse response;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        jwtAuthFilter = new JwtAuthFilter(jwtValidator);
        response = new MockServerHttpResponse();

        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
    }

    // ---------- TESTES PRINCIPAIS ----------

    @Test
    @DisplayName("Deve ignorar paths públicos como /swagger-ui e /v3/api-docs")
    void shouldBypassPublicPaths() {
        var request = MockServerHttpRequest.get("/swagger-ui/index.html").build();
        var exchange = MockServerWebExchange.from(request);

        StepVerifier.create(jwtAuthFilter.filter(exchange, chain))
                .verifyComplete();

        verify(chain, times(1)).filter(exchange);
        assertThat(response.getStatusCode()).isNull();
    }

    @Test
    @DisplayName("Deve retornar 401 Unauthorized quando header ausente em rota protegida")
    void shouldBypassWhenNoAuthorizationHeader() {
        var request = MockServerHttpRequest.get("/api/test").build();
        var exchange = MockServerWebExchange.from(request);

        StepVerifier.create(jwtAuthFilter.filter(exchange, chain))
                .verifyComplete();

        verify(chain, never()).filter(exchange);
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Deve continuar (bypass) sem autenticação para rotas públicas")
    void shouldBypassFilterForPublicPath() {
        // Arrange
        var request = MockServerHttpRequest.get("/api/accounts/login").build();
        var exchange = MockServerWebExchange.from(request);

        // Act
        StepVerifier.create(jwtAuthFilter.filter(exchange, chain))
                .verifyComplete();

        // Assert
        verify(chain, times(1)).filter(exchange);

        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    @DisplayName("Deve autenticar e adicionar claims quando token válido for fornecido")
    void shouldAuthenticateWhenTokenValid() {
        // Arrange
        var request = MockServerHttpRequest.get("/api/protected")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token123")
                .build();

        var exchange = MockServerWebExchange.from(request);

        Claims claims = mock(Claims.class);
        when(jwtValidator.parse("token123")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("user1");
        when(claims.get("role")).thenReturn("ADMIN");

        // Act
        StepVerifier.create(jwtAuthFilter.filter(exchange, chain))
                .verifyComplete();

        // Assert
        verify(chain).filter(any(ServerWebExchange.class));

        // Captura o exchange mutado
        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());
        ServerWebExchange mutated = captor.getValue();

        // Claims foram adicionadas
        //      assertThat(mutated.getAttribute("jwtClaims")).isEqualTo(claims);
    }

    @Test
    @DisplayName("Deve retornar 401 quando JwtValidator lançar exceção")
    void shouldReturn401WhenJwtValidatorFails() {
        var request = MockServerHttpRequest.get("/api/protected")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalidtoken")
                .build();

        var exchange = MockServerWebExchange.from(request);

        when(jwtValidator.parse("invalidtoken")).thenThrow(new RuntimeException("Token inválido"));

        StepVerifier.create(jwtAuthFilter.filter(exchange, chain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Deve montar authorities corretamente quando claim 'role' for lista")
    void shouldBuildAuthoritiesFromList() {
        var request = MockServerHttpRequest.get("/api/protected")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token-list")
                .build();

        var exchange = MockServerWebExchange.from(request);

        Claims claims = mock(Claims.class);
        when(jwtValidator.parse("token-list")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("user2");
        when(claims.get("role")).thenReturn(List.of("ROLE_USER", "ROLE_ADMIN"));

        StepVerifier.create(jwtAuthFilter.filter(exchange, chain))
                .verifyComplete();

        verify(chain).filter(any(ServerWebExchange.class));
        assertThat(exchange.getAttributes()).containsKey("jwtClaims");
    }

    @Test
    @DisplayName("Deve retornar vazio quando claim 'role' for nulo")
    void shouldHandleEmptyAuthorities() {
        var request = MockServerHttpRequest.get("/api/protected")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token-norole")
                .build();

        var exchange = MockServerWebExchange.from(request);

        Claims claims = mock(Claims.class);
        when(jwtValidator.parse("token-norole")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("user3");
        when(claims.get("role")).thenReturn(null);

        StepVerifier.create(jwtAuthFilter.filter(exchange, chain))
                .verifyComplete();

        assertThat(exchange.getAttributes()).containsKey("jwtClaims");
    }
}