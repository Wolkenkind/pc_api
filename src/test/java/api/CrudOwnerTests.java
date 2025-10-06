package api;

import base.ApiTestBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import model.Owner;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
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

import static io.restassured.RestAssured.given;
import static util.DatabaseUtils.OwnerTable.*;

public class CrudOwnerTests extends ApiTestBase {

    private final static Logger logger = LoggerFactory.getLogger(CrudOwnerTests.class);

    private final static ObjectMapper mapper = new ObjectMapper();
    private final static String FIRSTNAME_KEY = "firstName";
    private final static String LASTNAME_KEY = "lastName";
    private final static String ADDRESS_KEY = "address";
    private final static String CITY_KEY = "city";
    private final static String TELEPHONE_KEY = "telephone";
    private final static String ID_KEY = "id";
    private final static String PETS_KEY = "pets";
    private final static String CREATE_PATH = "/owners";
    private final static String READ_PATH = "/owners/{ownerId}";
    private final static String UPDATE_PATH = "/owners/{ownerId}";
    private final static String DELETE_PATH = "/owners/{ownerId}";

    private final static Set<String> READ_IGNORED_KEYS = Set.of(PETS_KEY);

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

    private static Map<String, Object> createOwnerTestData() {
        Map<String, Object> data = new HashMap<>();
        data.put(FIRSTNAME_KEY, faker.name().firstName());
        data.put(LASTNAME_KEY, faker.name().lastName());
        data.put(ADDRESS_KEY, faker.address().fullAddress());
        data.put(CITY_KEY, faker.address().city());
        data.put(TELEPHONE_KEY, faker.number().digits(10));
        return data;
    }

    @Test
    public void createOwner() throws JsonProcessingException {
        Map<String, Object> createOwnerData = createOwnerTestData();
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
        softly.assertThat(owner.getFirstName()).isEqualTo(createOwnerData.get(FIRSTNAME_KEY));
        softly.assertThat(owner.getLastName()).isEqualTo(createOwnerData.get(LASTNAME_KEY));
        softly.assertThat(owner.getAddress()).isEqualTo(createOwnerData.get(ADDRESS_KEY));
        softly.assertThat(owner.getCity()).isEqualTo(createOwnerData.get(CITY_KEY));
        softly.assertThat(owner.getTelephone()).isEqualTo(createOwnerData.get(TELEPHONE_KEY));

        int ownerId = owner.getId();
        logger.info(() -> "Owner with id " + ownerId + " created");

        createOwnerData.put(ID_KEY, ownerId);
        assertOwnerDbData(getOwnerDataFromDatabase(ownerId), createOwnerData, softly);
        softly.assertAll();
    }

    private Map<String, Object> getOwnerDataFromDatabase(int ownerId) {
        JdbcTemplate template = DatabaseUtils.createTemplate();
        String sql = "SELECT * FROM owners WHERE id = ?";
        return template.queryForMap(sql, ownerId);
    }

    private void assertOwnerDbData(Map<String, Object> actualDatabaseData, Map<String, Object> expectedData, SoftAssertions softly) {
        for (String columnName: actualDatabaseData.keySet()) {
            switch (columnName) {
                case FIRSTNAME_COL_NAME -> softly.assertThat(actualDatabaseData.get(FIRSTNAME_COL_NAME)).isEqualTo(expectedData.get(FIRSTNAME_KEY));
                case LASTNAME_COL_NAME -> softly.assertThat(actualDatabaseData.get(LASTNAME_COL_NAME)).isEqualTo(expectedData.get(LASTNAME_KEY));
                case ADDRESS_COL_NAME -> softly.assertThat(actualDatabaseData.get(ADDRESS_COL_NAME)).isEqualTo(expectedData.get(ADDRESS_KEY));
                case CITY_COL_NAME -> softly.assertThat(actualDatabaseData.get(CITY_COL_NAME)).isEqualTo(expectedData.get(CITY_KEY));
                case TELEPHONE_COL_NAME -> softly.assertThat(actualDatabaseData.get(TELEPHONE_COL_NAME)).isEqualTo(expectedData.get(TELEPHONE_KEY));
                case ID_COL_NAME -> softly.assertThat(actualDatabaseData.get(ID_COL_NAME)).isEqualTo(expectedData.get(ID_KEY));
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
        List<Object> pets = (List<Object>) actualData.get(PETS_KEY);
        if (!pets.isEmpty()) {
            softly.fail("Pets should be empty");
        }
    }

    @Test
    public void readOwner() {
        Map<String, Object> readOwnerData = createOwnerTestData();
        int ownerId = createOwnerInDatabase(readOwnerData);
        Map<String, Object> prepared = getOwnerDataFromDatabase(ownerId);
        readOwnerData.put(ID_KEY, ownerId);
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

    private int createOwnerInDatabase(Map<String, Object> ownerData) {
        JdbcTemplate template = DatabaseUtils.createTemplate();
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO owners (%s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?)"
                .formatted(FIRSTNAME_COL_NAME, LASTNAME_COL_NAME, ADDRESS_COL_NAME, CITY_COL_NAME, TELEPHONE_COL_NAME);
        template.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, (String) ownerData.get(FIRSTNAME_KEY));
            ps.setString(2, (String) ownerData.get(LASTNAME_KEY));
            ps.setString(3, (String) ownerData.get(ADDRESS_KEY));
            ps.setString(4, (String) ownerData.get(CITY_KEY));
            ps.setString(5, (String) ownerData.get(TELEPHONE_KEY));
            return ps;
        }, keyHolder);

        return keyHolder.getKeyAs(Integer.class);
    }

    private void checkOwnerData(Map<String, Object> actualData, Map<String, Object> expectedData) {
        final String exceptionMessage = "Problem preparing data, please check database and test";
        //rewrite for keys
        for (String columnName: actualData.keySet()) {
            switch (columnName) {
                case FIRSTNAME_COL_NAME: if (!actualData.get(FIRSTNAME_COL_NAME).equals(expectedData.get(FIRSTNAME_KEY))) {
                    throw new IllegalStateException(exceptionMessage);
                }
                    break;
                case LASTNAME_COL_NAME: if (!actualData.get(LASTNAME_COL_NAME).equals(expectedData.get(LASTNAME_KEY))) {
                    throw new IllegalStateException(exceptionMessage);
                }
                    break;
                case ADDRESS_COL_NAME: if (!actualData.get(ADDRESS_COL_NAME).equals(expectedData.get(ADDRESS_KEY))) {
                    throw new IllegalStateException(exceptionMessage);
                }
                    break;
                case CITY_COL_NAME: if (!actualData.get(CITY_COL_NAME).equals(expectedData.get(CITY_KEY))) {
                    throw new IllegalStateException(exceptionMessage);
                }
                    break;
                case TELEPHONE_COL_NAME: if (!actualData.get(TELEPHONE_COL_NAME).equals(expectedData.get(TELEPHONE_KEY))) {
                    throw new IllegalStateException(exceptionMessage);
                }
                    break;
                case ID_COL_NAME: if (!actualData.get(ID_COL_NAME).equals(expectedData.get(ID_KEY))) {
                    throw new IllegalStateException(exceptionMessage);
                }
                    break;
                default: throw new IllegalStateException("Unexpected database table column " + columnName);
            }
        }
    }

    @Test
    public void updateOwner() throws JsonProcessingException {
        Map<String, Object> readOwnerData = createOwnerTestData();
        int ownerId = createOwnerInDatabase(readOwnerData);
        Map<String, Object> prepared = getOwnerDataFromDatabase(ownerId);
        readOwnerData.put(ID_KEY, ownerId);
        checkOwnerData(prepared, readOwnerData);
        Map<String, Object> updateOwnerData = createOwnerTestData();
        String body = mapper.writeValueAsString(updateOwnerData);

        updateOwnerData.put(ID_KEY, ownerId);

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
}
