# LogiRoute - Architecture Documentation

## Overview

LogiRoute is a logistics optimization system built with Spring Boot 3 that demonstrates clean architecture principles, domain-driven design, and sophisticated business logic implementation.

## Core Design Principles

### 1. Clean Architecture

The application follows a layered architecture with clear separation of concerns:

```
Controller Layer (API)
       ↓
Service Layer (Business Logic)
       ↓
Repository Layer (Data Access)
       ↓
Database (PostgreSQL)
```

**Benefits:**
- Each layer has a single responsibility
- Dependencies point inward (controllers depend on services, services depend on repositories)
- Business logic is isolated and testable
- Easy to modify or replace components

### 2. Domain-Driven Design

#### Rich Domain Models

Entities contain business logic, not just data:

```java
// Vehicle.java
public boolean canLoad(double weightKg) {
    return getRemainingCapacityKg() >= weightKg;
}

// Package.java
public boolean canTransitionTo(PackageStatus newStatus) {
    // State machine logic
}
```

**Benefits:**
- Business rules are colocated with data
- Self-documenting code
- Reduced coupling between layers

#### Value Objects

Enums represent domain concepts:
- `VehicleStatus`: AVAILABLE, IN_TRANSIT
- `PackageStatus`: CREATED, LOADED, DELIVERED

### 3. Business Logic Implementation

#### Capacity Guard Pattern

**Location:** `DeliveryService.java:47-56`

**Purpose:** Prevent vehicle overloading

**Implementation:**
```java
double totalPackageWeight = packages.stream()
    .mapToDouble(Package::getWeightKg)
    .sum();

if (!vehicle.canLoad(totalPackageWeight)) {
    throw VehicleOverloadedException.forPackage(...);
}
```

**Testing:** `DeliveryServiceTest.java:127-142`

#### State Machine Pattern

**Location:** `Package.java:55-65`

**Purpose:** Enforce valid status transitions

**Valid Transitions:**
```
CREATED → LOADED → DELIVERED
   ✓        ✓          ✗
```

**Implementation:**
```java
public boolean canTransitionTo(PackageStatus newStatus) {
    return switch (this.status) {
        case CREATED -> newStatus == PackageStatus.LOADED;
        case LOADED -> newStatus == PackageStatus.DELIVERED;
        case DELIVERED -> false;
    };
}
```

**Testing:** `DeliveryServiceTest.java:202-273`

#### Smart Routing Algorithm

**Location:** `DeliveryService.java:58-61`

**Purpose:** Optimize delivery order by deadline

**Implementation:**
```java
List<Package> sortedPackages = packages.stream()
    .sorted(Comparator.comparing(Package::getDeliveryDeadline))
    .toList();
```

**Testing:** `DeliveryServiceTest.java:165-187`

## Exception Handling Strategy

### Custom Exception Hierarchy

```
RuntimeException
    ├── VehicleOverloadedException (Business Rule)
    ├── InvalidStatusTransitionException (Business Rule)
    └── ResourceNotFoundException (Data Access)
```

### Global Exception Handler

**Location:** `GlobalExceptionHandler.java`

**Benefits:**
- Centralized error handling
- Consistent error responses
- Proper HTTP status codes
- Detailed error messages for debugging

**Example Response:**
```json
{
  "timestamp": "2025-12-10T11:27:36",
  "status": 400,
  "error": "Vehicle Overload",
  "message": "Vehicle 'ABC-1234' cannot load 500.00 kg. Remaining capacity: 100.00 kg",
  "path": "/api/delivery/assign"
}
```

## DTO Pattern & MapStruct

### Separation of Concerns

```
API Layer (DTOs) ←→ MapStruct Mappers ←→ Domain Layer (Entities)
```

**Benefits:**
- API contracts independent of domain model
- Prevents exposing internal structure
- Type-safe mapping with compile-time verification
- Performance optimization (MapStruct generates code at compile time)

**Example:**
```java
@Mapper(componentModel = "SPRING")
public interface VehicleMapper {
    @Mapping(target = "remainingCapacityKg",
             expression = "java(vehicle.getRemainingCapacityKg())")
    VehicleDto toDto(Vehicle vehicle);
}
```

## Database Design

### Entity Relationships

```
Vehicle (1) ←→ (M) DeliveryRoute (1) ←→ (M) Package
```

**Cardinality:**
- One vehicle can have multiple delivery routes
- One delivery route can contain multiple packages
- One package belongs to one delivery route (or none)

### Cascade Operations

```java
@OneToMany(mappedBy = "vehicle",
           cascade = CascadeType.ALL,
           orphanRemoval = true)
private List<DeliveryRoute> deliveryRoutes;
```

**Benefits:**
- Automatic deletion of routes when vehicle is deleted
- Maintains referential integrity
- Simplifies repository operations

### Lazy Loading

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "vehicle_id")
private Vehicle vehicle;
```

**Benefits:**
- Reduces initial query overhead
- Loads related entities only when accessed
- Improves performance for large object graphs

## Transaction Management

### Declarative Transactions

```java
@Transactional
public DeliveryRouteDto assignPackagesToVehicle(...)

@Transactional(readOnly = true)
public DeliveryRouteDto getDeliveryRoute(...)
```

**Benefits:**
- Automatic commit/rollback
- ACID guarantees
- Read-only optimization for queries
- No manual transaction management code

## Testing Strategy

### Unit Testing with Mockito

**Approach:** Test business logic in isolation

**Components:**
- `@ExtendWith(MockitoExtension.class)` - JUnit 5 integration
- `@Mock` - Mock dependencies
- `@InjectMocks` - Inject mocks into service
- AssertJ - Fluent assertions

**Example:**
```java
@Test
void assignPackagesToVehicle_CapacityGuard_ThrowsException() {
    // Given
    testVehicle.setCurrentLoadKg(900.0);
    when(vehicleRepository.findById(1L))
        .thenReturn(Optional.of(testVehicle));

    // When/Then
    assertThatThrownBy(() ->
        deliveryService.assignPackagesToVehicle(1L, packageIds))
        .isInstanceOf(VehicleOverloadedException.class);
}
```

### Test Coverage

**Business Logic Tests:**
- ✓ Capacity guard validation
- ✓ State machine transitions
- ✓ Smart routing algorithm
- ✓ Error handling
- ✓ Transaction boundaries

**Coverage:** 12 comprehensive unit tests for `DeliveryService`

## Configuration Management

### Profile-Based Configuration

```
application.yml (production)
application-test.yml (testing with H2)
```

**Benefits:**
- Environment-specific configuration
- Easy switching between databases
- Separated test configuration

### Externalized Configuration

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/logiroute}
    username: ${DB_USER:logiroute}
    password: ${DB_PASSWORD:logiroute123}
```

**Benefits:**
- 12-factor app compliance
- Docker/Kubernetes friendly
- Secure credential management

## Build Configuration

### Maven Compiler Plugin

```xml
<annotationProcessorPaths>
    <path><!-- Lombok --></path>
    <path><!-- MapStruct --></path>
    <path><!-- Lombok-MapStruct Binding --></path>
</annotationProcessorPaths>
```

**Purpose:** Lombok and MapStruct integration

**Processing Order:**
1. Lombok generates getters/setters/builders
2. MapStruct uses generated code for mappings
3. Binding ensures compatibility

## Dependency Injection

### Constructor Injection

```java
@Service
@RequiredArgsConstructor // Lombok generates constructor
public class DeliveryService {
    private final VehicleRepository vehicleRepository;
    private final PackageRepository packageRepository;
    // ...
}
```

**Benefits:**
- Immutable dependencies (final fields)
- Testability (easy to provide mocks)
- No need for @Autowired
- Compile-time safety

## Logging Strategy

### SLF4J with Logback

```java
@Slf4j // Lombok generates logger
public class DeliveryService {
    public void assignPackages(...) {
        log.info("Attempting to assign {} packages to vehicle ID {}",
                 packageIds.size(), vehicleId);
        log.error("Vehicle overload detected: ...", ...);
    }
}
```

**Log Levels:**
- INFO: Business operations
- ERROR: Exceptions and failures
- DEBUG: Detailed flow (development)

## Performance Considerations

### Query Optimization

```java
@EntityGraph(attributePaths = {"packages", "vehicle"})
DeliveryRoute findById(Long id);
```

**Benefits:**
- Reduces N+1 query problem
- Eager loads specific associations
- Optimizes database round trips

### DTO Projection

```java
public interface PackageSummary {
    Long getId();
    Double getWeightKg();
    PackageStatus getStatus();
}
```

**Benefits:**
- Fetch only required fields
- Reduce memory footprint
- Faster queries

## Security Considerations

### Input Validation

```java
@NotBlank(message = "License plate is required")
@Pattern(regexp = "[A-Z]{3}-\\d{4}")
private String licensePlate;

@Positive(message = "Weight must be positive")
private Double weightKg;
```

**Benefits:**
- Fail fast on invalid input
- Clear error messages
- Prevents database constraint violations

### SQL Injection Prevention

Spring Data JPA uses parameterized queries automatically:

```java
List<Package> findByStatus(PackageStatus status);
// Generates: SELECT * FROM packages WHERE status = ?
```

## Scalability Patterns

### Horizontal Scalability

**Current state:** Stateless services

**Future considerations:**
- Add caching (Redis)
- Message queues for async processing
- Read replicas for queries
- CQRS pattern for high-read scenarios

### Vertical Scalability

**Database indexes:**
```sql
CREATE INDEX idx_packages_status ON packages(status);
CREATE INDEX idx_vehicles_status ON vehicles(status);
```

## Code Quality Tools

### Static Analysis

**Potential integrations:**
- SonarQube (code quality)
- SpotBugs (bug detection)
- Checkstyle (code style)
- JaCoCo (test coverage)

### Documentation

**Current:**
- JavaDoc on public APIs
- Architecture documentation
- README with examples

## Extension Points

### Adding New Business Rules

1. Create custom exception (if needed)
2. Add validation method to domain entity
3. Implement in service layer
4. Add exception handling in GlobalExceptionHandler
5. Write unit tests

### Adding New Entities

1. Create entity class
2. Create repository interface
3. Create DTO and mapper
4. Create service class
5. Create controller
6. Add validation rules
7. Write tests

## Deployment

### Docker Compose (Development)

```yaml
services:
  postgres:
    image: postgres:16-alpine
    ports: ["5432:5432"]
```

### Production Considerations

**Containerization:**
```dockerfile
FROM eclipse-temurin:17-jre-alpine
COPY target/logiroute-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

**Kubernetes:**
- Deployment for app
- StatefulSet for database
- ConfigMap for configuration
- Secrets for credentials
- Service for load balancing

## Monitoring & Observability

### Spring Boot Actuator (Future)

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,info
```

**Metrics:**
- HTTP request duration
- Database query performance
- JVM memory usage
- Custom business metrics

### Distributed Tracing (Future)

- Spring Cloud Sleuth
- Zipkin/Jaeger integration
- Request correlation IDs

## Best Practices Demonstrated

1. ✅ Single Responsibility Principle
2. ✅ Dependency Inversion Principle
3. ✅ Interface Segregation
4. ✅ Don't Repeat Yourself (DRY)
5. ✅ You Aren't Gonna Need It (YAGNI)
6. ✅ Separation of Concerns
7. ✅ Fail Fast Principle
8. ✅ Test-Driven Development
9. ✅ Immutability (where applicable)
10. ✅ Explicit over Implicit

## References

- Spring Boot Documentation: https://spring.io/projects/spring-boot
- Domain-Driven Design (Eric Evans)
- Clean Architecture (Robert C. Martin)
- Effective Java (Joshua Bloch)
