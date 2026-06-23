# Coding

## Build and Test Commands

```bash
# Run full build, tests, and JaCoCo coverage gate
mvn verify -ntp

# Run tests only (no coverage gate)
mvn test -ntp

# Run a single test class
mvn test -Dtest=HelloServiceTest -ntp
mvn test -Dtest=HelloApiControllerTest -ntp

# Build executable JAR (skip tests)
mvn package -DskipTests -ntp

# Build Docker image locally
docker build -t hello-api .

# Run locally
java -jar target/hello-openapi-1.0.0.jar
```

## Coverage Gate

JaCoCo enforces minimums on `mvn verify`:
- Instruction coverage: **70%**
- Branch coverage: **60%**

Generated classes are excluded: `HelloApplication`, `HelloApi`, `ApiUtil`, `model/**`. If the gate fails, add tests before pushing — CI runs `mvn verify` and will block the PR.

## Contract-First Architecture

The OpenAPI spec at `src/main/resources/openapi.yaml` is the **single source of truth**. Never edit generated files under `target/generated-sources/openapi/` directly.

The code generation flow on every build:
```
openapi.yaml
    │  openapi-generator-maven-plugin
    ▼
target/generated-sources/openapi/
    ├── com/example/hello/api/HelloApi.java        ← interface (implement this)
    ├── com/example/hello/api/ApiUtil.java          ← generated utility
    └── com/example/hello/model/GetHello200Response.java  ← response model
```

## Application Structure

```
HelloApiController  implements HelloApi (generated interface)
    └── HelloService (interface)
        └── HelloServiceImpl (business logic)
```

Logging uses SLF4J (`LoggerFactory.getLogger(ClassName.class)`). Log levels in `application.yml`:
- `root: INFO` — framework code
- `com.example.hello: DEBUG` — application code

Actuator endpoints exposed: `/actuator/health`, `/actuator/metrics`, `/actuator/prometheus`.

## Adding a New Endpoint

1. Add the endpoint to `src/main/resources/openapi.yaml`
2. Run `mvn generate-sources` to regenerate the interface
3. Implement the new method in `HelloApiController` — the generated `HelloApi` interface will show a compile error until implemented
4. Add a corresponding method to `HelloService` and `HelloServiceImpl`
5. Add log statements: INFO at entry/exit, DEBUG for values, ERROR in catch blocks
6. Write a test in `HelloApiControllerTest` using MockMvc
7. Run `mvn verify -ntp` — confirms tests pass and coverage gate holds

## Key Constraints

- `spring-boot-maven-plugin` must be declared in `pom.xml` — without it `java -jar` fails with "no main manifest attribute".
- Maven dependencies: verify versions exist on Maven Central before adding. Do not retry versions that 404.
