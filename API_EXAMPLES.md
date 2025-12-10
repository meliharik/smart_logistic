# LogiRoute - API Examples & Testing Guide

## Quick Start

```bash
# 1. Start PostgreSQL
docker-compose up -d

# 2. Run the application
mvn spring-boot:run

# 3. Application starts on http://localhost:8080
```

## Base URL
```
http://localhost:8080/api
```

---

## 1. Vehicle Management

### Get All Vehicles
```bash
curl http://localhost:8080/api/vehicles | jq '.'
```

**Response:**
```json
[
  {
    "id": 1,
    "licensePlate": "ABC-1234",
    "capacityKg": 1000.0,
    "currentLoadKg": 0.0,
    "status": "AVAILABLE",
    "remainingCapacityKg": 1000.0
  },
  {
    "id": 2,
    "licensePlate": "XYZ-5678",
    "capacityKg": 1500.0,
    "currentLoadKg": 0.0,
    "status": "AVAILABLE",
    "remainingCapacityKg": 1500.0
  }
]
```

### Get Available Vehicles
```bash
curl http://localhost:8080/api/vehicles/available | jq '.'
```

### Create Vehicle
```bash
curl -X POST http://localhost:8080/api/vehicles \
  -H "Content-Type: application/json" \
  -d '{
    "licensePlate": "DEF-9999",
    "capacityKg": 2000.0,
    "status": "AVAILABLE"
  }' | jq '.'
```

---

## 2. Package Management

### Get All Packages
```bash
curl http://localhost:8080/api/packages | jq '.'
```

**Response:**
```json
[
  {
    "id": 1,
    "deliveryAddress": "123 Main St, New York, NY 10001",
    "weightKg": 150.0,
    "status": "CREATED",
    "deliveryDeadline": "2025-12-10T15:00:00",
    "deliveryRouteId": null
  },
  {
    "id": 2,
    "deliveryAddress": "456 Oak Ave, Los Angeles, CA 90001",
    "weightKg": 250.0,
    "status": "CREATED",
    "deliveryDeadline": "2025-12-10T13:00:00",
    "deliveryRouteId": null
  }
]
```

### Get Unassigned Packages
```bash
curl http://localhost:8080/api/packages/unassigned | jq '.'
```

### Get Packages by Status
```bash
# Get all CREATED packages
curl http://localhost:8080/api/packages/status/CREATED | jq '.'

# Get all LOADED packages
curl http://localhost:8080/api/packages/status/LOADED | jq '.'

# Get all DELIVERED packages
curl http://localhost:8080/api/packages/status/DELIVERED | jq '.'
```

### Create Package
```bash
curl -X POST http://localhost:8080/api/packages \
  -H "Content-Type: application/json" \
  -d '{
    "deliveryAddress": "789 Elm St, Chicago, IL 60601",
    "weightKg": 100.0,
    "status": "CREATED",
    "deliveryDeadline": "2025-12-11T10:00:00"
  }' | jq '.'
```

---

## 3. Delivery Operations

### Assign Packages to Vehicle

#### ✅ SUCCESS Example - Within Capacity
```bash
curl -X POST http://localhost:8080/api/delivery/assign \
  -H "Content-Type: application/json" \
  -d '{
    "vehicleId": 1,
    "packageIds": [4, 2, 5]
  }' | jq '.'
```

**Request Details:**
- Vehicle: ABC-1234 (1000kg capacity)
- Packages: 4 (500kg), 2 (250kg), 5 (200kg)
- Total: 950kg < 1000kg ✓

**Response:**
```json
{
  "id": 1,
  "vehicleId": 1,
  "vehicleLicensePlate": "ABC-1234",
  "packages": [
    {
      "id": 4,
      "weightKg": 500.0,
      "status": "LOADED",
      "deliveryDeadline": "2025-12-10T12:00:00"
    },
    {
      "id": 2,
      "weightKg": 250.0,
      "status": "LOADED",
      "deliveryDeadline": "2025-12-10T13:00:00"
    },
    {
      "id": 5,
      "weightKg": 200.0,
      "status": "LOADED",
      "deliveryDeadline": "2025-12-10T14:00:00"
    }
  ],
  "createdAt": "2025-12-10T11:30:00",
  "completedAt": null,
  "totalWeight": 950.0
}
```

**Note:** Packages are automatically sorted by `deliveryDeadline` (earliest first)

#### ❌ FAILURE Example - Exceeds Capacity
```bash
curl -X POST http://localhost:8080/api/delivery/assign \
  -H "Content-Type: application/json" \
  -d '{
    "vehicleId": 1,
    "packageIds": [1, 3]
  }' | jq '.'
```

**Request Details:**
- Vehicle: ABC-1234 (remaining capacity: 50kg after previous assignment)
- Packages: 1 (150kg), 3 (300kg)
- Total: 450kg > 50kg ✗

**Response (HTTP 400):**
```json
{
  "timestamp": "2025-12-10T11:35:00",
  "status": 400,
  "error": "Vehicle Overload",
  "message": "Vehicle 'ABC-1234' cannot load package of 450.00 kg. Remaining capacity: 50.00 kg",
  "path": "/api/delivery/assign"
}
```

### Get Active Routes
```bash
curl http://localhost:8080/api/delivery/routes | jq '.'
```

### Get Route by ID
```bash
curl http://localhost:8080/api/delivery/routes/1 | jq '.'
```

### Get Routes by Vehicle
```bash
curl http://localhost:8080/api/delivery/routes/vehicle/1 | jq '.'
```

### Complete Route
```bash
curl -X PATCH http://localhost:8080/api/delivery/routes/1/complete | jq '.'
```

**Effect:**
- Sets `completedAt` timestamp
- Updates vehicle status to `AVAILABLE`
- Reduces vehicle `currentLoadKg` to 0

---

## 4. Package Status Updates

### ✅ Valid Transition: CREATED → LOADED
```bash
# Get a CREATED package
PACKAGE_ID=1

curl -X PATCH http://localhost:8080/api/packages/${PACKAGE_ID}/status \
  -H "Content-Type: application/json" \
  -d '{"status": "LOADED"}' | jq '.'
```

**Response:**
```json
{
  "id": 1,
  "deliveryAddress": "123 Main St, New York, NY 10001",
  "weightKg": 150.0,
  "status": "LOADED",
  "deliveryDeadline": "2025-12-10T15:00:00"
}
```

### ✅ Valid Transition: LOADED → DELIVERED
```bash
PACKAGE_ID=1

curl -X PATCH http://localhost:8080/api/packages/${PACKAGE_ID}/status \
  -H "Content-Type: application/json" \
  -d '{"status": "DELIVERED"}' | jq '.'
```

### ❌ Invalid Transition: CREATED → DELIVERED
```bash
PACKAGE_ID=1

curl -X PATCH http://localhost:8080/api/packages/${PACKAGE_ID}/status \
  -H "Content-Type: application/json" \
  -d '{"status": "DELIVERED"}' | jq '.'
```

**Response (HTTP 400):**
```json
{
  "timestamp": "2025-12-10T11:40:00",
  "status": 400,
  "error": "Invalid Status Transition",
  "message": "Package ID 1 cannot transition from CREATED to DELIVERED. Invalid state transition.",
  "path": "/api/packages/1/status"
}
```

### ❌ Invalid Transition: LOADED → CREATED (Backward)
```bash
curl -X PATCH http://localhost:8080/api/packages/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "CREATED"}' | jq '.'
```

**Response (HTTP 400):**
```json
{
  "timestamp": "2025-12-10T11:42:00",
  "status": 400,
  "error": "Invalid Status Transition",
  "message": "Package ID 1 cannot transition from LOADED to CREATED. Invalid state transition.",
  "path": "/api/packages/1/status"
}
```

### ❌ Invalid Transition: DELIVERED → * (Final State)
```bash
curl -X PATCH http://localhost:8080/api/packages/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "LOADED"}' | jq '.'
```

**Response (HTTP 400):**
```json
{
  "timestamp": "2025-12-10T11:45:00",
  "status": 400,
  "error": "Invalid Status Transition",
  "message": "Package ID 1 cannot transition from DELIVERED to LOADED. Invalid state transition.",
  "path": "/api/packages/1/status"
}
```

---

## 5. Complete Testing Scenario

### Step-by-Step Workflow

```bash
# 1. Check initial state
echo "=== Step 1: Get all vehicles ==="
curl -s http://localhost:8080/api/vehicles | jq '.[] | {licensePlate, capacityKg, currentLoadKg, status}'

echo ""
echo "=== Step 2: Get all packages ==="
curl -s http://localhost:8080/api/packages | jq '.[] | {id, weightKg, status, deliveryDeadline}'

# 2. Assign packages to vehicle 1
echo ""
echo "=== Step 3: Assign packages 4, 2, 5 to vehicle 1 ==="
curl -s -X POST http://localhost:8080/api/delivery/assign \
  -H "Content-Type: application/json" \
  -d '{"vehicleId": 1, "packageIds": [4, 2, 5]}' | jq '.'

# 3. Check vehicle status
echo ""
echo "=== Step 4: Check vehicle 1 status ==="
curl -s http://localhost:8080/api/vehicles/1 | jq '{licensePlate, currentLoadKg, remainingCapacityKg, status}'

# 4. Try to overload (should fail)
echo ""
echo "=== Step 5: Try to assign package 1 to vehicle 1 (should fail) ==="
curl -s -X POST http://localhost:8080/api/delivery/assign \
  -H "Content-Type: application/json" \
  -d '{"vehicleId": 1, "packageIds": [1]}' | jq '.'

# 5. Assign to vehicle 2
echo ""
echo "=== Step 6: Assign packages 1, 3 to vehicle 2 ==="
curl -s -X POST http://localhost:8080/api/delivery/assign \
  -H "Content-Type: application/json" \
  -d '{"vehicleId": 2, "packageIds": [1, 3]}' | jq '.'

# 6. View all active routes
echo ""
echo "=== Step 7: View all active routes ==="
curl -s http://localhost:8080/api/delivery/routes | jq '.[] | {id, vehicleLicensePlate, totalWeight, packageCount: (.packages | length)}'

# 7. Complete route 1
echo ""
echo "=== Step 8: Complete route 1 ==="
curl -s -X PATCH http://localhost:8080/api/delivery/routes/1/complete | jq '.'

# 8. Check vehicle 1 status again
echo ""
echo "=== Step 9: Check vehicle 1 status after completion ==="
curl -s http://localhost:8080/api/vehicles/1 | jq '{licensePlate, currentLoadKg, remainingCapacityKg, status}'
```

---

## 6. Error Handling Examples

### Validation Error
```bash
# Missing required field
curl -X POST http://localhost:8080/api/packages \
  -H "Content-Type: application/json" \
  -d '{
    "weightKg": 100.0
  }' | jq '.'
```

**Response (HTTP 400):**
```json
{
  "timestamp": "2025-12-10T11:50:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Request validation failed. Please check the errors.",
  "path": "/api/packages",
  "validationErrors": {
    "deliveryAddress": "Delivery address is required",
    "deliveryDeadline": "Delivery deadline is required"
  }
}
```

### Resource Not Found
```bash
curl http://localhost:8080/api/vehicles/999 | jq '.'
```

**Response (HTTP 404):**
```json
{
  "timestamp": "2025-12-10T11:52:00",
  "status": 404,
  "error": "Resource Not Found",
  "message": "Vehicle with ID 999 not found",
  "path": "/api/vehicles/999"
}
```

---

## 7. Using the Automated Test Script

```bash
chmod +x test-api.sh
./test-api.sh
```

This script automatically tests:
1. ✅ Listing vehicles and packages
2. ✅ Successful package assignment (capacity guard pass)
3. ✅ Failed assignment due to capacity (capacity guard fail)
4. ✅ Invalid status transitions (state machine fail)
5. ✅ Valid status transitions (state machine pass)
6. ✅ Route management

---

## 8. Performance Testing

### Bulk Operations
```bash
# Create multiple packages
for i in {1..10}; do
  curl -X POST http://localhost:8080/api/packages \
    -H "Content-Type: application/json" \
    -d "{
      \"deliveryAddress\": \"Address $i\",
      \"weightKg\": $((RANDOM % 100 + 50)),
      \"status\": \"CREATED\",
      \"deliveryDeadline\": \"2025-12-11T12:00:00\"
    }"
done
```

---

## 9. Monitoring Endpoints

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

*(Note: Actuator endpoint requires spring-boot-starter-actuator dependency)*

---

## Expected Test Results

### Capacity Guard Tests
| Test | Vehicle Capacity | Load Attempt | Expected Result |
|------|-----------------|--------------|-----------------|
| Test 1 | 1000kg | 950kg | ✅ SUCCESS |
| Test 2 | 50kg remaining | 300kg | ❌ VehicleOverloadedException |
| Test 3 | 1000kg | 1050kg | ❌ VehicleOverloadedException |

### State Machine Tests
| Current Status | Target Status | Expected Result |
|----------------|---------------|-----------------|
| CREATED | LOADED | ✅ SUCCESS |
| LOADED | DELIVERED | ✅ SUCCESS |
| CREATED | DELIVERED | ❌ InvalidStatusTransitionException |
| LOADED | CREATED | ❌ InvalidStatusTransitionException |
| DELIVERED | * | ❌ InvalidStatusTransitionException |

### Smart Routing Tests
| Packages | Deadlines | Expected Order |
|----------|-----------|----------------|
| [4, 2, 5] | [+1h, +2h, +3h] | [4, 2, 5] (sorted by deadline) |
| [1, 3, 2] | [+4h, +6h, +2h] | [2, 1, 3] (sorted by deadline) |

---

## Tips

1. **Use `jq` for JSON formatting** (install: `brew install jq`)
2. **Save responses to variables** for chaining requests
3. **Use `-v` flag** to see full HTTP response headers
4. **Check logs** in the application console for detailed error information

---

## Troubleshooting

### PostgreSQL not running
```bash
docker-compose up -d
docker-compose ps
```

### Application won't start
```bash
# Check if port 8080 is in use
lsof -i :8080

# View application logs
mvn spring-boot:run
```

### Database connection errors
```bash
# Reset database
docker-compose down -v
docker-compose up -d
mvn spring-boot:run
```
