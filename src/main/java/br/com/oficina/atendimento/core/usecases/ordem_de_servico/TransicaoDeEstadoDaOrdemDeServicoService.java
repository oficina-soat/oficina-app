package br.com.oficina.atendimento.core.usecases.ordem_de_servico;

import br.com.oficina.atendimento.core.entities.ordem_de_servico.OrdemDeServico;
import br.com.oficina.atendimento.core.entities.ordem_de_servico.TipoDeEstadoDaOrdemDeServico;
import br.com.oficina.atendimento.core.interfaces.gateway.OrdemDeServicoGateway;
import br.com.oficina.atendimento.core.interfaces.sender.EstadoDaOrdemDeServicoSender;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

public class TransicaoDeEstadoDaOrdemDeServicoService {

    private final OrdemDeServicoGateway ordemDeServicoGateway;
    private final EstadoDaOrdemDeServicoSender estadoDaOrdemDeServicoSender;

    public TransicaoDeEstadoDaOrdemDeServicoService(
            OrdemDeServicoGateway ordemDeServicoGateway,
            EstadoDaOrdemDeServicoSender estadoDaOrdemDeServicoSender
    ) {
        this.ordemDeServicoGateway = ordemDeServicoGateway;
        this.estadoDaOrdemDeServicoSender = estadoDaOrdemDeServicoSender;
    }

    public CompletableFuture<Void> executarTransicaoSimples(UUID ordemDeServicoId, Consumer<OrdemDeServico> transicao) {
        var mensagem = new AtomicReference<EstadoDaOrdemDeServicoSender.Mensagem>();
        return ordemDeServicoGateway.buscaSimplesParaAtualizar(ordemDeServicoId, ordemDeServico -> {
                    var estadoAnterior = ordemDeServico.estadoDaOrdemDeServico();
                    transicao.accept(ordemDeServico);
                    registrarMensagemSeHouveMudanca(ordemDeServico, estadoAnterior, mensagem);
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
                    return transicao.apply(ordemDeServico)
                            .thenRun(() -> registrarMensagemSeHouveMudanca(ordemDeServico, estadoAnterior, mensagem));
                })
                .thenCompose(_ -> enviarSeNecessario(mensagem.get()));
    }

    public CompletableFuture<Void> notificarMudancaSeHouver(
            OrdemDeServico ordemDeServico,
            TipoDeEstadoDaOrdemDeServico estadoAnterior
    ) {
        var mensagem = criarMensagemSeHouveMudanca(ordemDeServico, estadoAnterior);
        return enviarSeNecessario(mensagem);
    }

    private void registrarMensagemSeHouveMudanca(
            OrdemDeServico ordemDeServico,
            TipoDeEstadoDaOrdemDeServico estadoAnterior,
            AtomicReference<EstadoDaOrdemDeServicoSender.Mensagem> mensagem
    ) {
        mensagem.set(criarMensagemSeHouveMudanca(ordemDeServico, estadoAnterior));
    }

    private EstadoDaOrdemDeServicoSender.Mensagem criarMensagemSeHouveMudanca(
            OrdemDeServico ordemDeServico,
            TipoDeEstadoDaOrdemDeServico estadoAnterior
    ) {
        var novoEstado = ordemDeServico.estadoDaOrdemDeServico();
        if (estadoAnterior == novoEstado) {
            return null;
        }
        return new EstadoDaOrdemDeServicoSender.Mensagem(
                ordemDeServico.id(),
                ordemDeServico.clienteId(),
                estadoAnterior,
                novoEstado);
    }

    private CompletableFuture<Void> enviarSeNecessario(EstadoDaOrdemDeServicoSender.Mensagem mensagem) {
        return mensagem == null
                ? CompletableFuture.completedFuture(null)
                : estadoDaOrdemDeServicoSender.enviar(mensagem);
    }
}
