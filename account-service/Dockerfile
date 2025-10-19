# ===============================
# STAGE 1 — BUILD (JDK 25 + Maven)
# ===============================
FROM eclipse-temurin:25-jdk AS builder

# Instala Maven dentro do container (a imagem JDK pura não inclui)
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copia o pom.xml e baixa dependências (melhor cache em builds repetidos)
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copia o código-fonte e empacota
COPY src ./src
RUN mvn -B clean package -DskipTests

# ===============================
# STAGE 2 — RUNTIME (JDK 25 puro)
# ===============================
FROM eclipse-temurin:25-jdk AS runtime

WORKDIR /app

# Copia o JAR compilado do estágio anterior
COPY --from=builder /app/target/*.jar app.jar

# Copia o diretório de chaves privadas (montado pelo docker-compose)
# — mantém o caminho /app/keys igual ao usado na aplicação.yml
COPY src/main/resources/keys /app/keys

# Expondo a porta padrão
EXPOSE 8081

# Configura fuso horário e encoding
ENV TZ=America/Fortaleza
ENV LANG=pt_BR.UTF-8

# Variáveis padrão para JWT (podem ser sobrescritas no docker-compose)
ENV JWT_PRIVATE_KEY_PATH=/app/keys/private.pem
ENV JWT_TTL_SECONDS=3600

# Rodando o serviço
ENTRYPOINT ["java", "-jar", "app.jar"]
