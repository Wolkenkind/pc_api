package base;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static config.ApiConfig.*;
import static org.hamcrest.Matchers.lessThan;

public class ApiTestBase {
    private final static Logger logger = LoggerFactory.getLogger(ApiTestBase.class);
    protected static RequestSpecification requestSpec;
    protected static ResponseSpecification responseSpec;

    @BeforeEach
    public void setupRestAssuredLogging() {
        RestAssured.filters(new Slf4JLoggingFilter());
    }

    @BeforeAll
    public static void init() {
        requestSpec = new RequestSpecBuilder()
                .setBaseUri(BASE_URL)
                .setBasePath(BASE_API_PATH)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();
        responseSpec = new ResponseSpecBuilder()
                .expectContentType(ContentType.JSON)
                .expectResponseTime(lessThan(RESPONSE_TIME_THRESHOLD), TimeUnit.MILLISECONDS)
                .build();
    }

    protected void executeWithLogging(TestLogic testLogic, String testName) throws Exception {
        MDC.put("test_id", UUID.randomUUID().toString());
        try {
            logger.info("Start test {}", testName);
            testLogic.run();
            logger.info("Success test {}", testName);
        } finally {
            MDC.clear();
        }
    }
}
