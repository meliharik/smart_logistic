#!/bin/bash

BASE_URL="http://localhost:8080/api"

echo "======================================"
echo "LogiRoute API Testing Script"
echo "======================================"
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}1. Fetching all vehicles...${NC}"
curl -s "${BASE_URL}/vehicles" | jq '.'
echo ""
echo ""

echo -e "${BLUE}2. Fetching all packages...${NC}"
curl -s "${BASE_URL}/packages" | jq '.'
echo ""
echo ""

echo -e "${GREEN}3. TEST: Assign packages to vehicle (SUCCESS - within capacity)${NC}"
echo "   Assigning packages 4, 2, 5 to vehicle 1 (total ~950kg < 1000kg)"
curl -s -X POST "${BASE_URL}/delivery/assign" \
  -H "Content-Type: application/json" \
  -d '{
    "vehicleId": 1,
    "packageIds": [4, 2, 5]
  }' | jq '.'
echo ""
echo ""

echo -e "${BLUE}4. View active routes...${NC}"
curl -s "${BASE_URL}/delivery/routes" | jq '.'
echo ""
echo ""

echo -e "${RED}5. TEST: Try to overload vehicle (SHOULD FAIL)${NC}"
echo "   Trying to assign package 1 (150kg) to vehicle 1 (only ~50kg remaining)"
curl -s -X POST "${BASE_URL}/delivery/assign" \
  -H "Content-Type: application/json" \
  -d '{
    "vehicleId": 1,
    "packageIds": [1]
  }' | jq '.'
echo ""
echo ""

echo -e "${GREEN}6. TEST: Assign remaining packages to vehicle 2${NC}"
echo "   Assigning packages 1, 3 to vehicle 2 (total ~450kg < 1500kg)"
curl -s -X POST "${BASE_URL}/delivery/assign" \
  -H "Content-Type: application/json" \
  -d '{
    "vehicleId": 2,
    "packageIds": [1, 3]
  }' | jq '.'
echo ""
echo ""

echo -e "${BLUE}7. Check package status...${NC}"
curl -s "${BASE_URL}/packages/1" | jq '.'
echo ""
echo ""

echo -e "${RED}8. TEST: Invalid status transition (SHOULD FAIL)${NC}"
echo "   Trying to mark LOADED package directly as DELIVERED (skipping state)"
echo "   Creating a new package first..."
NEW_PKG=$(curl -s -X POST "${BASE_URL}/packages" \
  -H "Content-Type: application/json" \
  -d '{
    "deliveryAddress": "Test Address",
    "weightKg": 50.0,
    "status": "CREATED",
    "deliveryDeadline": "2025-12-11T12:00:00"
  }' | jq -r '.id')

echo "   New package ID: $NEW_PKG"
echo "   Trying invalid transition: CREATED -> DELIVERED"
curl -s -X PATCH "${BASE_URL}/packages/${NEW_PKG}/status" \
  -H "Content-Type: application/json" \
  -d '{"status": "DELIVERED"}' | jq '.'
echo ""
echo ""

echo -e "${GREEN}9. TEST: Valid status transition${NC}"
echo "   CREATED -> LOADED (valid)"
curl -s -X PATCH "${BASE_URL}/packages/${NEW_PKG}/status" \
  -H "Content-Type: application/json" \
  -d '{"status": "LOADED"}' | jq '.'
echo ""
echo ""

echo -e "${BLUE}10. View all active routes${NC}"
curl -s "${BASE_URL}/delivery/routes" | jq '.'
echo ""
echo ""

echo "======================================"
echo "✅ Testing Complete!"
echo "======================================"
