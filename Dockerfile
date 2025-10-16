FROM maven:3.9.9-eclipse-temurin-17-alpine

WORKDIR /app
COPY pom.xml .

RUN mvn dependency:go-offline -B
COPY src ./src

# needed to use pg_isready and checking for app start
RUN apk add --no-cache bash postgresql-client wget dos2unix
# Explicitly download surefire plugin
RUN mvn surefire:help -Ddetail=true > /dev/null 2>&1

COPY src/test/resources/scripts/start.sh ./start.sh
RUN dos2unix ./start.sh && chmod +x ./start.sh

ENV ENVIRONMENT=ci
ENV LOG_FORMAT=json
ENV TEST_FRAMEWORK=petclinic-ci-tests

# Wait for database and app to be ready, then run tests
CMD ["./start.sh"]