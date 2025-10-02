package base;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;

import static config.ApiConfig.BASE_API_PATH;
import static config.ApiConfig.BASE_URL;

public class ApiTestBase {
    protected static RequestSpecification requestSpec;

    @BeforeAll
    public static void init() {
        requestSpec = new RequestSpecBuilder()
                .setBaseUri(BASE_URL)
                .setBasePath(BASE_API_PATH)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();
    }

}
