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

import static base.TestStatus.*;
import static config.ApiConfig.*;
import static net.logstash.logback.argument.StructuredArguments.kv;
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
        TestStatus status = SUCCESS;
        long start = 0, durationNs, durationMs;
        try {
            logger.info("Start test {}", testName);
            start = System.nanoTime();
            testLogic.run();
            durationNs = System.nanoTime() - start;
            durationMs = TimeUnit.NANOSECONDS.toMillis(durationNs);
            //for Loki
            MDC.put("test_duration_ms", String.valueOf(durationMs));
            MDC.put("test_status", status.toString());
            logger.info("Test {} completed", testName,
                    kv("test_duration_ns", durationNs),
                    kv("test_duration_ms", durationMs),
                    kv("test_status", status)
            );
        } catch (AssertionError ae) {
            durationNs = System.nanoTime() - start;
            durationMs = TimeUnit.NANOSECONDS.toMillis(durationNs);
            status = FAILED;
            //for Loki
            MDC.put("test_duration_ms", String.valueOf(durationMs));
            MDC.put("test_status", status.toString());
            logger.error("Test {} failed", testName,
                    kv("test_duration_ns", durationNs),
                    kv("test_duration_ms", durationMs),
                    kv("test_status", status)
            );
            throw ae;
        } catch (Exception e) {
            durationNs = System.nanoTime() - start;
            durationMs = TimeUnit.NANOSECONDS.toMillis(durationNs);
            status = ERROR;
            //for Loki
            MDC.put("test_duration_ms", String.valueOf(durationMs));
            MDC.put("test_status", status.toString());
            logger.error("Test {} has thrown an error '{}'", testName, e.getMessage(),
                    kv("test_duration_ns", durationNs),
                    kv("test_duration_ms", durationMs),
                    kv("test_status", status)
            );
            throw e;
        } finally {
            MDC.clear();
        }
    }
}
