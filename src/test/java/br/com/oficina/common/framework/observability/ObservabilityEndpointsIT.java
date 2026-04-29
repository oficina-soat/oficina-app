package br.com.oficina.common.framework.observability;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
class ObservabilityEndpointsIT {

    @Test
    void shouldExposeLivenessProbeOverHttp() {
        given()
                .when()
                .get("/q/health/live")
                .then()
                .statusCode(200);
    }

    @Test
    void shouldExposeReadinessProbeOverHttp() {
        given()
                .when()
                .get("/q/health/ready")
                .then()
                .statusCode(200);
    }
}
