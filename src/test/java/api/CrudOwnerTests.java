package api;

import base.ApiTestBase;
import base.Slf4JLoggingFilter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import model.Owner;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import util.DatabaseUtils;
import util.ValidationUtils;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static base.ApiConstants.*;
import static check.OwnerChecker.*;
import static config.ApiConfig.*;
import static data.OwnerFactory.*;
import static io.restassured.RestAssured.given;
import static net.logstash.logback.argument.StructuredArguments.kv;
import static org.hamcrest.Matchers.lessThan;

public class CrudOwnerTests extends ApiTestBase {

    private final static Logger logger = LoggerFactory.getLogger(CrudOwnerTests.class);

    private final static ObjectMapper mapper = new ObjectMapper();

    /*
        Makes every test run with empty database at the beginning and cleans it afterward.
        Another variant is to use transaction auto rollback, e.g.
        TransactionTemplate txTemplate = DatabaseUtils.createTransactionTemplate();
        txTemplate.execute(status -> {
            ...
            return null;
        }); // all the changes made in transaction 'execute' block will be rollbacked if not committed explicitly
     */

    @BeforeEach
    public void setupRestAssuredLogging() {
        RestAssured.filters(new Slf4JLoggingFilter());
    }
    @BeforeEach
    @AfterEach
    public void cleanup() throws SQLException {
        DatabaseUtils.cleanDatabase();
    }

    @Test
    public void createOwner() throws JsonProcessingException {
        MDC.put("testId", UUID.randomUUID().toString());
        try {
            logger.info("Start test createOwner");
            createOwnerLogic();
            logger.info("Success test createOwner");
        } finally {
            MDC.clear();
        }
    }

    private void createOwnerLogic() throws JsonProcessingException {
        Map<String, Object> createOwnerData = getRandomOwnerTestData();
        String body = mapper.writeValueAsString(createOwnerData);
        Response response =
                given()
                        .spec(requestSpec)
                        .body(body)
                        .when()
                        .post(CREATE_PATH)
                        .then()
                        .spec(responseSpec)
                        .statusCode(201)
                        .body(JsonSchemaValidator.matchesJsonSchemaInClasspath(ValidationUtils.OWNER_SCHEMA))
                        .extract().response();
        Owner owner = response.jsonPath().getObject("", Owner.class);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(owner.getFirstName()).isEqualTo(createOwnerData.get(Owner.FIELD_FIRSTNAME));
        softly.assertThat(owner.getLastName()).isEqualTo(createOwnerData.get(Owner.FIELD_LASTNAME));
        softly.assertThat(owner.getAddress()).isEqualTo(createOwnerData.get(Owner.FIELD_ADDRESS));
        softly.assertThat(owner.getCity()).isEqualTo(createOwnerData.get(Owner.FIELD_CITY));
        softly.assertThat(owner.getTelephone()).isEqualTo(createOwnerData.get(Owner.FIELD_TELEPHONE));

        int ownerId = owner.getId();
        logger.info("Owner with id {} created", ownerId, kv(Owner.FIELD_ID, ownerId));
        MDC.put(Owner.FIELD_ID, String.valueOf(ownerId));

        createOwnerData.put(Owner.FIELD_ID, ownerId);
        assertOwnerDbData(getOwnerDataFromDatabase(ownerId), createOwnerData, softly);
        softly.assertAll();
    }

    @Test
    public void readOwner() {
        Map<String, Object> readOwnerData = getRandomOwnerTestData();
        int ownerId = createOwnerInDatabase(readOwnerData);
        Map<String, Object> prepared = getOwnerDataFromDatabase(ownerId);
        readOwnerData.put(Owner.FIELD_ID, ownerId);
        checkOwnerData(prepared, readOwnerData);

        Response response =
                given()
                        .spec(requestSpec)
                        .pathParam("ownerId", ownerId)
                .when()
                        .get(READ_PATH)
                .then()
                        .spec(responseSpec)
                        .statusCode(200)
                        .body(JsonSchemaValidator.matchesJsonSchemaInClasspath(ValidationUtils.OWNER_SCHEMA))
                        .extract().response();
        Map<String, Object> ownerData = response.jsonPath().getMap("");

        SoftAssertions softly = new SoftAssertions();
        assertOwnerData(ownerData, readOwnerData, softly);
        softly.assertAll();
    }

    @Test
    public void updateOwner() throws JsonProcessingException {
        Map<String, Object> readOwnerData = getRandomOwnerTestData();
        int ownerId = createOwnerInDatabase(readOwnerData);
        Map<String, Object> prepared = getOwnerDataFromDatabase(ownerId);
        readOwnerData.put(Owner.FIELD_ID, ownerId);
        checkOwnerData(prepared, readOwnerData);
        Map<String, Object> updateOwnerData = getRandomOwnerTestData();
        String body = mapper.writeValueAsString(updateOwnerData);

        updateOwnerData.put(Owner.FIELD_ID, ownerId);

        Response response =
                given()
                        .spec(requestSpec)
                        .pathParam("ownerId", ownerId)
                        .body(body)
                        .when()
                        .put(UPDATE_PATH)
                        .then()
                        .spec(responseSpec)
                        //.statusCode(200) //according to Swagger it should be 200 OK with response body, but in reality it's 204 with no body
                        .statusCode(204)
                        //.body(JsonSchemaValidator.matchesJsonSchemaInClasspath(ValidationUtils.OWNER_SCHEMA))
                        .extract().response();
        //Map<String, Object> ownerData = response.jsonPath().getMap("");

        SoftAssertions softly = new SoftAssertions();
        //assertOwnerData(ownerData, updateOwnerData, softly);
        assertOwnerDbData(getOwnerDataFromDatabase(ownerId), updateOwnerData, softly);
        softly.assertAll();
    }

    @Test
    public void deleteOwner() {
        ResponseSpecification noContentResponse = new ResponseSpecBuilder()
                .expectResponseTime(lessThan(RESPONSE_TIME_THRESHOLD), TimeUnit.MILLISECONDS)
                .build();

        Map<String, Object> deleteOwnerData = getRandomOwnerTestData();
        int ownerId = createOwnerInDatabase(deleteOwnerData);
        Map<String, Object> prepared = getOwnerDataFromDatabase(ownerId);
        deleteOwnerData.put(Owner.FIELD_ID, ownerId);
        checkOwnerData(prepared, deleteOwnerData);

        Response response =
                given()
                        .spec(requestSpec)
                        .pathParam("ownerId", ownerId)
                        .when()
                        .delete(DELETE_PATH)
                        .then()
                        .spec(noContentResponse)
                        //.statusCode(200) //according to Swagger it should be 200 OK with response body, but in reality it's 204 with no body
                        .statusCode(204)
                        //.body(JsonSchemaValidator.matchesJsonSchemaInClasspath(ValidationUtils.OWNER_SCHEMA))
                        .extract().response();
        //Map<String, Object> ownerData = response.jsonPath().getMap("");

        SoftAssertions softly = new SoftAssertions();
        //assertOwnerData(ownerData, deleteOwnerData, softly);
        Map<String, Object> deleted = getOwnerDataFromDatabase(ownerId);
        softly.assertThat(deleted).hasSize(0);
        softly.assertAll();
    }

    //negative

    @ParameterizedTest
    @MethodSource("data.OwnerFactory#getNegativeTestData")
    public void createOwnerError(Map<String, Object> createOwnerData, int expectedCode) throws JsonProcessingException {
        String body = mapper.writeValueAsString(createOwnerData);
        Response response =
                given()
                        .spec(requestSpec)
                        .body(body)
                        .when()
                        .post(CREATE_PATH)
                        .then()
                        .spec(responseSpec)
                        .statusCode(400)
                        .body(JsonSchemaValidator.matchesJsonSchemaInClasspath(ValidationUtils.API_PROBLEM_SCHEMA))
                        .extract().response();

        Map<String, Object> message = response.jsonPath().getMap("");

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(message.get("type")).isEqualTo( BASE_URL + BASE_API_PATH + CREATE_PATH);
        softly.assertThat(message.get("status")).isEqualTo(expectedCode);

        softly.assertAll();
    }
}
