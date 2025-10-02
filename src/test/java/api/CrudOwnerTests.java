package api;

import base.ApiTestBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import model.Owner;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import util.ValidationUtils;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.*;

public class CrudOwnerTests extends ApiTestBase {

    private final static ObjectMapper mapper = new ObjectMapper();
    private final static String FIRSTNAME_KEY = "firstName";
    private final static String LASTNAME_KEY = "lastName";
    private final static String ADDRESS_KEY = "address";
    private final static String CITY_KEY = "city";
    private final static String TELEPHONE_KEY = "telephone";
    private final static String CREATE_PATH = "/owners";
    private final static Faker faker = new Faker();

    private final static Map<String, Object> createOwnerData = createOwnerTestData();

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
                    //.body(JsonSchemaValidator.matchesJsonSchemaInClasspath(ValidationUtils.OWNER_SCHEMA))
                    .extract().response();
        //Owner owner = response.jsonPath().getObject("", Owner.class);

        SoftAssertions softly = new SoftAssertions();
        //softly.assertThat(owner.)
    }
}
