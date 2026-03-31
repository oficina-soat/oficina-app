package br.com.oficina.atendimento.core.interfaces.gateway;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface EstoqueGateway {

    CompletableFuture<Void> baixarEstoquePorConsumo(EstoqueRequest estoqueRequest);

    record EstoqueRequest(long pecaId, UUID ordemDeServicoId, BigDecimal quantidade, String observacao) {
    }
}
