package br.com.oficina.atendimento.core.usecases.ordem_de_servico;

import br.com.oficina.atendimento.core.entities.cliente.Cliente;
import br.com.oficina.atendimento.core.entities.cliente.DocumentoFactory;
import br.com.oficina.atendimento.core.entities.cliente.Email;
import br.com.oficina.atendimento.core.entities.ordem_de_servico.OrdemDeServico;
import br.com.oficina.atendimento.core.entities.ordem_de_servico.OrdemDeServicoFactory;
import br.com.oficina.atendimento.core.entities.ordem_de_servico.TipoDeEstadoDaOrdemDeServico;
import br.com.oficina.atendimento.core.entities.veiculo.MarcaDeVeiculo;
import br.com.oficina.atendimento.core.entities.veiculo.ModeloDeVeiculo;
import br.com.oficina.atendimento.core.entities.veiculo.PlacaDeVeiculo;
import br.com.oficina.atendimento.core.entities.veiculo.Veiculo;
import br.com.oficina.atendimento.core.interfaces.gateway.CatalogoGateway;
import br.com.oficina.atendimento.core.interfaces.gateway.ClienteGateway;
import br.com.oficina.atendimento.core.interfaces.gateway.OrdemDeServicoGateway;
import br.com.oficina.atendimento.core.interfaces.gateway.VeiculoGateway;
import br.com.oficina.atendimento.core.interfaces.presenter.OrdemDeServicoPresenter;
import br.com.oficina.atendimento.core.interfaces.presenter.dto.OrdemDeServicoDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AbrirOrdemDeServicoCompletaUseCase {

    private final OrdemDeServicoGateway ordemDeServicoGateway;
    private final ClienteGateway clienteGateway;
    private final VeiculoGateway veiculoGateway;
    private final CatalogoGateway catalogoGateway;
    private final OrdemDeServicoPresenter ordemDeServicoPresenter;
    private final TransicaoDeEstadoDaOrdemDeServicoService transicaoDeEstadoDaOrdemDeServicoService;

    public AbrirOrdemDeServicoCompletaUseCase(OrdemDeServicoGateway ordemDeServicoGateway,
                                              ClienteGateway clienteGateway,
                                              VeiculoGateway veiculoGateway,
                                              CatalogoGateway catalogoGateway,
                                              OrdemDeServicoPresenter ordemDeServicoPresenter,
                                              TransicaoDeEstadoDaOrdemDeServicoService transicaoDeEstadoDaOrdemDeServicoService) {
        this.ordemDeServicoGateway = ordemDeServicoGateway;
        this.clienteGateway = clienteGateway;
        this.veiculoGateway = veiculoGateway;
        this.catalogoGateway = catalogoGateway;
        this.ordemDeServicoPresenter = ordemDeServicoPresenter;
        this.transicaoDeEstadoDaOrdemDeServicoService = transicaoDeEstadoDaOrdemDeServicoService;
    }

    public CompletableFuture<Void> executar(Command command) {
        var documento = DocumentoFactory.from(command.documentoDoCliente());
        var placa = new PlacaDeVeiculo(command.placaDoVeiculo());

        var cliente = new Cliente(0, documento, new Email(command.emailDoCliente()));
        var veiculo = new Veiculo(
                0,
                placa,
                new MarcaDeVeiculo(command.marcaDoVeiculo()),
                new ModeloDeVeiculo(command.modeloDoVeiculo()),
                command.ano());
        return clienteGateway.adicionar(cliente)
                .thenCompose(clienteId -> veiculoGateway.adicionar(veiculo)
                        .thenApply(veiculoId -> {
                            var ordemDeServico = OrdemDeServicoFactory.criarNovo(clienteId, veiculoId);
                            var estadoAnterior = ordemDeServico.estadoDaOrdemDeServico();
                            ordemDeServico.iniciarDiagnostico();
                            return new OrdemAberta(ordemDeServico, estadoAnterior);
                        })
                        .thenCompose(ordemAberta -> incluirServicos(ordemAberta.ordemDeServico(), command.servicos())
                                .thenCompose(_ -> incluirPecas(ordemAberta.ordemDeServico(), command.pecas()))
                                .thenCompose(_ -> ordemDeServicoGateway.adicionar(ordemAberta.ordemDeServico()))
                                .thenCompose(_ -> transicaoDeEstadoDaOrdemDeServicoService.notificarMudancaSeHouver(
                                        ordemAberta.ordemDeServico(),
                                        ordemAberta.estadoAnterior()))
                                .thenAccept(_ -> ordemDeServicoPresenter.present(OrdemDeServicoDTO.fromDomain(ordemAberta.ordemDeServico())))));
    }

    private CompletableFuture<Void> incluirServicos(OrdemDeServico ordemDeServico, List<ServicoItemCommand> servicos) {
        return servicos.stream().reduce(
                CompletableFuture.completedFuture(null),
                (fluxo, servico) -> fluxo.thenCompose(_ -> catalogoGateway.buscaServicoPorId(servico.servicoId())
                        .thenAccept(servicoCatalogado -> ordemDeServico.adicionaServico(
                                servico.servicoId(),
                                servicoCatalogado.nome(),
                                servico.quantidade(),
                                servico.valorUnitario()))),
                (fluxo1, fluxo2) -> fluxo1.thenCompose(_ -> fluxo2));
    }

    private CompletableFuture<Void> incluirPecas(OrdemDeServico ordemDeServico, List<PecaItemCommand> pecas) {
        return pecas.stream().reduce(
                CompletableFuture.completedFuture(null),
                (fluxo, peca) -> fluxo.thenCompose(_ -> catalogoGateway.buscaPecaPorId(peca.pecaId())
                        .thenAccept(pecaCatalogada -> ordemDeServico.adicionaPeca(
                                peca.pecaId(),
                                pecaCatalogada.nome(),
                                peca.quantidade(),
                                peca.valorUnitario()))),
                (fluxo1, fluxo2) -> fluxo1.thenCompose(_ -> fluxo2));
    }

    public record Command(String documentoDoCliente,
                          String emailDoCliente,
                          String placaDoVeiculo,
                          String marcaDoVeiculo,
                          String modeloDoVeiculo,
                          int ano,
                          List<ServicoItemCommand> servicos,
                          List<PecaItemCommand> pecas) {
    }

    public record ServicoItemCommand(long servicoId, BigDecimal quantidade, BigDecimal valorUnitario) {
    }

    public record PecaItemCommand(long pecaId, BigDecimal quantidade, BigDecimal valorUnitario) {
    }

    private record OrdemAberta(OrdemDeServico ordemDeServico, TipoDeEstadoDaOrdemDeServico estadoAnterior) {
    }
}
