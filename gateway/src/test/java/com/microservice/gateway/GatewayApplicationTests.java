package com.microservice.gateway;

import com.microservice.gateway.config.JwtAuthGlobalFilter;
import com.microservice.gateway.security.JwtValidator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;


@SuppressWarnings("deprecation")
@SpringBootTest
class GatewayApplicationTests {


    @MockBean
    private JwtValidator jwtValidator;

    @MockBean
    private JwtAuthGlobalFilter jwtAuthGlobalFilter;

    @Test
    void contextLoads() {
    }

}
