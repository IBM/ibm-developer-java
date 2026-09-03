package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class PoemsTest {

    @Test
    void testHelloEndpoint() {
        given()
                .when().get("/poems")
                .then()
                .statusCode(200)
                .body(is("hello"));
    }
}
