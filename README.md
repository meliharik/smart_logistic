# 🚚 LogiRoute - Logistics Optimization System

<div align="center">

![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)

**Enterprise-grade logistics management system with intelligent package routing, capacity optimization, and state machine validation.**

[Features](#-features) •
[Demo](#-demo) •
[Quick Start](#-quick-start) •
[Architecture](#-architecture) •
[API Documentation](#-api-documentation) •
[Contributing](#-contributing)

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Key Features](#-features)
- [Demo](#-demo)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Quick Start](#-quick-start)
- [API Documentation](#-api-documentation)
- [Project Structure](#-project-structure)
- [Testing](#-testing)
- [Troubleshooting](#-troubleshooting)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🎯 Overview

**LogiRoute** is a production-ready logistics optimization system built with **Spring Boot 3** and **Clean Architecture** principles. It demonstrates enterprise software development best practices including:

- ✅ **Domain-Driven Design (DDD)** - Rich domain models with business logic
- ✅ **Clean Architecture** - Layered design with clear separation of concerns
- ✅ **SOLID Principles** - Maintainable and extensible codebase
- ✅ **Test-Driven Development** - Comprehensive unit tests (12 tests, 100% pass rate)
- ✅ **RESTful API Design** - Industry-standard REST endpoints
- ✅ **Transaction Management** - ACID guarantees with Spring @Transactional

---

## ✨ Features

### 1. 🛡️ Capacity Guard - Vehicle Overload Prevention

Validates vehicle capacity before package assignment to prevent overloading.

```java
// Validation Logic: src/main/java/.../service/DeliveryService.java:47-56
if (!vehicle.canLoad(totalPackageWeight)) {
    throw VehicleOverloadedException.forPackage(
        vehicle.getLicensePlate(),
        totalPackageWeight,
        vehicle.getRemainingCapacityKg()
    );
}
```

**Business Rule:** `totalWeight + currentLoad <= vehicleCapacity`

### 2. 🔄 State Machine - Package Status Validation

Enforces valid package status transitions to maintain data integrity.

```java
// State Machine: src/main/java/.../domain/entity/Package.java:55-65
public boolean canTransitionTo(PackageStatus newStatus) {
    return switch (this.status) {
        case CREATED -> newStatus == PackageStatus.LOADED;
        case LOADED -> newStatus == PackageStatus.DELIVERED;
        case DELIVERED -> false; // Terminal state
    };
}
```

**Valid Flow:** `CREATED → LOADED → DELIVERED` (no skipping allowed)

### 3. 🎯 Smart Routing - Deadline-Based Optimization

Automatically sorts packages by delivery deadline for optimal route planning.

```java
// Routing Algorithm: src/main/java/.../service/DeliveryService.java:58-61
List<Package> sortedPackages = packages.stream()
    .sorted(Comparator.comparing(Package::getDeliveryDeadline))
    .toList();
```

**Result:** Earliest deadline packages are delivered first.

---

## 🎨 Demo

### Web UI
Access the modern web interface at **http://localhost:8080** after starting the application.

![LogiRoute Dashboard](https://via.placeholder.com/800x400/667eea/ffffff?text=LogiRoute+Dashboard)

### API Examples

#### ✅ SUCCESS: Assign packages within capacity
```bash
curl -X POST http://localhost:8080/api/delivery/assign \
  -H "Content-Type: application/json" \
  -d '{
    "vehicleId": 1,
    "packageIds": [4, 2, 5]
  }'

# Response: 200 OK (packages sorted by deadline)
```

#### ❌ FAILURE: Capacity Guard prevents overload
```bash
curl -X POST http://localhost:8080/api/delivery/assign \
  -H "Content-Type: application/json" \
  -d '{
    "vehicleId": 1,
    "packageIds": [1, 3]
  }'

# Response: 400 Bad Request
{
  "status": 400,
  "error": "Vehicle Overload",
  "message": "Vehicle 'ABC-1234' cannot load package of 450.00 kg..."
}
```

#### ❌ FAILURE: State Machine prevents invalid transition
```bash
curl -X PATCH http://localhost:8080/api/packages/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "DELIVERED"}'

# Response: 400 Bad Request
{
  "status": 400,
  "error": "Invalid Status Transition",
  "message": "Package cannot transition from CREATED to DELIVERED"
}
```

---

## 🛠 Tech Stack

| Category | Technology |
|----------|-----------|
| **Language** | Java 17+ |
| **Framework** | Spring Boot 3.2.1 |
| **Database** | PostgreSQL 16 (Docker) |
| **ORM** | Spring Data JPA + Hibernate |
| **Validation** | Jakarta Bean Validation |
| **Mapping** | MapStruct + Lombok |
| **Testing** | JUnit 5 + Mockito |
| **Build Tool** | Maven 3.6+ |
| **Containerization** | Docker & Docker Compose |

---

## 🏗 Architecture

### Layered Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Controller Layer                      │
│        (REST API Endpoints + DTO Validation)            │
└─────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│                    Service Layer                         │
│   (Business Logic: Capacity Guard, State Machine)       │
└─────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│                  Repository Layer                        │
│           (Spring Data JPA Repositories)                 │
└─────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│                   Database Layer                         │
│                (PostgreSQL 16)                          │
└─────────────────────────────────────────────────────────┘
```

### Domain Model

```
┌─────────────┐         ┌──────────────┐        ┌─────────────┐
│   Vehicle   │◄────────│DeliveryRoute │────────►│   Package   │
├─────────────┤         ├──────────────┤        ├─────────────┤
│ id          │         │ id           │        │ id          │
│ licensePlate│         │ vehicle      │        │ address     │
│ capacityKg  │         │ packages[]   │        │ weightKg    │
│ currentLoad │         │ createdAt    │        │ status      │
│ status      │         │ completedAt  │        │ deadline    │
└─────────────┘         └──────────────┘        └─────────────┘
```

---

## 🚀 Quick Start

### Prerequisites

Ensure you have the following installed:

```bash
java -version    # Java 17 or higher
mvn -version     # Maven 3.6+
docker --version # Docker Desktop
```

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/logiroute.git
cd logiroute
```

### 2. Start PostgreSQL

```bash
# Start PostgreSQL container
docker-compose up -d

# Verify database is ready (wait 10-15 seconds)
docker exec logiroute-postgres psql -U postgres -d logiroute -c "SELECT 1"
```

**Note:** If you have a local PostgreSQL running on port 5432, stop it first:
```bash
brew services stop postgresql@14  # macOS
# or
sudo systemctl stop postgresql    # Linux
```

### 3. Run Tests (Optional but Recommended)

```bash
mvn test

# Expected output: Tests run: 12, Failures: 0, Errors: 0
```

### 4. Start the Application

```bash
mvn spring-boot:run
```

**Wait for:** `Started LogiRouteApplication in X.XXX seconds`

### 5. Access the Application

- **Web UI:** http://localhost:8080
- **API Base URL:** http://localhost:8080/api
- **Health Check:** http://localhost:8080/actuator/health

### Seed Data

The application automatically loads test data:
- **2 vehicles:** ABC-1234 (1000kg), XYZ-5678 (1500kg)
- **5 packages:** Various weights and deadlines

---

## 📚 API Documentation

### Vehicles

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/vehicles` | List all vehicles |
| GET | `/api/vehicles/{id}` | Get vehicle by ID |
| GET | `/api/vehicles/available` | Get available vehicles |
| POST | `/api/vehicles` | Create new vehicle |
| PUT | `/api/vehicles/{id}` | Update vehicle |
| DELETE | `/api/vehicles/{id}` | Delete vehicle |

### Packages

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/packages` | List all packages |
| GET | `/api/packages/{id}` | Get package by ID |
| GET | `/api/packages/unassigned` | Get unassigned packages |
| GET | `/api/packages/status/{status}` | Filter by status |
| POST | `/api/packages` | Create new package |
| PUT | `/api/packages/{id}` | Update package |
| PATCH | `/api/packages/{id}/status` | Update status (validates state machine) |
| DELETE | `/api/packages/{id}` | Delete package |

### Delivery Operations

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/delivery/assign` | Assign packages to vehicle (capacity guard) |
| GET | `/api/delivery/routes` | Get all active routes |
| GET | `/api/delivery/routes/{id}` | Get route by ID |
| GET | `/api/delivery/routes/vehicle/{vehicleId}` | Get routes by vehicle |
| PATCH | `/api/delivery/routes/{id}/complete` | Complete delivery route |

---

## 📂 Project Structure

```
src/main/java/com/logistics/logiroute/
├── config/
│   └── DataLoader.java              # Seed data configuration
├── controller/
│   ├── DeliveryController.java      # Delivery operations REST API
│   ├── PackageController.java       # Package CRUD operations
│   └── VehicleController.java       # Vehicle CRUD operations
├── domain/
│   ├── entity/
│   │   ├── DeliveryRoute.java       # Route entity
│   │   ├── Package.java             # Package entity (State Machine)
│   │   └── Vehicle.java             # Vehicle entity (Capacity logic)
│   └── enums/
│       ├── PackageStatus.java       # CREATED, LOADED, DELIVERED
│       └── VehicleStatus.java       # AVAILABLE, IN_TRANSIT
├── dto/                             # Data Transfer Objects
│   ├── request/                     # Request DTOs
│   └── response/                    # Response DTOs
├── exception/
│   ├── GlobalExceptionHandler.java  # Centralized error handling
│   ├── VehicleOverloadedException.java
│   ├── InvalidStatusTransitionException.java
│   └── ResourceNotFoundException.java
├── mapper/                          # MapStruct mappers
│   ├── DeliveryRouteMapper.java
│   ├── PackageMapper.java
│   └── VehicleMapper.java
├── repository/                      # Spring Data JPA repositories
│   ├── DeliveryRouteRepository.java
│   ├── PackageRepository.java
│   └── VehicleRepository.java
└── service/
    ├── DeliveryService.java         # Core business logic
    ├── PackageService.java
    └── VehicleService.java
```

---

## 🧪 Testing

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=DeliveryServiceTest
```

### Test Coverage

**12 comprehensive unit tests** covering:

✅ **Capacity Guard:**
- Success: Packages within capacity
- Failure: Single package exceeds capacity
- Failure: Multiple packages exceed capacity

✅ **State Machine:**
- Valid transitions (CREATED→LOADED, LOADED→DELIVERED)
- Invalid transition prevention (CREATED→DELIVERED)
- Backward transition prevention
- No transitions from DELIVERED state

✅ **Smart Routing:**
- Packages sorted by earliest deadline

✅ **Additional:**
- Resource not found handling
- Route completion
- Package state validation

---

## 🐛 Troubleshooting

### Issue: Application won't start - Port 8080 already in use

**Solution:**
```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>

# Or kill all Java processes
pkill -9 java
```

### Issue: PostgreSQL connection failed - "role does not exist"

**Solution:**
```bash
# Stop local PostgreSQL if running
brew services stop postgresql@14  # macOS
sudo systemctl stop postgresql    # Linux

# Reset Docker PostgreSQL
docker-compose down -v
docker-compose up -d
sleep 15

# Restart application
mvn spring-boot:run
```

### Issue: Tests failing

**Solution:**
```bash
# Clean and rebuild
mvn clean compile

# Run tests with detailed output
mvn test -X
```

### Issue: PostgreSQL container won't start

**Solution:**
```bash
# Check Docker is running
docker ps

# View logs
docker logs logiroute-postgres

# Complete reset
docker-compose down -v
docker-compose up -d
```

---

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

### Quick Contribution Steps

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Development Guidelines

- Follow the existing code style
- Write unit tests for new features
- Update documentation as needed
- Ensure all tests pass before submitting PR

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 📞 Contact & Support

- **Issues:** [GitHub Issues](https://github.com/yourusername/logiroute/issues)
- **Discussions:** [GitHub Discussions](https://github.com/yourusername/logiroute/discussions)

---

## 🌟 Acknowledgments

Built with:
- [Spring Boot](https://spring.io/projects/spring-boot) - Application framework
- [PostgreSQL](https://www.postgresql.org/) - Database
- [MapStruct](https://mapstruct.org/) - Bean mapping
- [Lombok](https://projectlombok.org/) - Boilerplate reduction

---

<div align="center">

**⭐ If you find this project useful, please consider giving it a star! ⭐**

Made with ❤️ using Spring Boot 3, Clean Architecture, and Domain-Driven Design

</div>
