# LogiRoute - Logistics Optimization System

Enterprise-grade Spring Boot 3 logistics optimization system with intelligent package routing, capacity management, and state machine validation.

## 🚀 Key Features

### 1. **Capacity Guard** - Vehicle Overload Prevention
Validates vehicle capacity before package assignment:
- Checks: `totalWeight + currentLoad <= capacity`
- Throws `VehicleOverloadedException` when exceeded
- **Implementation:** `DeliveryService.java:47-56`

### 2. **State Machine** - Package Status Validation
Enforces valid package status transitions:
- Valid flow: `CREATED → LOADED → DELIVERED`
- Prevents invalid transitions
- **Implementation:** `Package.java:55-65`

### 3. **Smart Routing** - Deadline-Based Optimization
Automatically sorts packages by delivery deadline:
- Earliest deadline first
- Optimizes delivery efficiency
- **Implementation:** `DeliveryService.java:58-61`

## 📋 Tech Stack

- **Java 17+**
- **Spring Boot 3.2.1**
- **Spring Data JPA** + PostgreSQL 16
- **Lombok** + **MapStruct**
- **Jakarta Validation**
- **Docker Compose**
- **JUnit 5** + **Mockito**

## 🏗️ Architecture

```
Controller → Service → Repository → Database
    ↓          ↓          ↓
   DTO    Business     JPA
          Logic     Entities
```

**Layers:**
- **Controller:** REST API endpoints
- **Service:** Business logic (Capacity Guard, State Machine, Routing)
- **Repository:** Data access with Spring Data JPA
- **Entity:** Domain models with business methods

## ⚡ Quick Start

### 1. Verify Prerequisites

```bash
# Check Java version (must be 17+)
java -version

# Check Maven
mvn -version

# Check Docker
docker --version
```

### 2. Start PostgreSQL

```bash
# Start PostgreSQL (creates database and user automatically)
docker-compose up -d

# Wait for PostgreSQL to be ready (10-15 seconds)
sleep 15

# Verify database connection
docker exec logiroute-postgres psql -U logiroute -d logiroute -c "SELECT current_database();"
```

**Expected Output:** `logiroute`

### 3. Run Tests (Verify Code Works)

```bash
# Run all tests
mvn test
```

**Expected Output:**
```
Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
✅ BUILD SUCCESS
```

### 4. Run the Application

```bash
# Start application (will take 30-40 seconds first time)
mvn spring-boot:run
```

**Wait for this message:**
```
Started LogiRouteApplication in X.XXX seconds
```

**Application starts on:** `http://localhost:8080`

**Seed Data Automatically Loaded:**
- 2 vehicles: ABC-1234 (1000kg), XYZ-5678 (1500kg)
- 5 packages with various weights and deadlines

### 5. Test the API (Open a New Terminal)

```bash
# Get all vehicles
curl http://localhost:8080/api/vehicles

# Get all packages
curl http://localhost:8080/api/packages
```

## 🧪 Testing the Business Logic

### Test 1: Capacity Guard (SUCCESS)

Assign packages within capacity:

```bash
curl -X POST http://localhost:8080/api/delivery/assign \
  -H "Content-Type: application/json" \
  -d '{
    "vehicleId": 1,
    "packageIds": [4, 2, 5]
  }'
```

**Expected:** 200 OK, packages sorted by deadline

### Test 2: Capacity Guard (FAILURE)

Try to exceed vehicle capacity:

```bash
curl -X POST http://localhost:8080/api/delivery/assign \
  -H "Content-Type: application/json" \
  -d '{
    "vehicleId": 1,
    "packageIds": [1, 3]
  }'
```

**Expected:** 400 Bad Request
```json
{
  "status": 400,
  "error": "Vehicle Overload",
  "message": "Vehicle 'ABC-1234' cannot load ..."
}
```

### Test 3: State Machine (INVALID TRANSITION)

Try invalid status transition (CREATED → DELIVERED):

```bash
curl -X PATCH http://localhost:8080/api/packages/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "DELIVERED"}'
```

**Expected:** 400 Bad Request
```json
{
  "status": 400,
  "error": "Invalid Status Transition",
  "message": "Package ID 1 cannot transition from CREATED to DELIVERED"
}
```

### Test 4: State Machine (VALID TRANSITION)

Valid transition (CREATED → LOADED):

```bash
curl -X PATCH http://localhost:8080/api/packages/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "LOADED"}'
```

**Expected:** 200 OK

## 📚 API Endpoints

### Vehicles
- `GET /api/vehicles` - List all vehicles
- `GET /api/vehicles/{id}` - Get vehicle by ID
- `GET /api/vehicles/available` - Get available vehicles
- `POST /api/vehicles` - Create vehicle
- `PUT /api/vehicles/{id}` - Update vehicle
- `DELETE /api/vehicles/{id}` - Delete vehicle

### Packages
- `GET /api/packages` - List all packages
- `GET /api/packages/{id}` - Get package by ID
- `GET /api/packages/unassigned` - Get unassigned packages
- `GET /api/packages/status/{status}` - Get by status (CREATED, LOADED, DELIVERED)
- `POST /api/packages` - Create package
- `PUT /api/packages/{id}` - Update package
- `PATCH /api/packages/{id}/status` - Update status (validates state machine)
- `DELETE /api/packages/{id}` - Delete package

### Delivery Operations
- `POST /api/delivery/assign` - Assign packages to vehicle (capacity guard)
- `GET /api/delivery/routes` - Get active routes
- `GET /api/delivery/routes/{id}` - Get route by ID
- `GET /api/delivery/routes/vehicle/{vehicleId}` - Get routes by vehicle
- `PATCH /api/delivery/routes/{id}/complete` - Complete route

## 🧪 Unit Tests

**12 comprehensive tests covering:**

✅ **Capacity Guard Tests:**
- Successful assignment within capacity
- Single package exceeds capacity
- Multiple packages exceed capacity

✅ **State Machine Tests:**
- Valid transitions (CREATED→LOADED, LOADED→DELIVERED)
- Invalid transitions prevented (CREATED→DELIVERED)
- Backward transitions prevented
- No transitions from DELIVERED state

✅ **Smart Routing Tests:**
- Packages sorted by earliest deadline

✅ **Additional Tests:**
- Resource not found handling
- Route completion
- Package state validation

**Run tests:**
```bash
mvn test
```

## 📂 Project Structure

```
src/main/java/com/logistics/logiroute/
├── config/
│   └── DataLoader.java              # Seed data
├── controller/
│   ├── DeliveryController.java      # Delivery operations
│   ├── PackageController.java       # Package CRUD
│   └── VehicleController.java       # Vehicle CRUD
├── domain/
│   ├── entity/
│   │   ├── DeliveryRoute.java       # Route entity
│   │   ├── Package.java             # Package entity (State Machine logic)
│   │   └── Vehicle.java             # Vehicle entity (Capacity logic)
│   └── enums/
│       ├── PackageStatus.java       # CREATED, LOADED, DELIVERED
│       └── VehicleStatus.java       # AVAILABLE, IN_TRANSIT
├── dto/                             # Data Transfer Objects
├── exception/
│   ├── GlobalExceptionHandler.java  # Centralized error handling
│   ├── VehicleOverloadedException.java
│   ├── InvalidStatusTransitionException.java
│   └── ResourceNotFoundException.java
├── mapper/                          # MapStruct mappers
├── repository/                      # Spring Data JPA
└── service/
    ├── DeliveryService.java         # Core business logic
    ├── PackageService.java
    └── VehicleService.java
```

## 💡 Key Implementation Details

### Capacity Guard Logic
```java
// src/main/java/com/logistics/logiroute/service/DeliveryService.java:47-56
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

### State Machine Validation
```java
// src/main/java/com/logistics/logiroute/domain/entity/Package.java:55-65
public boolean canTransitionTo(PackageStatus newStatus) {
    if (this.status == newStatus) return true;

    return switch (this.status) {
        case CREATED -> newStatus == PackageStatus.LOADED;
        case LOADED -> newStatus == PackageStatus.DELIVERED;
        case DELIVERED -> false;
    };
}
```

### Smart Routing Algorithm
```java
// src/main/java/com/logistics/logiroute/service/DeliveryService.java:58-61
List<Package> sortedPackages = packages.stream()
    .sorted(Comparator.comparing(Package::getDeliveryDeadline))
    .toList();
```

## 🛠️ Configuration

**Database:** `src/main/resources/application.yml`
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/logiroute
    username: logiroute
    password: logiroute123
```

**Test Database:** `src/test/resources/application-test.yml`
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb  # In-memory H2 for tests
```

## 🐳 Docker Commands

```bash
# Start PostgreSQL
docker-compose up -d

# Stop PostgreSQL
docker-compose down

# Reset database (delete all data)
docker-compose down -v

# View PostgreSQL logs
docker logs logiroute-postgres

# Connect to PostgreSQL
docker exec -it logiroute-postgres psql -U logiroute -d logiroute
```

## 🔧 Maven Commands

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Run specific test class
mvn test -Dtest=DeliveryServiceTest

# Run application
mvn spring-boot:run

# Package as JAR
mvn clean package

# Skip tests during build
mvn clean package -DskipTests
```

## 📊 Testing Scenarios (Pre-loaded Data)

The `DataLoader` creates the following test scenarios:

| Scenario | Details | Expected Result |
|----------|---------|----------------|
| **1. Success** | Assign packages [4,2,5] to ABC-1234<br>Total: 950kg < 1000kg | ✅ SUCCESS<br>Sorted by deadline |
| **2. Failure** | Assign package 3 (300kg) to ABC-1234<br>Remaining: 50kg < 300kg | ❌ VehicleOverloadedException |
| **3. Success** | Assign packages [1,3] to XYZ-5678<br>Total: 450kg < 1500kg | ✅ SUCCESS |

## 🏆 Architecture Highlights

- ✅ **Clean Architecture** - Layered design with clear separation
- ✅ **Domain-Driven Design** - Rich domain models
- ✅ **SOLID Principles** - Single responsibility, dependency inversion
- ✅ **Exception Handling** - Global handler with proper HTTP status codes
- ✅ **DTO Pattern** - API/domain separation with MapStruct
- ✅ **Transaction Management** - @Transactional for ACID guarantees
- ✅ **Validation** - Jakarta Bean Validation at API boundaries
- ✅ **Testing** - Comprehensive unit tests with Mockito

## ❓ Troubleshooting

### 1. Application Won't Start - "role logiroute does not exist"

**Problem:** PostgreSQL didn't create the user properly.

**Solution:**
```bash
# 1. Stop everything
docker-compose down -v

# 2. Start PostgreSQL fresh
docker-compose up -d

# 3. Wait for initialization
sleep 15

# 4. Verify user exists
docker exec logiroute-postgres psql -U logiroute -d logiroute -c "SELECT current_user;"

# 5. If above works, start application
mvn spring-boot:run
```

### 2. Port 8080 Already in Use

```bash
# Find process using port 8080
lsof -i :8080

# Kill the process (replace PID with actual number)
kill -9 <PID>

# Or kill all Java processes
pkill -9 java
```

### 3. Tests Failing

```bash
# Clean everything and rebuild
mvn clean compile

# Run tests
mvn test

# If still failing, check output
mvn test -X
```

### 4. PostgreSQL Won't Start

```bash
# Check Docker is running
docker ps

# View PostgreSQL logs
docker logs logiroute-postgres

# Restart Docker Desktop and try again
docker-compose down -v
docker-compose up -d
```

### 5. Complete Reset (Nuclear Option)

```bash
# Stop and remove everything
docker-compose down -v
pkill -9 java
mvn clean

# Start fresh
docker-compose up -d
sleep 15
mvn test
mvn spring-boot:run
```

## 📈 Future Enhancements

- [ ] Route optimization algorithms (TSP, genetic algorithms)
- [ ] Real-time vehicle tracking with WebSockets
- [ ] Multi-depot support
- [ ] Time window constraints
- [ ] Driver management module
- [ ] REST API documentation (Swagger/OpenAPI)
- [ ] Integration tests
- [ ] Performance monitoring (Spring Boot Actuator + Micrometer)
- [ ] Caching layer (Redis)
- [ ] Event-driven architecture (Kafka/RabbitMQ)

## 📄 License

This is an educational project for demonstrating Spring Boot architecture and logistics optimization concepts.

---

**Built with ❤️ using Spring Boot 3, Clean Architecture, and Domain-Driven Design**
