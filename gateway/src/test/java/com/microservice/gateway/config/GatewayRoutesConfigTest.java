package com.microservice.gateway.config;

import com.microservice.gateway.security.JwtValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.gateway.route.RouteLocator;
import reactor.test.StepVerifier;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class GatewayRoutesConfigTest {

    @Autowired
    private RouteLocator routeLocator;

    @MockBean
    private JwtValidator jwtValidator;

    @MockBean
    private JwtAuthGlobalFilter jwtAuthGlobalFilter;

    @Test
    @DisplayName("Deve carregar todas as rotas definidas no GatewayRoutesConfig")
    void testRoutesExistence() {
        StepVerifier.create(routeLocator.getRoutes().collectList())
                .assertNext(routes -> {
                    assertThat(routes).isNotEmpty();
                    assertThat(routes).extracting("id")
                            .containsExactlyInAnyOrder(
                                    "account-service",
                                    "product-service",
                                    "sales-service"
                            );
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Deve conter rota para account-service com URI correta") // NOME CORRIGIDO
    void testAccountServiceRouteConfig() {
        StepVerifier.create(routeLocator.getRoutes()
                        .filter(route -> route.getId().equals("account-service"))
                        .single()) // .single() garante que apenas uma rota com esse ID existe
                .assertNext(route -> {
                    assertThat(route.getUri().toString()).isEqualTo("lb://account-service");
                    assertThat(route.getFilters()).isNotEmpty(); // Verificamos que há filtros (o rewritePath)
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Deve conter rota para product-service com URI correta")
    void testProductServiceRouteConfig() {
        StepVerifier.create(routeLocator.getRoutes()
                        .filter(route -> route.getId().equals("product-service"))
                        .single())
                .assertNext(route ->
                        assertThat(route.getUri().toString()).isEqualTo("lb://product-service")
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Deve conter rota para sales-service com URI correta") // NOME CORRIGIDO
    void testSalesServiceRouteConfig() {
        StepVerifier.create(routeLocator.getRoutes()
                        .filter(route -> route.getId().equals("sales-service"))
                        .single())
                .assertNext(route ->
                        assertThat(route.getUri().toString()).isEqualTo("lb://sales-service")
                )
                .verifyComplete();
    }
}