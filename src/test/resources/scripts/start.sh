#!/bin/bash
set -e

echo "Waiting for database..."
while ! pg_isready -h $DB_HOST -p $DB_PORT -U $DB_USER; do
    echo 'Waiting for database...'
    sleep 2
done

echo "Waiting for Petclinic application..."
while ! wget -q --spider $BASE_URL/api/owners; do
    echo 'Waiting for Petclinic application to fully start...'
    sleep 5
done

echo 'Application is ready! Running tests...'
mvn test -DLOG_FORMAT=json -DENVIRONMENT=ci -DTEST_RUN_ID=${CI_PIPELINE_ID:-manual}