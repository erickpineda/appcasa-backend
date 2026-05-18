# AppCasa Backend

Backend REST de AppCasa para la gestion del hogar.

## Stack

- Java 25
- Maven
- Spring Boot 3.5
- Spring Security + JWT
- Spring Data JPA
- Flyway
- MySQL / H2

## Prerequisitos

- JDK 25
- Maven 3.9+
- MySQL 8 para perfiles `dev` o `prod`

## Arranque rapido

### Perfil local

```bash
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

### Compilacion

```bash
mvn clean test
```

## Repositorio hermano

El frontend de AppCasa vive en el repositorio publico `appcasa-frontend`.
