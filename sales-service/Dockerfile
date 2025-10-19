# ===============================
# STAGE 1 — BUILD (JDK 25 + Maven)
# ===============================
FROM eclipse-temurin:25-jdk AS builder

# Instala Maven dentro do container (pois a imagem JDK pura não vem com ele)
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copia pom.xml e baixa dependências (melhor para cache)
COPY pom.xml .
RUN mvn -B -f pom.xml dependency:go-offline

# Copia o código-fonte e empacota
COPY src ./src
RUN mvn -B clean package -DskipTests

# ===============================
# STAGE 2 — RUNTIME (JDK 25 puro)
# ===============================
FROM eclipse-temurin:25-jdk AS runtime

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8083

ENV TZ=America/Fortaleza
ENV LANG=pt_BR.UTF-8

ENTRYPOINT ["java", "-jar", "app.jar"]
