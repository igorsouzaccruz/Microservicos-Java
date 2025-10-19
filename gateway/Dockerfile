# ===============================
# STAGE 1 — BUILD (JDK 21 + Maven)
# ===============================
FROM eclipse-temurin:21-jdk AS builder

# Instala o Maven (a imagem JDK pura não inclui)
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copia o POM e baixa dependências (melhor cache em builds repetidos)
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copia o código-fonte e empacota
COPY src ./src
RUN mvn -B clean package -DskipTests

# ===============================
# STAGE 2 — RUNTIME (JDK 21 puro)
# ===============================
FROM eclipse-temurin:21-jdk AS runtime

WORKDIR /app

# Copia o JAR empacotado do estágio anterior
COPY --from=builder /app/target/*.jar app.jar

# Copia o diretório de chaves públicas (usado para validar JWT)
COPY src/main/resources/keys /app/keys

# Expõe a porta do Gateway
EXPOSE 8080

# Define variáveis padrão — sobrescritas via docker-compose se necessário
ENV TZ=America/Fortaleza
ENV LANG=pt_BR.UTF-8
ENV JWT_PUBLIC_KEY_PATH=/app/keys/public.pem

# Inicializa o Gateway
ENTRYPOINT ["java", "-jar", "app.jar"]
