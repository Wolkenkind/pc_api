# Automated API tests example for Spring PetClinic-REST project

This project exhibits automated API tests example written for the [Spring PetClinic-REST][1] project.

Tests were written using [JUnit 5][2] and [REST Assured][3], using [JavaFaker][4] for automatic creation of random test data. Other dependencies include:

* [Jackson library][5] for (de)serializing JSON
* [Spring JDBC][6] is used for work with JDBC templates, [Spring Tx][7] is added for possible transaction management
* [PostgreSQL driver][8] for direct work with [PostgreSQL][9] database
* [SLF4J][10] is used as logging facade with [Logback][11] as logging framework, employing [Logstash Logback Encoder][12] for possible data piping into [Logstash][13]
* [Grafana][14] dashboard with [Loki][15] log aggregation added as more lightweight option for monitoring test execution logs

---
## Quick Start

```bash
# Clone and run with Docker Compose
git clone https://github.com/Wolkenkind/pc_api.git
cd pc_api_tests
docker compose up --build api-tests
```
---
## Description

Test suite is divided into two classes: ```CrudOwnerTests``` and ```CrudOwnerWorkflowTests```. Both of them use ```ApiTestBase``` as base class that provides logging capability and commonly used **REST Assured**'s request and response specifications.

```model``` package contains model classes used for precise and clear (de)serialization of **JSON** data.

```OwnerFactory``` class from ```data``` package employs **JavaFaker** for providing random test data for positive cases and prepared test data for negative cases.

```Slf4JLoggingFilter``` class from ```base``` package is responsible for routing logs from **REST Assured** framework through Logback into needed aggregation system.

```resources/json/schema``` folder contains **JSON schemas** used for **JSON-validation** in responses via **REST Assured**'s ```JsonSchemaValidator```. Schemas were derived from **Swagger** descriptions.

Multi-source configuration ensures flexibility configurating tests - precedence is taken in following order: environment variables, system properties, properties-files and hard-coded defaults.

## Configuration

| Variable                  | Purpose                                                                   | Default                           |
|---------------------------|---------------------------------------------------------------------------|-----------------------------------|
| `BASE_URL`                | PetClinic API base URL                                                    | `http://localhost:9966/petclinic` |
| `BASE_API_PATH`           | PetClinic API path                                                        | `/api`                            |
| `RESPONSE_TIME_THRESHOLD` | Expected API response time in ms (used in default response specification) | `5000`                            |
| `DB_HOST`                 | Database host                                                             | `localhost`                       |
| `DB_PORT`                 | Database port                                                             | `5432`                            |
| `DB_VENDOR`               | Database vendor (only PostgreSQL is currently supported)                  | `postgres`                        |
| `DB_USER`                 | Database user                                                             | `petclinic`                       |
| `DB_PASSWORD`             | Database user password                                                    | `petclinic`                       |
| `LOG_FORMAT`              | Log output format                                                         | `text` (or `json` for structured) |
| `LOKI_URL`                | Loki log aggregation endpoint                                             | -                                 |
| `TEST_RUN_ID`             | CI pipeline correlation ID                                                | -                                 |

---

Provided ```Dockerfile``` and ```docker-compose.yaml``` enable using tests in **CI/CD** pipelines.

---

## Test Architecture

### Test Types
- **CRUD Tests**: Individual API operation tests with database validation (```CrudOwnerTests```)
- **Workflow Tests**: End-to-end user journey testing (API used for validation) (```CrudOwnerWorkflowTests```)
- **Parameterized Tests**: Comprehensive negative testing with multiple data scenarios

### Key Features
- **Structured JSON Logging**: Production-ready observability
- **Multi-layer Validation**: API response + JSON schema + database state
- **Performance Monitoring**: Test execution timing with warmup strategies
- **CI/CD Ready**: Containerized execution with health checks

---

## How to use

Clone the repository and open in your favorite IDE.

* Ensure there's an accessible **PostgreSQL** database '_petclinic_' on port _5432_ with user '_petclinic_' and password '_petclinic_'

* Ensure there's an accessible **PetClinic-REST** application on port _9966_

Run ```CrudOwnerTests``` and\or ```CrudOwnerWorkflowTests```

---

You can use ```docker-compose.yaml``` file in the root directory to quickly organize environment for testing if you have [Docker][16] installed. Run following command in the root folder of project:

```docker compose up postgres grafana loki petclinic-app```

This will download if needed all the software and deploy containers with it.

If property named '**_LOG_FORMAT_**' is set to value '_json_' then the logs will be output in structured **JSON**-form suitable for further processing in **ELK-stack**. Otherwise, logs will be written in human-readable form.

You can start tests without using IDE using **Docker**. Run following command in the root folder of project:

```docker compose up api-tests```

This will build tests if needed and run it. You can check the logs afterwards with the

```docker compose logs api-tests```

There's also an option of using **Grafana Loki** for logs observation and analysis. To use it, first uncomment the line in ```docker-compose.yaml```:

```# - LOKI_URL=http://loki:3100 # uncomment this to run with Loki```

Then deploy all services with the command

```docker compose up postgres grafana loki petclinic-app```

Then, after running tests you can open http://localhost:3000/login to access Grafana using username '_admin_' and password '_admin_'. Go to the '_Data sources_' tab and click '_Add data source_'. Choose '_Loki_' and enter 'http://loki:3100' into **Connection** - **URL**. After that click button '**Save & test**' at the bottom of the screen. You should see the message '_Data source successfully connected._'

After that you can go '_Explore_' tab, choose '_Loki_' as data source and run queries.

[1]: https://github.com/spring-petclinic/spring-petclinic-rest
[2]: https://junit.org/
[3]: https://rest-assured.io/
[4]: https://github.com/DiUS/java-faker
[5]: https://github.com/FasterXML/jackson
[6]: https://spring.io/projects/spring-data-jdbc
[7]: https://docs.spring.io/spring-framework/reference/data-access/transaction.html
[8]: https://jdbc.postgresql.org/
[9]: https://www.postgresql.org/
[10]: https://www.slf4j.org/
[11]: https://logback.qos.ch/
[12]: https://github.com/logfellow/logstash-logback-encoder
[13]: https://www.elastic.co/logstash
[14]: https://grafana.com/
[15]: https://grafana.com/oss/loki/
[16]: https://www.docker.com/