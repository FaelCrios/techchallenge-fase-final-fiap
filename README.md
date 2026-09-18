# FilaCerta SUS

MVP de back-end desenvolvido para o Hackathon da pós-graduação FIAP.

A proposta é otimizar filas de espera do SUS e permitir o reaproveitamento de vagas disponibilizadas por cancelamentos ou recusas.

## Stack inicial

- Java 21
- Spring Boot 3.5.16
- Maven
- Spring Web
- Bean Validation
- Spring Boot Actuator
- Springdoc OpenAPI / Swagger UI
- JUnit 5

## Executando

Pré-requisitos:

- JDK 21
- Maven 3.9+

```bash
mvn spring-boot:run
```

## Endpoints iniciais

Status da aplicação:

```text
GET http://localhost:8080/api/v1/status
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

Actuator Health:

```text
GET http://localhost:8080/actuator/health
```

## Próximas etapas

1. PostgreSQL com Docker Compose.
2. Flyway para versionamento do banco.
3. Cadastro de unidades e especialidades.
4. Fila de espera.
5. Vagas e motor de priorização.
6. Ofertas, aceite, recusa e expiração.
7. Auditoria e indicadores.
