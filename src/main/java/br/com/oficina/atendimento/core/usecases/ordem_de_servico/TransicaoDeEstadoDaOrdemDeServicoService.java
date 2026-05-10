package br.com.oficina.atendimento.core.usecases.ordem_de_servico;

import br.com.oficina.atendimento.core.entities.ordem_de_servico.OrdemDeServico;
import br.com.oficina.atendimento.core.entities.ordem_de_servico.TipoDeEstadoDaOrdemDeServico;
import br.com.oficina.atendimento.core.interfaces.gateway.OrdemDeServicoGateway;
import br.com.oficina.atendimento.core.interfaces.sender.EstadoDaOrdemDeServicoSender;
import br.com.oficina.common.framework.observability.AppObservability;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

public class TransicaoDeEstadoDaOrdemDeServicoService {

    private final OrdemDeServicoGateway ordemDeServicoGateway;
    private final EstadoDaOrdemDeServicoSender estadoDaOrdemDeServicoSender;
    private final AppObservability appObservability;

    public TransicaoDeEstadoDaOrdemDeServicoService(
            OrdemDeServicoGateway ordemDeServicoGateway,
            EstadoDaOrdemDeServicoSender estadoDaOrdemDeServicoSender
    ) {
        this(ordemDeServicoGateway, estadoDaOrdemDeServicoSender, AppObservability.noop());
    }

    public TransicaoDeEstadoDaOrdemDeServicoService(
            OrdemDeServicoGateway ordemDeServicoGateway,
            EstadoDaOrdemDeServicoSender estadoDaOrdemDeServicoSender,
            AppObservability appObservability
    ) {
        this.ordemDeServicoGateway = ordemDeServicoGateway;
        this.estadoDaOrdemDeServicoSender = estadoDaOrdemDeServicoSender;
        this.appObservability = appObservability;
    }

    public CompletableFuture<Void> executarTransicaoSimples(UUID ordemDeServicoId, Consumer<OrdemDeServico> transicao) {
        var mensagem = new AtomicReference<EstadoDaOrdemDeServicoSender.Mensagem>();
        return ordemDeServicoGateway.buscaSimplesParaAtualizar(ordemDeServicoId, ordemDeServico -> {
                    var estadoAnterior = ordemDeServico.estadoDaOrdemDeServico();
                    var estadoAnteriorDesde = ordemDeServico.dataDoEstado();
                    transicao.accept(ordemDeServico);
                    registrarMensagemSeHouveMudanca(ordemDeServico, estadoAnterior, estadoAnteriorDesde, mensagem);
                })
                .thenCompose(_ -> enviarSeNecessario(mensagem.get()));
    }

    public CompletableFuture<Void> executarTransicaoCompleta(
            UUID ordemDeServicoId,
            Function<OrdemDeServico, CompletableFuture<Void>> transicao
    ) {
        var mensagem = new AtomicReference<EstadoDaOrdemDeServicoSender.Mensagem>();
        return ordemDeServicoGateway.buscaComPecasEServicosParaAtualizar(ordemDeServicoId, ordemDeServico -> {
                    var estadoAnterior = ordemDeServico.estadoDaOrdemDeServico();
                    var estadoAnteriorDesde = ordemDeServico.dataDoEstado();
                    return transicao.apply(ordemDeServico)
                            .thenRun(() -> registrarMensagemSeHouveMudanca(
                                    ordemDeServico,
                                    estadoAnterior,
                                    estadoAnteriorDesde,
                                    mensagem));
                })
                .thenCompose(_ -> enviarSeNecessario(mensagem.get()));
    }

    public CompletableFuture<Void> notificarMudancaSeHouver(
            OrdemDeServico ordemDeServico,
            TipoDeEstadoDaOrdemDeServico estadoAnterior
    ) {
        var mensagem = criarMensagemSeHouveMudanca(
                ordemDeServico,
                estadoAnterior,
                inferirDataDoEstadoAnterior(ordemDeServico));
        return enviarSeNecessario(mensagem);
    }

    public CompletableFuture<Void> notificarMudancaSeHouver(
            OrdemDeServico ordemDeServico,
            TipoDeEstadoDaOrdemDeServico estadoAnterior,
            Instant estadoAnteriorDesde
    ) {
        var mensagem = criarMensagemSeHouveMudanca(ordemDeServico, estadoAnterior, estadoAnteriorDesde);
        return enviarSeNecessario(mensagem);
    }

    private void registrarMensagemSeHouveMudanca(
            OrdemDeServico ordemDeServico,
            TipoDeEstadoDaOrdemDeServico estadoAnterior,
            Instant estadoAnteriorDesde,
            AtomicReference<EstadoDaOrdemDeServicoSender.Mensagem> mensagem
    ) {
        mensagem.set(criarMensagemSeHouveMudanca(ordemDeServico, estadoAnterior, estadoAnteriorDesde));
    }

    private EstadoDaOrdemDeServicoSender.Mensagem criarMensagemSeHouveMudanca(
            OrdemDeServico ordemDeServico,
            TipoDeEstadoDaOrdemDeServico estadoAnterior,
            Instant estadoAnteriorDesde
    ) {
        var novoEstado = ordemDeServico.estadoDaOrdemDeServico();
        if (estadoAnterior == novoEstado) {
            return null;
        }
        appObservability.onOrderTransition(
                ordemDeServico.id(),
                estadoAnterior,
                novoEstado,
                estadoAnteriorDesde,
                ordemDeServico.dataDoEstado());
        return new EstadoDaOrdemDeServicoSender.Mensagem(
                ordemDeServico.id(),
                ordemDeServico.clienteId(),
                estadoAnterior,
                novoEstado);
    }

    private Instant inferirDataDoEstadoAnterior(OrdemDeServico ordemDeServico) {
        var historico = ordemDeServico.historicoDeEstados();
        return historico.size() >= 2
                ? historico.get(historico.size() - 2).dataDoEstado()
                : ordemDeServico.dataDoEstado();
    }

    private CompletableFuture<Void> enviarSeNecessario(EstadoDaOrdemDeServicoSender.Mensagem mensagem) {
        return mensagem == null
                ? CompletableFuture.completedFuture(null)
                : estadoDaOrdemDeServicoSender.enviar(mensagem);
    }
}
