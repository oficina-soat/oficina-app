package br.com.oficina.common.framework.observability;

import br.com.oficina.atendimento.core.entities.ordem_de_servico.TipoDeEstadoDaOrdemDeServico;

import java.time.Instant;
import java.util.UUID;

public interface AppObservability {

    void onOrderCreated(UUID ordemDeServicoId, TipoDeEstadoDaOrdemDeServico statusAtual);

    void onOrderTransition(UUID ordemDeServicoId,
                           TipoDeEstadoDaOrdemDeServico statusAnterior,
                           TipoDeEstadoDaOrdemDeServico statusNovo,
                           Instant statusAnteriorDesde,
                           Instant statusNovoDesde);

    void onIntegrationSuccess(String integration, String operation, long latencyMs);

    void onIntegrationFailure(String integration,
                              String operation,
                              String failureType,
                              long latencyMs,
                              Throwable throwable);

    static AppObservability noop() {
        return NoopAppObservability.INSTANCE;
    }

    enum NoopAppObservability implements AppObservability {
        INSTANCE;

        @Override
        public void onOrderCreated(UUID ordemDeServicoId, TipoDeEstadoDaOrdemDeServico statusAtual) {
        }

        @Override
        public void onOrderTransition(UUID ordemDeServicoId,
                                      TipoDeEstadoDaOrdemDeServico statusAnterior,
                                      TipoDeEstadoDaOrdemDeServico statusNovo,
                                      Instant statusAnteriorDesde,
                                      Instant statusNovoDesde) {
        }

        @Override
        public void onIntegrationSuccess(String integration, String operation, long latencyMs) {
        }

        @Override
        public void onIntegrationFailure(String integration,
                                         String operation,
                                         String failureType,
                                         long latencyMs,
                                         Throwable throwable) {
        }
    }
}
