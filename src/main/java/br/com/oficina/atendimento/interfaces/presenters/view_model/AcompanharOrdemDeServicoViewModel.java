package br.com.oficina.atendimento.interfaces.presenters.view_model;

import java.util.UUID;

public record AcompanharOrdemDeServicoViewModel(UUID ordemDeServicoId, String estado) {
}
