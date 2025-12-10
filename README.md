# LogiRoute - Logistics Optimization System

A Spring Boot 3 logistics optimization system that implements intelligent package routing with capacity management and state machine validation.

## Features

### Core Business Logic

1. **Capacity Guard**: Validates vehicle capacity before package assignment
   - Prevents overloading by checking: `package.weight + vehicle.currentLoad <= vehicle.capacity`
   - Throws `VehicleOverloadedException` when capacity is exceeded

2. **State Machine**: Enforces valid package status transitions
   - Valid flow: `CREATED -> LOADED -> DELIVERED`
   - Prevents illegal transitions (e.g., CREATED -> DELIVERED directly)
   - Throws `InvalidStatusTransitionException` for invalid transitions

3. **Smart Routing**: Automatically sorts packages by delivery deadline
   - Packages with earliest deadlines are prioritized first
   - Optimizes delivery efficiency

## Tech Stack

- **Java 17+**
- **Spring Boot 3.2.1**
- **Spring Data JPA** with PostgreSQL
- **Lombok** for boilerplate reduction
- **MapStruct** for DTO mapping
- **Jakarta Validation**
- **Docker & Docker Compose**
- **JUnit 5 & Mockito** for testing

## Project Structure

```
src/main/java/com/logistics/logiroute/
├── config/
│   └── DataLoader.java                 # Seed data loader
├── controller/
│   ├── DeliveryController.java         # Delivery & route management
│   ├── PackageController.java          # Package CRUD
│   └── VehicleController.java          # Vehicle CRUD
├── domain/
│   ├── entity/
│   │   ├── DeliveryRoute.java          # Route entity
│   │   ├── Package.java                # Package entity
│   │   └── Vehicle.java                # Vehicle entity
│   └── enums/
│       ├── PackageStatus.java          # Package states
│       └── VehicleStatus.java          # Vehicle states
├── dto/
│   ├── request/
│   │   ├── AssignPackagesRequest.java
│   │   └── UpdatePackageStatusRequest.java
│   ├── response/
│   │   └── ErrorResponse.java
│   ├── DeliveryRouteDto.java
│   ├── PackageDto.java
│   └── VehicleDto.java
├── exception/
│   ├── GlobalExceptionHandler.java     # Centralized exception handling
│   ├── InvalidStatusTransitionException.java
│   ├── ResourceNotFoundException.java
│   └── VehicleOverloadedException.java
├── mapper/
│   ├── DeliveryRouteMapper.java        # MapStruct mapper
│   ├── PackageMapper.java
│   └── VehicleMapper.java
├── repository/
│   ├── DeliveryRouteRepository.java
│   ├── PackageRepository.java
│   └── VehicleRepository.java
└── service/
    ├── DeliveryService.java            # Core business logic
    ├── PackageService.java
    └── VehicleService.java
```

## Quick Start

### 1. Start PostgreSQL

```bash
docker-compose up -d
```

### 2. Build the Project

```bash
mvn clean install
```

### 3. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080` and automatically seed the database with:
- **2 vehicles**: ABC-1234 (1000kg), XYZ-5678 (1500kg)
- **5 packages**: Various weights and delivery deadlines

### 4. Run Tests

```bash
mvn test
```

## API Examples

### View All Vehicles

```bash
curl http://localhost:8080/api/vehicles
```

### View All Packages

```bash
curl http://localhost:8080/api/packages
```

### Assign Packages to Vehicle (Capacity Guard Test)

**Scenario 1: Success - Within capacity**
```bash
curl -X POST http://localhost:8080/api/delivery/assign \
  -H "Content-Type: application/json" \
  -d '{
    "vehicleId": 1,
    "packageIds": [4, 2, 5]
  }'
```
Response: Packages sorted by deadline (4→2→5), total 950kg < 1000kg ✓

**Scenario 2: Failure - Exceeds capacity**
```bash
curl -X POST http://localhost:8080/api/delivery/assign \
  -H "Content-Type: application/json" \
  -d '{
    "vehicleId": 1,
    "packageIds": [1, 2, 3]
  }'
```
Response: VehicleOverloadedException (total 650kg > 1000kg) ✗

### Update Package Status (State Machine Test)

**Valid transition: CREATED → LOADED**
```bash
curl -X PATCH http://localhost:8080/api/packages/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "LOADED"}'
```
Response: Success ✓

**Invalid transition: CREATED → DELIVERED**
```bash
curl -X PATCH http://localhost:8080/api/packages/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "DELIVERED"}'
```
Response: InvalidStatusTransitionException ✗

### View Active Routes

```bash
curl http://localhost:8080/api/delivery/routes
```

### Complete a Route

```bash
curl -X PATCH http://localhost:8080/api/delivery/routes/1/complete
```

## Testing Scenarios (from Seed Data)

The DataLoader creates test scenarios demonstrating the business logic:

1. **Capacity Guard - Success**
   - Assign packages 4, 2, 5 to vehicle ABC-1234
   - Total: 950kg < 1000kg capacity
   - Result: Success with smart routing (sorted by deadline)

2. **Capacity Guard - Failure**
   - Try to assign package 3 (300kg) to ABC-1234 after above
   - Remaining capacity: 50kg < 300kg
   - Result: VehicleOverloadedException

3. **Smart Routing**
   - Assign packages 1, 3 to vehicle XYZ-5678
   - Packages sorted: earliest deadline first
   - Result: Optimized delivery route

## Key Implementation Details

### DeliveryService.java

**Capacity Guard Logic** (src/main/java/com/logistics/logiroute/service/DeliveryService.java:47-56)
```java
double totalPackageWeight = packages.stream()
        .mapToDouble(Package::getWeightKg)
        .sum();

if (!vehicle.canLoad(totalPackageWeight)) {
    throw VehicleOverloadedException.forPackage(
            vehicle.getLicensePlate(),
            totalPackageWeight,
            vehicle.getRemainingCapacityKg()
    );
}
```

**Smart Routing** (src/main/java/com/logistics/logiroute/service/DeliveryService.java:58-61)
```java
List<Package> sortedPackages = packages.stream()
        .sorted(Comparator.comparing(Package::getDeliveryDeadline))
        .toList();
```

**State Machine Validation** (src/main/java/com/logistics/logiroute/service/DeliveryService.java:114-123)
```java
private void updatePackageStatus(Package pkg, PackageStatus newStatus) {
    if (!pkg.canTransitionTo(newStatus)) {
        throw InvalidStatusTransitionException.forPackage(
                pkg.getId(),
                pkg.getStatus(),
                newStatus
        );
    }
    pkg.setStatus(newStatus);
}
```

### Package.java

**State Transition Logic** (src/main/java/com/logistics/logiroute/domain/entity/Package.java:55-65)
```java
public boolean canTransitionTo(PackageStatus newStatus) {
    if (this.status == newStatus) {
        return true;
    }
    return switch (this.status) {
        case CREATED -> newStatus == PackageStatus.LOADED;
        case LOADED -> newStatus == PackageStatus.DELIVERED;
        case DELIVERED -> false;
    };
}
```

## Unit Tests

Comprehensive tests for DeliveryService cover:

- ✓ Capacity guard: successful assignment within capacity
- ✓ Capacity guard: exception when exceeding capacity
- ✓ Capacity guard: multiple packages exceeding total capacity
- ✓ Smart routing: packages sorted by deadline
- ✓ State machine: valid transitions (CREATED→LOADED, LOADED→DELIVERED)
- ✓ State machine: invalid transitions prevented
- ✓ Route completion: vehicle status updates
- ✓ Error handling: resource not found, illegal arguments

Run tests: `mvn test`

## Database Schema

```sql
vehicles
├── id (PK)
├── license_plate (unique)
├── capacity_kg
├── current_load_kg
└── status (AVAILABLE | IN_TRANSIT)

packages
├── id (PK)
├── delivery_address
├── weight_kg
├── status (CREATED | LOADED | DELIVERED)
├── delivery_deadline
└── delivery_route_id (FK)

delivery_routes
├── id (PK)
├── vehicle_id (FK)
├── created_at
└── completed_at
```

## Architecture Highlights

- **Clean Architecture**: Clear separation of concerns (Controller → Service → Repository)
- **Domain-Driven Design**: Rich domain entities with business logic
- **Exception Handling**: Centralized with GlobalExceptionHandler
- **DTO Pattern**: Separation of API contracts from domain models
- **MapStruct**: Type-safe, performant object mapping
- **Validation**: Jakarta Bean Validation at API boundary
- **Transaction Management**: @Transactional for data consistency

## Configuration

Database configuration in `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/logiroute
    username: logiroute
    password: logiroute123
```

## Stopping the Application

```bash
# Stop Spring Boot
Ctrl+C

# Stop PostgreSQL
docker-compose down
```

## Future Enhancements

- Route optimization algorithms (TSP, genetic algorithms)
- Real-time vehicle tracking
- Multi-depot support
- Time window constraints
- Driver management
- REST API documentation (Swagger/OpenAPI)
- Integration tests
- Performance monitoring
