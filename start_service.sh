#!/usr/bin/env bash
## Database Setup
#set -e
#echo "Dropping existing databases.."
#dropdb -U postgres -h localhost --if-exists location_tracking
#
#echo "Creating base databases"
#createdb -U postgres -h localhost  location_tracking


## Build Project
./gradlew clean build

# Start the service
java -Dspring.config.location=config/development/application.yml  -jar build/libs/demo-0.0.1-SNAPSHOT.jar
