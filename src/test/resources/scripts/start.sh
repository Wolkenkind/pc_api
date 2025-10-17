#!/bin/bash
set -e

echo "=== Starting Test Environment ==="
echo "Database: $DB_HOST:$DB_PORT"
echo "User: $DB_USER"
echo "Base URL: $BASE_URL"
echo "Loki URL: ${LOKI_URL:-Not set}"

echo "Waiting for database..."
while ! pg_isready -h $DB_HOST -p $DB_PORT -U $DB_USER; do
    echo 'Waiting for database...'
    sleep 2
done

echo "Database is accessible, waiting for schema..."
while ! PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d petclinic -c "SELECT 1 FROM information_schema.tables WHERE table_name='owners' LIMIT 1" >/dev/null 2>&1; do
    echo 'Waiting for database tables...'
    sleep 5
done

if [ -n "$LOKI_URL" ]; then
    echo "Loki URL is set, checking Loki availability..."

    echo "Waiting for Loki..."
    counter=0
    max_attempts=30
    while [ $counter -lt $max_attempts ]; do
        if [ "$(curl -s $LOKI_URL/ready 2>/dev/null)" = "ready" ]; then
                    echo "Loki is ready!"
                    break
                fi
        echo "Waiting for Loki to be ready... (attempt $((counter+1))/$max_attempts)"
        sleep 5
        counter=$((counter+1))
    done

    if [ $counter -eq $max_attempts ]; then
        echo "ERROR: Loki failed to become ready after $max_attempts attempts"
        exit 1
    fi

    echo "Loki is ready!"
else
    echo "Loki URL not set, skipping Loki checks"
fi

echo '=== All services are ready! Running tests... ==='
mvn test -DLOG_FORMAT=json -DENVIRONMENT=ci -DTEST_RUN_ID=${CI_PIPELINE_ID:-manual}