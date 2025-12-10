#!/bin/bash

echo "======================================"
echo "LogiRoute - Starting Application"
echo "======================================"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker Desktop."
    exit 1
fi

echo "✓ Docker is running"

# Start PostgreSQL
echo ""
echo "🐘 Starting PostgreSQL..."
docker-compose up -d

# Wait for PostgreSQL to be ready
echo "⏳ Waiting for PostgreSQL to be ready..."
sleep 5

# Run the application
echo ""
echo "🚀 Starting LogiRoute application..."
echo ""
mvn spring-boot:run
