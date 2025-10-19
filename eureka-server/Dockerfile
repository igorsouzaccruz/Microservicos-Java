# ===============================
# STAGE 1 — BUILD (JDK 21 + Maven)
# ===============================
FROM eclipse-temurin:21-jdk AS builder

RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*
WORKDIR /app

# Copia POM e baixa dependências
COPY pom.xml .
RUN mvn -B -f pom.xml dependency:go-offline

# Copia o código e compila
COPY src ./src
RUN mvn -B clean package -DskipTests

# ===============================
# STAGE 2 — RUNTIME (JDK 21 puro)
# ===============================
FROM eclipse-temurin:21-jdk AS runtime

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8761

ENV TZ=America/Fortaleza
ENV LANG=pt_BR.UTF-8

HEALTHCHECK --interval=20s --timeout=5s --retries=5 CMD curl -f http://localhost:8761/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
