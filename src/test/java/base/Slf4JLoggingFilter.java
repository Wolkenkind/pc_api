package base;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slf4JLoggingFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger("REST-ASSURED");

    @Override
    public Response filter(FilterableRequestSpecification requestSpec,
                           FilterableResponseSpecification responseSpec,
                           FilterContext context) {

        logRequest(requestSpec);
        Response response = context.next(requestSpec, responseSpec);
        logResponse(response);

        return response;
    }

    private void logRequest(FilterableRequestSpecification requestSpec) {
        logger.info("=== REST ASSURED REQUEST ===");
        logger.info("{} {}", requestSpec.getMethod(), requestSpec.getURI());
        logger.info("Headers: {}", requestSpec.getHeaders());

        // Log request body if present
        String body = requestSpec.getBody();
        if (body != null && !body.toString().trim().isEmpty()) {
            logger.info("Request Body: {}", body);
        }
    }

    private void logResponse(Response response) {
        logger.info("=== REST ASSURED RESPONSE ===");
        logger.info("Status: {} {}", response.getStatusCode(), response.getStatusLine());
        logger.info("Response Headers: {}", response.getHeaders());

        // Log response body if present
        String responseBody = response.getBody().asString();
        if (responseBody != null && !responseBody.trim().isEmpty()) {
            logger.info("Response Body: {}", responseBody);
        }
    }
}