# Getting Started

This is a Java 17 spring boot project. Some libraries/tools used:

- OpenAPI for both documentation and codegen
- H2 database
- Flyway for migrations
- Lombok for code generation
- Spotless for auto code formatting

Building the app
```shell
./mvnw clean install
```
This generates openapi stubs, do code formatting, as well as building and running the test.

To run the app
```shell
./mvnw spring-boot:run
```

## API first approach

Please refer to [OpenAPI specification](src/main/resources/openapi.yml) for documentation.
Controllers and DTOs are all generated from the specification automatically.
This avoids the need of having to keep both client and server code in sync as we can also generate the client stubs for downstream clients.

Business logic is implemented via delegate interfaces.

## Database

An H2 in-memory database is used to simulate persistence. Migrations are run automatically on app bootup.
Please use the [web interface](http://localhost:8080/h2-console) if you need to explore the database.
```text
connection string: `jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1`
user: `sa`
no password
```

## Docker

A dockerfile is added for convenience.

### Build
```shell
docker build --target build -t yl/lunch:latest . 
```
docker build --target deployable -t yl/lunch:latest .
### Run
```shell
docker run -p 8080:8080 yl/lunch
```

## Improvements

### Error codes

All the errors thrown do not come with custom error messages.
We can implement exception handlers using `@ControllerAdvice`.
See [`ControllerExceptionHandler`](src/main/java/com/govtech/lunchsession/controllers/ControllerExceptionHandler.java) for an example.

### Database

Needless to say we need replace to h2 with a proper DBMS for production environments.