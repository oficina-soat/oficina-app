package br.com.oficina.atendimento.core.interfaces.presenter.dto;

import java.time.Instant;

public record ItemHistoricoEstadoDTO(
        String estado,
        Instant dataHora) {
}
