package com.microservice.gateway.config;

import com.microservice.gateway.security.JwtValidator;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthGlobalFilterTest {

    @Mock
    private JwtValidator jwtValidator;

    @Mock
    private GatewayFilterChain chain;

    @InjectMocks
    private JwtAuthGlobalFilter jwtAuthGlobalFilter;

    @Test
    @DisplayName("Deve retornar a ordem correta (-1)")
    void shouldReturnCorrectOrder() {
        assertThat(jwtAuthGlobalFilter.getOrder()).isEqualTo(-1);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/api/accounts/login",
            "/accounts/login",
            "/api/accounts/register",
            "/accounts/register",
            "/v3/api-docs/some-group",
            "/swagger-ui/index.html",
            "/webjars/swagger-ui/style.css",
            "/actuator/health",
            "/fallback/some-service"
    })
    @DisplayName("Deve continuar a cadeia (bypass) para rotas públicas")
    void shouldBypassFilterForPublicPaths(String publicPath) {
        // Arrange
        var request = MockServerHttpRequest.get(publicPath).build();
        var exchange = MockServerWebExchange.from(request);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // Act
        StepVerifier.create(jwtAuthGlobalFilter.filter(exchange, chain))
                .verifyComplete();

        // Assert
        verify(chain, times(1)).filter(exchange);
        verify(jwtValidator, never()).parse(any());
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    @DisplayName("Deve retornar 401 Unauthorized se o header Authorization estiver ausente")
    void shouldReturnUnauthorizedWhenNoHeader() {
        // Arrange
        var request = MockServerHttpRequest.get("/api/protected-route").build();
        var exchange = MockServerWebExchange.from(request);

        // Act
        StepVerifier.create(jwtAuthGlobalFilter.filter(exchange, chain))
                .verifyComplete();

        // Assert
        verify(chain, never()).filter(any());
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Deve retornar 401 Unauthorized se o header não for 'Bearer'")
    void shouldReturnUnauthorizedWhenHeaderNotBearer() {
        // Arrange
        var request = MockServerHttpRequest.get("/api/protected-route")
                .header(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz") // Basic Auth
                .build();
        var exchange = MockServerWebExchange.from(request);

        // Act
        StepVerifier.create(jwtAuthGlobalFilter.filter(exchange, chain))
                .verifyComplete();

        // Assert
        verify(chain, never()).filter(any());
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Deve retornar 401 Unauthorized se o token JWT for inválido")
    void shouldReturnUnauthorizedWhenTokenIsInvalid() {
        // Arrange
        String invalidToken = "invalid.jwt.token";
        var request = MockServerHttpRequest.get("/api/protected-route")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
                .build();
        var exchange = MockServerWebExchange.from(request);

        when(jwtValidator.parse(invalidToken)).thenThrow(new RuntimeException("Token inválido"));

        // Act
        StepVerifier.create(jwtAuthGlobalFilter.filter(exchange, chain))
                .verifyComplete();

        // Assert
        verify(jwtValidator, times(1)).parse(invalidToken);
        verify(chain, never()).filter(any());
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Deve continuar a cadeia e adicionar headers quando o token for válido")
    void shouldContinueChainAndAddHeadersWhenTokenIsValid() {
        // --- ARRANGE ---

        // 1. Crie e prepare o mockClaims AQUI (e não no @BeforeEach)
        Claims mockClaims = mock(Claims.class);
        when(mockClaims.getSubject()).thenReturn("user-123");
        when(mockClaims.get("email")).thenReturn("user@test.com");
        when(mockClaims.get("role")).thenReturn("ROLE_USER");

        // 2. Continue o Arrange do teste
        String validToken = "valid.jwt.token";
        var request = MockServerHttpRequest.get("/api/protected-route")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .build();
        var exchange = MockServerWebExchange.from(request);

        // 3. Configure o validador para retornar o mock
        when(jwtValidator.parse(validToken)).thenReturn(mockClaims);

        // 4. Capture o 'exchange' modificado que é passado para a cadeia
        ArgumentCaptor<ServerWebExchange> exchangeCaptor = ArgumentCaptor.forClass(ServerWebExchange.class);
        when(chain.filter(exchangeCaptor.capture())).thenReturn(Mono.empty());

        // --- ACT ---
        StepVerifier.create(jwtAuthGlobalFilter.filter(exchange, chain))
                .verifyComplete();

        // --- ASSERT ---
        verify(jwtValidator, times(1)).parse(validToken);
        verify(chain, times(1)).filter(any());

        ServerWebExchange mutatedExchange = exchangeCaptor.getValue();
        ServerHttpRequest mutatedRequest = mutatedExchange.getRequest();

        // Verifica os novos headers na requisição
        assertThat(mutatedRequest.getHeaders().getFirst("X-User-Id")).isEqualTo("user-123");
        assertThat(mutatedRequest.getHeaders().getFirst("X-User-Email")).isEqualTo("user@test.com");
        assertThat(mutatedRequest.getHeaders().getFirst("X-User-Role")).isEqualTo("ROLE_USER");

        // Verifica os atributos do exchange
        assertThat(mutatedExchange.getAttributes().get("jwtClaims")).isEqualTo(mockClaims);

        // Verifica que o status da resposta não foi alterado
        assertThat(mutatedExchange.getResponse().getStatusCode()).isNull();
    }
}