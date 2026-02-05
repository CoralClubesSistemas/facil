# =================================================================
# ETAPA 1: Construcción (Maven + Java 21)
# =================================================================
FROM maven:3.9.5-eclipse-temurin-21 AS build

WORKDIR /app

# Recibimos credenciales desde el GitHub Action (definidas en build-args)
ARG GITHUB_ACTOR
ARG GITHUB_TOKEN

# 1. Copiar POM para descargar dependencias primero (Caché de Docker)
COPY pom.xml .

# 2. Configurar settings.xml dinámico para autenticar con GitHub Packages
RUN mkdir -p /root/.m2 && \
    echo "<settings><servers>" > /root/.m2/settings.xml && \
    echo "<server><id>github-utils</id><username>${GITHUB_ACTOR}</username><password>${GITHUB_TOKEN}</password></server>" >> /root/.m2/settings.xml && \
    echo "<server><id>github-exceptions</id><username>${GITHUB_ACTOR}</username><password>${GITHUB_TOKEN}</password></server>" >> /root/.m2/settings.xml && \
    echo "</servers></settings>" >> /root/.m2/settings.xml

# 3. Descargar dependencias en modo offline
RUN mvn dependency:go-offline -B

# 4. Copiar código fuente y compilar el JAR
COPY src ./src
RUN mvn clean package -DskipTests

# =================================================================
# ETAPA 2: Ejecución
# =================================================================
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Exponemos el puerto estándar de Spring Boot
EXPOSE 8080

# Copiamos el JAR generado en la etapa anterior
COPY --from=build /app/target/facil.jar app.jar

# Entrypoint para iniciar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]