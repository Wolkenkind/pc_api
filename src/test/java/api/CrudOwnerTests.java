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
import org.junit.platform.commons.logging.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import util.DatabaseUtils;
import util.ValidationUtils;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.junit.platform.commons.logging.Logger;

import static io.restassured.RestAssured.*;
import static util.DatabaseUtils.OwnerTable.*;

public class CrudOwnerTests extends ApiTestBase {

    private final static Logger logger = LoggerFactory.getLogger(CrudOwnerTests.class);

    private final static ObjectMapper mapper = new ObjectMapper();
    private final static String FIRSTNAME_KEY = "firstName";
    private final static String LASTNAME_KEY = "lastName";
    private final static String ADDRESS_KEY = "address";
    private final static String CITY_KEY = "city";
    private final static String TELEPHONE_KEY = "telephone";
    private final static String CREATE_PATH = "/owners";
    private final static Faker faker = new Faker();

    private final static Map<String, Object> createOwnerData = createOwnerTestData();

    private int ownerId = 0;

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

        ownerId = owner.getId();
        logger.info(() -> "Owner with id " + ownerId + " created");

        checkOwnerExistsInDatabase(ownerId, createOwnerData, softly);
        softly.assertAll();
    }

    private void checkOwnerExistsInDatabase(int ownerId, Map<String, Object> data, SoftAssertions softly) {
        JdbcTemplate template = DatabaseUtils.createTemplate();
        String sql = "SELECT FROM owners * WHERE id = ?";
        Map<String, Object> result = template.queryForMap(sql, ownerId);
        for (String columnName: result.keySet()) {
            switch (columnName) {
                case FIRSTNAME_COL_NAME -> softly.assertThat(result.get(FIRSTNAME_COL_NAME)).isEqualTo(data.get(FIRSTNAME_KEY));
                case LASTNAME_COL_NAME -> softly.assertThat(result.get(LASTNAME_COL_NAME)).isEqualTo(data.get(LASTNAME_KEY));
                case ADDRESS_COL_NAME -> softly.assertThat(result.get(ADDRESS_COL_NAME)).isEqualTo(data.get(ADDRESS_KEY));
                case CITY_COL_NAME -> softly.assertThat(result.get(CITY_COL_NAME)).isEqualTo(data.get(CITY_KEY));
                case TELEPHONE_COL_NAME -> softly.assertThat(result.get(TELEPHONE_COL_NAME)).isEqualTo(data.get(TELEPHONE_KEY));
                default -> softly.fail("Unexpected database table column: " + columnName);
            }
        }
    }
}
