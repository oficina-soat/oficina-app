package br.com.oficina.common.tests;

public record AutenticarUsuarioResponse(
        String access_token,
        String token_type,
        int expires_in) {
}
