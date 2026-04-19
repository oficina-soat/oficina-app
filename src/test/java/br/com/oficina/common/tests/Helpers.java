package br.com.oficina.common.tests;

import br.com.oficina.common.web.TipoDePapel;
import io.restassured.http.Header;
import io.smallrye.jwt.build.Jwt;

import java.time.Instant;
import java.util.Set;

public class Helpers {
    public static String gerarToken(TipoDePapel tipoDePapel) {
        return Jwt.issuer("oficina-api")
                .subject(tipoDePapel.valor())
                .upn(tipoDePapel.valor())
                .groups(Set.of(tipoDePapel.valor()))
                .expiresAt(Instant.now().plusSeconds(3600))
                .sign();
    }

    public static Header gerarHeaderToken(TipoDePapel tipoDePapel) {
        return new Header("Authorization", "Bearer " + Helpers.gerarToken(tipoDePapel));
    }
}
