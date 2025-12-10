-- Create user and database for LogiRoute application
-- This script runs as the postgres superuser during initialization

-- Create the logiroute role/user
CREATE ROLE logiroute WITH LOGIN PASSWORD 'logiroute123';

-- Create the logiroute database owned by logiroute user
CREATE DATABASE logiroute OWNER logiroute;

-- Grant all privileges
GRANT ALL PRIVILEGES ON DATABASE logiroute TO logiroute;

SELECT 'LogiRoute database and user created successfully!' AS message;
