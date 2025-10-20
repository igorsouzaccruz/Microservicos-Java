package com.microservico.sales.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.ApplicationContext;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = WebClientConfig.class,
        properties = {
                "eureka.client.enabled=false",
                "spring.cloud.discovery.enabled=false"
        }
)
class WebClientConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Deve registrar o bean WebClient.Builder no contexto Spring")
    void shouldRegisterWebClientBuilderBean() {
        WebClient.Builder builder = applicationContext.getBean(WebClient.Builder.class);
        assertNotNull(builder, "O bean WebClient.Builder deve estar registrado no contexto");
    }

    @Test
    @DisplayName("O bean WebClient.Builder deve possuir a anotação @LoadBalanced")
    void shouldHaveLoadBalancedAnnotation() throws NoSuchMethodException {

        Annotation[] annotations = WebClientConfig.class
                .getMethod("webClientBuilder")
                .getAnnotations();

        boolean hasLoadBalanced = false;
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(LoadBalanced.class)) {
                hasLoadBalanced = true;
                break;
            }
        }

        assertTrue(hasLoadBalanced, "O método webClientBuilder() deve estar anotado com @LoadBalanced");
    }
}