# =========================
# BUILD STAGE
# =========================
FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

# Copiar wrapper primero
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Dar permisos
RUN chmod +x mvnw

# Descargar dependencias (mejor cache)
RUN ./mvnw dependency:go-offline

# Copiar código
COPY src ./src

# Compilar
RUN ./mvnw clean package -DskipTests


# =========================
# RUNTIME STAGE
# =========================
FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]