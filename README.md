# Microserviços Java — E-commerce Distribuído (Spring Cloud)

> Documentação Técnica Completa — Versão Enterprise

---

## 1. Visão Geral do Sistema

O projeto **Microserviços Java** é uma arquitetura distribuída voltada para simular um ambiente de e-commerce modularizado em microserviços, cada um com responsabilidades específicas, comunicação via REST e orquestração centralizada.

O sistema é composto por cinco serviços principais:
- **Eureka Server**: registro e descoberta de serviços.
- **Gateway API**: roteamento e autenticação JWT com **RSA**.
- **Account Service**: autenticação e gerenciamento de usuários.
- **Product Service**: gerenciamento de catálogo de produtos.
- **Sales Service**: registro e controle de vendas.

Esses serviços se comunicam dinamicamente via **Spring Cloud Eureka**, e o tráfego passa por um **Spring Cloud Gateway**, que atua como camada de entrada e segurança.

---

## 2. Arquitetura Geral

### Diagrama ASCII

```
                ┌───────────────────────────────┐
                │         API GATEWAY           │
                │       (Porta 8080)            │
                │ - Validação JWT               │
                │ - Roteamento de Requisições   │
                └──────────────┬────────────────┘
                               │
        ┌──────────────────────┼────────────────────────┐
        │                      │                        │
┌───────────────┐     ┌────────────────┐      ┌────────────────┐
│ ACCOUNT SVC   │     │ PRODUCT SVC    │      │ SALES SVC      │
│ Porta: 8081   │     │ Porta: 8082    │      │ Porta: 8083    │
│ - Login       │     │ - CRUD Produtos│      │ - Vendas        │
│ - Registro    │     │ - Consulta     │      │ - Integração    │
└───────────────┘     └────────────────┘      └────────────────┘
        │                      │                        │
        └──────────────────────┴────────────────────────┘
                               │
                      ┌───────────────────┐
                      │  EUREKA SERVER    │
                      │  (Porta 8761)     │
                      └───────────────────┘
```

### Local para Imagem Visual
```markdown
![Arquitetura dos Microserviços](https://github.com/igorsouzaccruz/Microservicos-Java/blob/main/microservicos.png?raw=true)

```

---

## 3. Estrutura Completa do Projeto

```
📦 Microservicos-Java
├── 📁 account-service
│   ├── src/main/java/com/microservico/account
│   │   ├── controllers/AccountController.java
│   │   ├── models/{User.java, dto/LoginDTO.java, dto/RegisterDTO.java}
│   │   ├── repositories/UserRepository.java
│   │   ├── security/{JwtAuthFilter.java, JwtProvider.java, SecurityConfig.java}
│   │   └── services/AccountService.java
│   ├── src/test/java/com/microservico/account
│   │   ├── AccountControllerTest.java
│   │   └── AccountServiceTest.java
│   └── resources/application.yml
│
├── 📁 product-service
│   ├── controllers/ProductController.java
│   ├── models/Product.java
│   ├── repositories/ProductRepository.java
│   ├── services/ProductService.java
│   ├── test/ProductControllerTest.java
│   └── resources/application.yml
│
├── 📁 sales-service
│   ├── controllers/SalesController.java
│   ├── models/Sale.java
│   ├── clients/ProductClient.java
│   ├── services/SalesService.java
│   ├── test/SalesControllerTest.java
│   └── resources/application.yml
│
├── 📁 gateway
│   ├── config/SecurityConfig.java
│   ├── filters/JwtAuthFilter.java
│   └── resources/application.yml
│
├── 📁 eureka-server
│   ├── EurekaServerApplication.java
│   └── resources/application.yml
│
├── docker-compose.yml
├── pom.xml (pai)
└── README.md
```

## 4. Como Executar o Projeto

### Localmente com Maven
```bash
# 1. Subir o Eureka Server
cd eureka-server
mvn spring-boot:run

# 2. Subir o Gateway
cd ../gateway
mvn spring-boot:run

# 3. Subir os microserviços
cd ../account-service && mvn spring-boot:run
cd ../product-service && mvn spring-boot:run
cd ../sales-service && mvn spring-boot:run
```

### Via Docker Compose
```bash
docker-compose up -d --build
```
**Serviços disponíveis:**
- Gateway: [http://localhost:8080](http://localhost:8080)
- Eureka Server: [http://localhost:8761](http://localhost:8761)
- Swagger: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) (need fix)

Para parar:
```bash
docker-compose down
```

---

## 5. Endpoints e Exemplos

### Account Service
| Método | Endpoint | Descrição |
|---------|-----------|-----------|
| POST | `/api/accounts/register` | Cadastra novo usuário |
| POST | `/api/accounts/login` | Autentica e retorna JWT |


**Register**
```bash
curl -X POST http://localhost:8080/api/accounts/register -H "Content-Type: application/json"   -d '{
  "email": "igors2@teste.com",
  "password": "123456",
  "address": "Rua Exemplo, 123 - Fortaleza, CE",
  "admin": true
}'
```
**Resposta**
```json
{
    "message": "Usuário registrado com sucesso"
}
```
---
**Login**
```bash
curl -X POST http://localhost:8080/api/accounts/login   -H "Content-Type: application/json"   -d '{"email":"user@example.com","password":"123456"}'
```
**Resposta**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNzQwMDAwMDAwLCJleHAiOjE3NDAwMzYwMDB9.DyP0RUTR-J4TjF8x5Xpr9HfK1hLPOyCqDkz2_sF8mDE"
}
```

### Product Service
| Método | Endpoint | Descrição |
|---------|-----------|-----------|
| GET | `/api/products` | Lista todos os produtos |
| POST | `/api/products` | Cadastra produto |
| PUT | `/api/products/{id}` | Atualiza produto |
| DELETE | `/api/products/{id}` | Exclui produto |

### Sales Service
| Método | Endpoint | Descrição |
|---------|-----------|-----------|
| GET | `/api/sales/user/{id}` | Vendas por usuário |
| POST | `/api/sales` | Registra nova venda |

---

## 6. Testes Automatizados

**Frameworks:** JUnit 5, Mockito  
**Execução:**
```bash
mvn test
```
---


## 7. Dependências e Configuração de Cada Serviço

### Account Service
**Responsabilidade:** autenticação e registro de usuários.  
**Principais dependências:**
```xml
<dependencies>
  <dependency>spring-boot-starter-web</dependency>
  <dependency>spring-boot-starter-security</dependency>
  <dependency>spring-boot-starter-data-jpa</dependency>
  <dependency>io.jsonwebtoken:jjwt-api</dependency>
  <dependency>io.jsonwebtoken:jjwt-impl</dependency>
  <dependency>io.jsonwebtoken:jjwt-jackson</dependency>
  <dependency>com.h2database:h2</dependency>
</dependencies>
```

**application.yml**
```yaml
server:
  port: ${SERVER_PORT:8081}
  forward-headers-strategy: native

spring:
  application:
    name: account-service

  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:h2:mem:accountdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE}
    driver-class-name: org.h2.Driver
    username: ${SPRING_DATASOURCE_USERNAME:sa}
    password: ${SPRING_DATASOURCE_PASSWORD:}
  h2:
    console:
      enabled: true
      path: /h2-console

  jpa:
    hibernate:
      ddl-auto: ${SPRING_JPA_HIBERNATE_DDL_AUTO:update}
    properties:
      hibernate:
        format_sql: true
    show-sql: true

jwt:
  private-key-path: ${JWT_PRIVATE_KEY_PATH:file:/app/keys/private.pem}
  ttl-seconds: ${JWT_TTL_SECONDS:3600}

eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
    registry-fetch-interval-seconds: 5
    service-url:
      defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE:http://eureka:8761/eureka/}
  instance:
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 10
    lease-expiration-duration-in-seconds: 30

logging:
  level:
    root: INFO
    org.hibernate.SQL: DEBUG
    org.springframework.security: INFO
    com.microservico.account: DEBUG

```

### Product Service
**Responsabilidade:** catálogo de produtos e CRUD.  
**application.yml**
```yaml
server:
  port: 8085
  forward-headers-strategy: native

spring:
  application:
    name: product-service
  datasource:
    url: jdbc:h2:mem:testdb
    driverClassName: org.h2.Driver
    username: sa
    password: ''
  h2:
    console:
      enabled: true
      path: /h2-console
      settings:
        trace: false
        web-allow-others: false
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
eureka:
  client:
    service-url:
      defaultZone: http://eureka:8761/eureka/
    register-with-eureka: true
    fetch-registry: true

```

### Sales Service
**Responsabilidade:** registro de vendas e integração com Product Service.  
**application.yml**
```yaml
server:
  port: 8083

spring:
  application:
    name: sales-service
  datasource:
    url: jdbc:h2:mem:salesdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

eureka:
  client:
    service-url:
      defaultZone: http://eureka:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
```

### Gateway
**Responsabilidade:** roteamento e autenticação JWT.  
**application.yml**
```yaml
server:
  port: 8080
  forward-headers-strategy: native

spring:
  application:
    name: gateway

  cloud:
    gateway:
      server:
        webflux:
          discovery:
            locator:
              enabled: true
              lower-case-service-id: true
eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://eureka:8761/eureka/

  instance:
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 10
    lease-expiration-duration-in-seconds: 30

jwt:
  public-key-path: ${JWT_PUBLIC_KEY_PATH:file:/app/keys/public.pem}

logging:
  level:
    root: INFO
    org.springframework.cloud.gateway: INFO
    org.springframework.security: DEBUG
    org.springframework.web: INFO
    com.microservice.gateway: DEBUG

springdoc:
  api-docs:
    enabled: true
  swagger-ui:
    enabled: true
    path: /swagger-ui.html  
    display-request-duration: true
    urls:
      - name: account-service
        url: /api/accounts/v3/api-docs
      - name: product-service
        url: /api/products/v3/api-docs
      - name: sales-service
        url: /api/sales/v3/api-docs
```

### Eureka Server
**Responsabilidade:** descoberta e registro de serviços.  
**application.yml**
```yaml
server:
  port: 8761
  forward-headers-strategy: native

spring:
  application:
    name: eureka-server

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false

  instance:
    hostname: eureka
    prefer-ip-address: true

management:
  endpoints:
    web:
      exposure:
        include: health,info

logging:
  level:
    root: INFO
    com.netflix.eureka: INFO
    org.springframework.cloud: INFO

```

---

## 8. Próximos passos

- Configurar um Logger service
- Ajustes nos servicos
- Configurar Keycloak 
- Configurar um commons para reaproveitar configurações nos servicos

---


## 9. Autor e Licença

**Igor Souza Cruz**  
Software Engineer | Fullstack (Backend Focus)  
E-mail: igor.souzaccruz@gmail.com  
LinkedIn: [linkedin.com/in/igorsouzaccruz](https://linkedin.com/in/igorsouzaccruz)  
GitHub: [github.com/igorsouzaccruz](https://github.com/igorsouzaccruz)

**Licença MIT** — uso livre para fins técnicos e educacionais.
