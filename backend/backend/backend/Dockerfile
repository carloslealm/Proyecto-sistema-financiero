# ============================================================
#  LoanManager Backend — Dockerfile
#  Multi-stage build: reduce el tamaño final de la imagen
# ============================================================

# ── Etapa 1: Compilación ─────────────────────────────────────
# Usamos Maven + JDK 17 para compilar el proyecto
FROM maven:3.9.5-eclipse-temurin-17 AS builder

WORKDIR /app

# Copiar solo el pom.xml primero para aprovechar el cache de Docker.
# Si el pom.xml no cambió, Docker reutiliza la capa de dependencias
# sin volver a descargarlas — ahorra tiempo en cada build.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Ahora copiar el código fuente y compilar
COPY src ./src
RUN mvn clean package -DskipTests -B


# ── Etapa 2: Ejecución ───────────────────────────────────────
# Solo necesitamos el JRE (más liviano que el JDK) para ejecutar
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Crear usuario no-root por seguridad.
# Nunca ejecutes aplicaciones en producción como root.
RUN addgroup -S loanmanager && adduser -S loanmanager -G loanmanager

# Copiar solo el JAR generado en la etapa anterior
COPY --from=builder /app/target/*.jar app.jar

# Cambiar al usuario no-root
USER loanmanager

# Puerto que expone el contenedor
EXPOSE 8080

# Variables de entorno con valores por defecto
# Se pueden sobreescribir en docker-compose.yml
ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Comando de inicio
# -Djava.security.egd: mejora la velocidad de inicio en contenedores
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
