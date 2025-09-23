# Login Processing Service

A Spring Boot service that consumes `customer-login` events, calls a tracking REST API with Basic Auth, retries up to 3 times,
publishes `login-tracking-result` to Kafka and persists the result in a database (PostgresDB).

## Running locally
### Open a terminal (Bash), navigate to the project’s root folder, and run:

```bash
  docker-compose -f docker-compose-local.yml up -d
```

```bash
  .\mvnw spring-boot:run
```

## Improvements
- Flyway for schema management and DB migrations -> set jpa:hibernate:ddl-auto: none
- Replacing @SpringBootTest on CustomerTrackingServiceTests class with more lightweight @RunWith(SpringRunner.class)