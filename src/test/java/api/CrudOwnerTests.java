package api;

import base.ApiTestBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import model.Owner;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import util.DatabaseUtils;
import util.ValidationUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static base.ApiConstants.*;
import static config.ApiConfig.RESPONSE_TIME_THRESHOLD;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.lessThan;
import static util.DatabaseUtils.OwnerTable.*;

public class CrudOwnerTests extends ApiTestBase {

    private final static Logger logger = LoggerFactory.getLogger(CrudOwnerTests.class);

    private final static ObjectMapper mapper = new ObjectMapper();

    private final static Set<String> READ_IGNORED_KEYS = Set.of(Owner.FIELD_PETS);

    private final static Faker faker = new Faker();

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
    @AfterEach
    public void cleanup() throws SQLException {
        DatabaseUtils.cleanDatabase();

    }

    private static Map<String, Object> getRandomOwnerTestData() {
        Map<String, Object> data = new HashMap<>();
        data.put(Owner.FIELD_FIRSTNAME, faker.name().firstName());
        data.put(Owner.FIELD_LASTNAME, faker.name().lastName());
        data.put(Owner.FIELD_ADDRESS, faker.address().fullAddress());
        data.put(Owner.FIELD_CITY, faker.address().city());
        data.put(Owner.FIELD_TELEPHONE, faker.number().digits(10));
        return data;
    }

    @Test
    public void createOwner() throws JsonProcessingException {
        Map<String, Object> createOwnerData = getRandomOwnerTestData();
        String body = mapper.writeValueAsString(createOwnerData);
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

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(owner.getFirstName()).isEqualTo(createOwnerData.get(Owner.FIELD_FIRSTNAME));
        softly.assertThat(owner.getLastName()).isEqualTo(createOwnerData.get(Owner.FIELD_LASTNAME));
        softly.assertThat(owner.getAddress()).isEqualTo(createOwnerData.get(Owner.FIELD_ADDRESS));
        softly.assertThat(owner.getCity()).isEqualTo(createOwnerData.get(Owner.FIELD_CITY));
        softly.assertThat(owner.getTelephone()).isEqualTo(createOwnerData.get(Owner.FIELD_TELEPHONE));

        int ownerId = owner.getId();
        logger.info(() -> "Owner with id " + ownerId + " created");

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

        SoftAssertions softly = new SoftAssertions();
        //assertOwnerData(ownerData, deleteOwnerData, softly);
        Map<String, Object> deleted = getOwnerDataFromDatabase(ownerId);
        softly.assertThat(deleted).hasSize(0);
        softly.assertAll();
    }

    private Map<String, Object> getOwnerDataFromDatabase(int ownerId) {
        JdbcTemplate template = DatabaseUtils.createTemplate();
        String sql = "SELECT * FROM owners WHERE id = ?";
        Map<String, Object> res;

        try {
            res = template.queryForMap(sql, ownerId);
        } catch (EmptyResultDataAccessException emptyException) {
            return Map.of();
        }

        return res;
    }

    private void assertOwnerDbData(Map<String, Object> actualDatabaseData, Map<String, Object> expectedData, SoftAssertions softly) {
        for (String columnName: actualDatabaseData.keySet()) {
            switch (columnName) {
                case FIRSTNAME_COL_NAME -> softly.assertThat(actualDatabaseData.get(FIRSTNAME_COL_NAME)).isEqualTo(expectedData.get(Owner.FIELD_FIRSTNAME));
                case LASTNAME_COL_NAME -> softly.assertThat(actualDatabaseData.get(LASTNAME_COL_NAME)).isEqualTo(expectedData.get(Owner.FIELD_LASTNAME));
                case ADDRESS_COL_NAME -> softly.assertThat(actualDatabaseData.get(ADDRESS_COL_NAME)).isEqualTo(expectedData.get(Owner.FIELD_ADDRESS));
                case CITY_COL_NAME -> softly.assertThat(actualDatabaseData.get(CITY_COL_NAME)).isEqualTo(expectedData.get(Owner.FIELD_CITY));
                case TELEPHONE_COL_NAME -> softly.assertThat(actualDatabaseData.get(TELEPHONE_COL_NAME)).isEqualTo(expectedData.get(Owner.FIELD_TELEPHONE));
                case ID_COL_NAME -> softly.assertThat(actualDatabaseData.get(ID_COL_NAME)).isEqualTo(expectedData.get(Owner.FIELD_ID));
                default -> softly.fail("Unexpected database table column: " + columnName);
            }
        }
    }

    private void assertOwnerData(Map<String, Object> actualData, Map<String, Object> expectedData, SoftAssertions softly) {
        for (String keyName: actualData.keySet()) {
            if (!READ_IGNORED_KEYS.contains(keyName)) {
                Object actualValue = actualData.get(keyName);
                Object expectedValue = expectedData.get(keyName);
                if (expectedValue == null) {
                    softly.fail("Unexpected key '%s' : '%s'".formatted(keyName, actualValue));
                }
                softly.assertThat(actualValue).isEqualTo(expectedValue);
            }
        }
        List<Object> pets = (List<Object>) actualData.get(Owner.FIELD_PETS);
        if (!pets.isEmpty()) {
            softly.fail("Pets should be empty");
        }
    }

    private int createOwnerInDatabase(Map<String, Object> ownerData) {
        JdbcTemplate template = DatabaseUtils.createTemplate();
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO owners (%s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?)"
                .formatted(FIRSTNAME_COL_NAME, LASTNAME_COL_NAME, ADDRESS_COL_NAME, CITY_COL_NAME, TELEPHONE_COL_NAME);
        template.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, (String) ownerData.get(Owner.FIELD_FIRSTNAME));
            ps.setString(2, (String) ownerData.get(Owner.FIELD_LASTNAME));
            ps.setString(3, (String) ownerData.get(Owner.FIELD_ADDRESS));
            ps.setString(4, (String) ownerData.get(Owner.FIELD_CITY));
            ps.setString(5, (String) ownerData.get(Owner.FIELD_TELEPHONE));
            return ps;
        }, keyHolder);

        return keyHolder.getKeyAs(Integer.class);
    }

    private void checkOwnerData(Map<String, Object> actualData, Map<String, Object> expectedData) {
        final String exceptionMessage = "Problem preparing data, please check database and test";
        //rewrite for keys
        for (String columnName: actualData.keySet()) {
            switch (columnName) {
                case FIRSTNAME_COL_NAME: if (!actualData.get(FIRSTNAME_COL_NAME).equals(expectedData.get(Owner.FIELD_FIRSTNAME))) {
                    throw new IllegalStateException(exceptionMessage);
                }
                    break;
                case LASTNAME_COL_NAME: if (!actualData.get(LASTNAME_COL_NAME).equals(expectedData.get(Owner.FIELD_LASTNAME))) {
                    throw new IllegalStateException(exceptionMessage);
                }
                    break;
                case ADDRESS_COL_NAME: if (!actualData.get(ADDRESS_COL_NAME).equals(expectedData.get(Owner.FIELD_ADDRESS))) {
                    throw new IllegalStateException(exceptionMessage);
                }
                    break;
                case CITY_COL_NAME: if (!actualData.get(CITY_COL_NAME).equals(expectedData.get(Owner.FIELD_CITY))) {
                    throw new IllegalStateException(exceptionMessage);
                }
                    break;
                case TELEPHONE_COL_NAME: if (!actualData.get(TELEPHONE_COL_NAME).equals(expectedData.get(Owner.FIELD_TELEPHONE))) {
                    throw new IllegalStateException(exceptionMessage);
                }
                    break;
                case ID_COL_NAME: if (!actualData.get(ID_COL_NAME).equals(expectedData.get(Owner.FIELD_ID))) {
                    throw new IllegalStateException(exceptionMessage);
                }
                    break;
                default: throw new IllegalStateException("Unexpected database table column " + columnName);
            }
        }
    }
}
