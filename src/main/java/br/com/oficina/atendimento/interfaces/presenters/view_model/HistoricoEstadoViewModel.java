package br.com.oficina.atendimento.interfaces.presenters.view_model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record HistoricoEstadoViewModel(
        UUID ordemDeServicoId,
        List<ItemViewModel> historicoList) {
    public record ItemViewModel(
            String estado,
            Instant dataHora) {
    }
}
