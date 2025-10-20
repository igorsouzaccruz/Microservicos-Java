package com.microservice.gateway.config;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class LoggingGlobalFilterTest {

    private LoggingGlobalFilter filter;

    @Mock
    private GatewayFilterChain chain;

    private MockServerHttpResponse response;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        filter = new LoggingGlobalFilter();
        response = new MockServerHttpResponse();
    }

    @Test
    @DisplayName("Deve registrar logs e adicionar headers de claims JWT no request")
    void shouldAddHeadersWhenClaimsExist() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .method(HttpMethod.GET, URI.create("/api/test"))
                .build();

        ServerWebExchange exchange = MockServerWebExchange.from(request);

        Claims claims = mock(Claims.class);
        when(claims.get("email")).thenReturn("user@test.com");
        when(claims.get("sub")).thenReturn("123");
        when(claims.get("role")).thenReturn("ADMIN");

        exchange.getAttributes().put("jwtClaims", claims);

        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // Act
        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        // Assert
        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());

        ServerWebExchange mutatedExchange = captor.getValue();

        assertThat(mutatedExchange.getRequest().getHeaders().getFirst("X-User-Email")).isEqualTo("user@test.com");
        assertThat(mutatedExchange.getRequest().getHeaders().getFirst("X-User-Id")).isEqualTo("123");
        assertThat(mutatedExchange.getRequest().getHeaders().getFirst("X-User-Role")).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("Não deve adicionar headers se claims forem nulos")
    void shouldNotAddHeadersWhenClaimsNull() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .method(HttpMethod.POST, URI.create("/api/no-claims"))
                .build();

        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // Act
        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        // Assert
        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());

        ServerWebExchange mutatedExchange = captor.getValue();
        Map<String, String> headers = mutatedExchange.getRequest().getHeaders().toSingleValueMap();

        assertThat(headers).doesNotContainKeys("X-User-Email", "X-User-Id", "X-User-Role");
    }

    @Test
    @DisplayName("Deve registrar log de resposta com tempo de execução")
    void shouldLogResponseTime() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .method(HttpMethod.GET, URI.create("/api/time"))
                .build();

        ServerWebExchange exchange = MockServerWebExchange.from(request);
        response.setStatusCode(HttpStatus.OK);

        when(chain.filter(any(ServerWebExchange.class))).thenAnswer(invocation -> Mono.empty());

        // Act
        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        // Assert
        verify(chain, times(1)).filter(any(ServerWebExchange.class));
        assertThat(filter.getOrder()).isZero(); // ensure order is 0
    }
}