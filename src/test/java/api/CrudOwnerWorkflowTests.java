package api;

import base.ApiTestBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import db.DatabaseConstants;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import mapper.OwnerMapper;
import model.Owner;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import util.ValidationUtils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static base.ApiConstants.*;
import static check.OwnerChecker.assertOwnerData;
import static check.OwnerChecker.assertOwnerDbData;
import static config.ApiConfig.RESPONSE_TIME_THRESHOLD;
import static data.OwnerFactory.getOwnerDataFromDatabase;
import static data.OwnerFactory.getRandomOwnerTestData;
import static io.restassured.RestAssured.given;
import static model.Owner.FIELD_ID;
import static org.hamcrest.Matchers.lessThan;

public class CrudOwnerWorkflowTests extends ApiTestBase {
    private final static Logger logger = LoggerFactory.getLogger(CrudOwnerWorkflowTests.class);

    private final static ObjectMapper mapper = new ObjectMapper();

    @Test
    public void createReadWorkflowTest() throws JsonProcessingException {
        SoftAssertions softly = new SoftAssertions();

        Owner owner = createOwnerWithApi(softly);
        readOwnerWithApi(owner, softly);

        softly.assertAll();
    }

    @Test
    public void createUpdateWorkflowTest() throws JsonProcessingException {
        SoftAssertions softly = new SoftAssertions();

        createUpdateLogic(softly);

        softly.assertAll();
    }

    @Test
    public void createDeleteWorkflowTest() throws JsonProcessingException {
        SoftAssertions softly = new SoftAssertions();

        Owner owner = createOwnerWithApi(softly);

        ResponseSpecification noContentResponse = new ResponseSpecBuilder()
                .expectResponseTime(lessThan(RESPONSE_TIME_THRESHOLD), TimeUnit.MILLISECONDS)
                .build();

        Response response =
                given()
                        .spec(requestSpec)
                        .pathParam("ownerId", owner.getId())
                        .log().all()
                        .when()
                        .delete(DELETE_PATH)
                        .then()
                        .spec(noContentResponse)
                        .log().all()
                        //.statusCode(200) //according to Swagger it should be 200 OK with response body, but in reality it's 204 with no body
                        .statusCode(204)
                        //.body(JsonSchemaValidator.matchesJsonSchemaInClasspath(ValidationUtils.OWNER_SCHEMA))
                        .extract().response();
        //Map<String, Object> ownerData = response.jsonPath().getMap("");

        Map<String, Object> deleted = getOwnerDataFromDatabase(owner.getId());
        softly.assertThat(deleted).hasSize(0);

        softly.assertAll();
    }

    @Test
    public void updateReadWorkflowTest() throws JsonProcessingException {
        SoftAssertions softly = new SoftAssertions();

        Owner updatedOwner = createUpdateLogic(softly);
        readOwnerWithApi(updatedOwner, softly);

        softly.assertAll();
    }

    private Owner createOwnerWithApi(SoftAssertions softly) throws JsonProcessingException {
        Map<String, Object> ownerData = getRandomOwnerTestData();
        String body = mapper.writeValueAsString(ownerData);
        Response response =
                given()
                        .spec(requestSpec)
                        .body(body)
                        .log().all()
                        .when()
                        .post(CREATE_PATH)
                        .then()
                        .spec(responseSpec)
                        .log().all()
                        .statusCode(201)
                        .body(JsonSchemaValidator.matchesJsonSchemaInClasspath(ValidationUtils.OWNER_SCHEMA))
                        .extract().response();
        Owner owner = response.jsonPath().getObject("", Owner.class);

        softly.assertThat(owner.getFirstName()).isEqualTo(ownerData.get(Owner.FIELD_FIRSTNAME));
        softly.assertThat(owner.getLastName()).isEqualTo(ownerData.get(Owner.FIELD_LASTNAME));
        softly.assertThat(owner.getAddress()).isEqualTo(ownerData.get(Owner.FIELD_ADDRESS));
        softly.assertThat(owner.getCity()).isEqualTo(ownerData.get(Owner.FIELD_CITY));
        softly.assertThat(owner.getTelephone()).isEqualTo(ownerData.get(Owner.FIELD_TELEPHONE));

        int ownerId = owner.getId();
        logger.info(() -> "Owner with id " + ownerId + " created");

        return owner;
    }

    private void readOwnerWithApi(Owner owner, SoftAssertions softly) {
        Response response =
                given()
                        .spec(requestSpec)
                        .pathParam("ownerId", owner.getId())
                        .log().all()
                        .when()
                        .get(READ_PATH)
                        .then()
                        .spec(responseSpec)
                        .log().all()
                        .statusCode(200)
                        .body(JsonSchemaValidator.matchesJsonSchemaInClasspath(ValidationUtils.OWNER_SCHEMA))
                        .extract().response();
        Map<String, Object> ownerData = response.jsonPath().getMap("");

        assertOwnerData(ownerData, owner, softly);
    }

    private Map<String, Object> updateOwnerWithApi(
            int id,
            Map<String, Object> updateOwnerData,
            SoftAssertions softly,
            boolean assertDb) throws JsonProcessingException {
        String body = mapper.writeValueAsString(updateOwnerData);

        updateOwnerData.put(FIELD_ID, id);

        Response response =
                given()
                        .spec(requestSpec)
                        .pathParam("ownerId", id)
                        .body(body)
                        .log().all()
                        .when()
                        .put(UPDATE_PATH)
                        .then()
                        .spec(responseSpec)
                        .log().all()
                        //.statusCode(200) //according to Swagger it should be 200 OK with response body, but in reality it's 204 with no body
                        .statusCode(204)
                        //.body(JsonSchemaValidator.matchesJsonSchemaInClasspath(ValidationUtils.OWNER_SCHEMA))
                        .extract().response();
        //Map<String, Object> ownerData = response.jsonPath().getMap("");

        //assertOwnerData(ownerData, updateOwnerData, softly);
        Map<String, Object> updatedOwnerDbData = getOwnerDataFromDatabase(id);
        if (assertDb) {
            assertOwnerDbData(updatedOwnerDbData, updateOwnerData, softly);
        }

        return updatedOwnerDbData;
    }

    private Owner createUpdateLogic(SoftAssertions softly) throws JsonProcessingException {
        Owner owner = createOwnerWithApi(softly);
        Map<String, Object> updateData = getRandomOwnerTestData();
        //actually according to Swagger, update should return body with updated entity, but in reality it's 204 with no body
        Map<String, Object> updatedOwnerDbData = updateOwnerWithApi(owner.getId(), updateData, softly, false);

        Optional<Integer> idFromUpdate = Optional.ofNullable((Integer) updatedOwnerDbData.get(DatabaseConstants.OwnerTable.ID_COL_NAME));
        if (idFromUpdate.isEmpty()) {
            softly.fail("No id of updated owner was returned from database!");
        }

        softly.assertThat(idFromUpdate.orElse(-1)).isEqualTo(owner.getId());

        assertOwnerDbData(updatedOwnerDbData, updateData, softly);

        return OwnerMapper.fromDbKeyValues(updatedOwnerDbData);
    }
}
