# ===============================
# STAGE 1 — BUILD (JDK 25 + Maven instalado)
# ===============================
FROM eclipse-temurin:25-jdk AS builder

# Instala Maven manualmente (não vem na imagem do JDK puro)
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copia pom.xml e baixa dependências (cache eficiente)
COPY pom.xml .
RUN mvn -B -f pom.xml dependency:go-offline

# Copia o código-fonte e compila
COPY src ./src
RUN mvn -B clean package -DskipTests

# ===============================
# STAGE 2 — RUNTIME (JDK 25 puro)
# ===============================
FROM eclipse-temurin:25-jdk AS runtime

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8761

ENV TZ=America/Fortaleza
ENV LANG=pt_BR.UTF-8

HEALTHCHECK --interval=20s --timeout=5s --retries=5 CMD curl -f http://localhost:8761/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
