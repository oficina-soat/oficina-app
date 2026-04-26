package br.com.oficina.common.core.entities;

public enum UsuarioStatus {
    ATIVO,
    INATIVO;

    public static UsuarioStatus from(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status é obrigatório");
        }

        try {
            return valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Status inválido: " + status);
        }
    }
}
