package br.com.oficina.atendimento.framework.security;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

@QuarkusTest
@TestProfile(MagicLinkServiceIT.ApiGatewayMagicLinkProfile.class)
class MagicLinkServiceIT {

    private static final String API_GATEWAY_BASE_URL = "https://abc123.execute-api.us-east-1.amazonaws.com";

    @Inject MagicLinkService magicLinkService;

    @Test
    @RunOnVertxContext
    void deveGerarLinksComBasePublicaDoApiGatewayTest(UniAsserter asserter) {
        var ordemDeServicoId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        asserter.execute(() -> Uni.createFrom().completionStage(() ->
                        magicLinkService.gerarLinks(ordemDeServicoId, "cliente@oficina.com"))
                .invoke(links -> Assertions.assertAll(
                        () -> Assertions.assertTrue(links.acompanhar().startsWith(
                                API_GATEWAY_BASE_URL + "/ordem-de-servico/" + ordemDeServicoId + "/acompanhar-link?actionToken=")),
                        () -> Assertions.assertTrue(links.aprovar().startsWith(
                                API_GATEWAY_BASE_URL + "/ordem-de-servico/" + ordemDeServicoId + "/aprovar-link?actionToken=")),
                        () -> Assertions.assertTrue(links.recusar().startsWith(
                                API_GATEWAY_BASE_URL + "/ordem-de-servico/" + ordemDeServicoId + "/recusar-link?actionToken=")),
                        () -> Assertions.assertFalse(links.acompanhar().contains("localhost")),
                        () -> Assertions.assertFalse(links.aprovar().contains("localhost")),
                        () -> Assertions.assertFalse(links.recusar().contains("localhost"))
                ))
                .replaceWithVoid());
    }

    public static class ApiGatewayMagicLinkProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("oficina.magic-link.base-url", API_GATEWAY_BASE_URL + "/");
        }
    }
}
