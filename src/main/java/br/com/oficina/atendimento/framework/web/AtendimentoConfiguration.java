package br.com.oficina.atendimento.framework.web;

import br.com.oficina.atendimento.core.interfaces.gateway.CatalogoGateway;
import br.com.oficina.atendimento.core.interfaces.gateway.ClienteGateway;
import br.com.oficina.atendimento.core.interfaces.gateway.EstoqueGateway;
import br.com.oficina.atendimento.core.interfaces.gateway.OrdemDeServicoGateway;
import br.com.oficina.atendimento.core.interfaces.gateway.VeiculoGateway;
import br.com.oficina.atendimento.core.interfaces.gateway.ActionTokenGateway;
import br.com.oficina.atendimento.core.interfaces.presenter.ClientePresenter;
import br.com.oficina.atendimento.core.interfaces.presenter.VeiculoPresenter;
import br.com.oficina.atendimento.core.interfaces.sender.EstadoDaOrdemDeServicoSender;
import br.com.oficina.atendimento.core.interfaces.sender.OrcamentoSender;
import br.com.oficina.atendimento.core.usecases.cliente.AdicionarClienteUseCase;
import br.com.oficina.atendimento.core.usecases.cliente.ApagarClienteUseCase;
import br.com.oficina.atendimento.core.usecases.cliente.AtualizarClienteUseCase;
import br.com.oficina.atendimento.core.usecases.cliente.BuscarClienteUseCase;
import br.com.oficina.atendimento.core.usecases.cliente.ListarClientesUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.AbrirOrdemDeServicoCompletaUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.AcompanharOrdemDeServicoUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.AprovarOrdemDeServicoUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.ConsultarHistoricoDeEstadoUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.CriarOrdemDeServicoUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.EntregarOrdemDeServicoUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.FinalizarDiagnosticoUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.FinalizarOrdemDeServicoUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.IncluirPecaUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.IncluirServicoUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.IniciarDiagnosticoUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.ListarOrdemDeServicoUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.RecusarOrdemDeServicoUseCase;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.TransicaoDeEstadoDaOrdemDeServicoService;
import br.com.oficina.atendimento.core.usecases.ordem_de_servico.ValidarMagicLinkUseCase;
import br.com.oficina.atendimento.core.usecases.veiculo.AdicionarVeiculoUseCase;
import br.com.oficina.atendimento.core.usecases.veiculo.ApagarVeiculoUseCase;
import br.com.oficina.atendimento.core.usecases.veiculo.AtualizarVeiculoUseCase;
import br.com.oficina.atendimento.core.usecases.veiculo.BuscarVeiculoUseCase;
import br.com.oficina.atendimento.interfaces.controllers.ClienteCommandController;
import br.com.oficina.atendimento.interfaces.controllers.ClienteQueryController;
import br.com.oficina.atendimento.interfaces.controllers.OrdemDeServicoCommandController;
import br.com.oficina.atendimento.interfaces.controllers.OrdemDeServicoMagicLinkController;
import br.com.oficina.atendimento.interfaces.controllers.OrdemDeServicoQueryController;
import br.com.oficina.atendimento.interfaces.controllers.VeiculoCommandController;
import br.com.oficina.atendimento.interfaces.controllers.VeiculoQueryController;
import br.com.oficina.atendimento.interfaces.presenters.AcompanharOrdemDeServicoPresenterAdapter;
import br.com.oficina.atendimento.interfaces.presenters.ClientePresenterAdapter;
import br.com.oficina.atendimento.interfaces.presenters.EstadoAtualOrdemDeServicoPresenterAdapter;
import br.com.oficina.atendimento.interfaces.presenters.HistoricoEstadoPresenterAdapter;
import br.com.oficina.atendimento.interfaces.presenters.IdentificadorOrdemDeServicoPresenterAdapter;
import br.com.oficina.atendimento.interfaces.presenters.ListarOrdemDeServicoPresenterAdapter;
import br.com.oficina.atendimento.interfaces.presenters.MagicLinkAcompanhamentoPresenterAdapter;
import br.com.oficina.atendimento.interfaces.presenters.MagicLinkConfirmacaoPresenterAdapter;
import br.com.oficina.atendimento.interfaces.presenters.MagicLinkResultadoPresenterAdapter;
import br.com.oficina.atendimento.interfaces.presenters.OrcamentoPresenterAdapter;
import br.com.oficina.atendimento.interfaces.presenters.VeiculoPresenterAdapter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class AtendimentoConfiguration {

    @Produces ClienteCommandController clienteCommandController(ClienteGateway clienteGateway) {
        return new ClienteCommandController(
                new AdicionarClienteUseCase(clienteGateway),
                new AtualizarClienteUseCase(clienteGateway),
                new ApagarClienteUseCase(clienteGateway));
    }

    @Produces ClienteQueryController clienteQueryController(ClienteGateway clienteGateway,
                                                            ClientePresenter clientePresenter) {
        return new ClienteQueryController(
                new BuscarClienteUseCase(clienteGateway, clientePresenter),
                new ListarClientesUseCase(clienteGateway, clientePresenter));
    }

    @Produces @RequestScoped ClientePresenterAdapter clientePresenter() {
        return new ClientePresenterAdapter();
    }

    @Produces VeiculoCommandController veiculoCommandController(VeiculoGateway veiculoGateway) {
        return new VeiculoCommandController(
                new AdicionarVeiculoUseCase(veiculoGateway),
                new AtualizarVeiculoUseCase(veiculoGateway),
                new ApagarVeiculoUseCase(veiculoGateway));
    }

    @Produces VeiculoQueryController veiculoQueryController(VeiculoGateway veiculoGateway,
                                                            VeiculoPresenter veiculoPresenter) {
        return new VeiculoQueryController(
                new BuscarVeiculoUseCase(veiculoGateway, veiculoPresenter));
    }

    @Produces @RequestScoped VeiculoPresenterAdapter veiculoPresenter() {
        return new VeiculoPresenterAdapter();
    }

    @Produces OrdemDeServicoCommandController ordemDeServicoCommandController(OrdemDeServicoGateway ordemDeServicoGateway,
                                                                              EstoqueGateway estoqueGateway,
                                                                              OrcamentoSender orcamentoSender,
                                                                              EstadoDaOrdemDeServicoSender estadoDaOrdemDeServicoSender,
                                                                              ClienteGateway clienteGateway,
                                                                              VeiculoGateway veiculoGateway,
                                                                              CatalogoGateway catalogoGateway,
                                                                              IdentificadorOrdemDeServicoPresenterAdapter identificadorOrdemDeServicoPresenterAdapter,
                                                                              OrcamentoPresenterAdapter orcamentoPresenterAdapter) {
        var transicaoDeEstadoDaOrdemDeServicoService = new TransicaoDeEstadoDaOrdemDeServicoService(
                ordemDeServicoGateway,
                estadoDaOrdemDeServicoSender);
        return new OrdemDeServicoCommandController(
                new AbrirOrdemDeServicoCompletaUseCase(ordemDeServicoGateway, clienteGateway, veiculoGateway, catalogoGateway, identificadorOrdemDeServicoPresenterAdapter, transicaoDeEstadoDaOrdemDeServicoService),
                new AprovarOrdemDeServicoUseCase(transicaoDeEstadoDaOrdemDeServicoService),
                new CriarOrdemDeServicoUseCase(ordemDeServicoGateway, clienteGateway, veiculoGateway, identificadorOrdemDeServicoPresenterAdapter),
                new EntregarOrdemDeServicoUseCase(transicaoDeEstadoDaOrdemDeServicoService),
                new FinalizarDiagnosticoUseCase(transicaoDeEstadoDaOrdemDeServicoService, estoqueGateway, clienteGateway, orcamentoPresenterAdapter, orcamentoSender),
                new FinalizarOrdemDeServicoUseCase(transicaoDeEstadoDaOrdemDeServicoService),
                new IniciarDiagnosticoUseCase(transicaoDeEstadoDaOrdemDeServicoService),
                new IncluirPecaUseCase(ordemDeServicoGateway, catalogoGateway),
                new IncluirServicoUseCase(ordemDeServicoGateway, catalogoGateway),
                new RecusarOrdemDeServicoUseCase(transicaoDeEstadoDaOrdemDeServicoService));
    }

    @Produces OrdemDeServicoQueryController ordemDeServicoQueryController(OrdemDeServicoGateway ordemDeServicoGateway,
                                                                          AcompanharOrdemDeServicoPresenterAdapter acompanharOrdemDeServicoPresenterAdapter,
                                                                          ListarOrdemDeServicoPresenterAdapter listarOrdemDeServicoPresenterAdapter,
                                                                          HistoricoEstadoPresenterAdapter historicoEstadoPresenterAdapter) {
        return new OrdemDeServicoQueryController(
                new AcompanharOrdemDeServicoUseCase(ordemDeServicoGateway, acompanharOrdemDeServicoPresenterAdapter),
                new ListarOrdemDeServicoUseCase(ordemDeServicoGateway, listarOrdemDeServicoPresenterAdapter),
                new ConsultarHistoricoDeEstadoUseCase(ordemDeServicoGateway, historicoEstadoPresenterAdapter));
    }

    @Produces OrdemDeServicoMagicLinkController ordemDeServicoMagicLinkController(ActionTokenGateway actionTokenGateway,
                                                                                  OrdemDeServicoGateway ordemDeServicoGateway,
                                                                                  EstadoDaOrdemDeServicoSender estadoDaOrdemDeServicoSender,
                                                                                  AcompanharOrdemDeServicoPresenterAdapter acompanharOrdemDeServicoPresenterAdapter,
                                                                                  MagicLinkAcompanhamentoPresenterAdapter magicLinkAcompanhamentoPresenterAdapter,
                                                                                  MagicLinkConfirmacaoPresenterAdapter magicLinkConfirmacaoPresenterAdapter,
                                                                                  MagicLinkResultadoPresenterAdapter magicLinkResultadoPresenterAdapter) {
        var transicaoDeEstadoDaOrdemDeServicoService = new TransicaoDeEstadoDaOrdemDeServicoService(
                ordemDeServicoGateway,
                estadoDaOrdemDeServicoSender);
        return new OrdemDeServicoMagicLinkController(
                new ValidarMagicLinkUseCase(actionTokenGateway),
                new AcompanharOrdemDeServicoUseCase(ordemDeServicoGateway, acompanharOrdemDeServicoPresenterAdapter),
                new AprovarOrdemDeServicoUseCase(transicaoDeEstadoDaOrdemDeServicoService),
                new RecusarOrdemDeServicoUseCase(transicaoDeEstadoDaOrdemDeServicoService),
                acompanharOrdemDeServicoPresenterAdapter,
                magicLinkAcompanhamentoPresenterAdapter,
                magicLinkConfirmacaoPresenterAdapter,
                magicLinkResultadoPresenterAdapter);
    }

    @Produces @RequestScoped AcompanharOrdemDeServicoPresenterAdapter ordemDeServicoPresenter() {
        return new AcompanharOrdemDeServicoPresenterAdapter();
    }

    @Produces @RequestScoped EstadoAtualOrdemDeServicoPresenterAdapter estadoAtualOrdemDeServicoPresenterAdapter() {
        return new EstadoAtualOrdemDeServicoPresenterAdapter();
    }

    @Produces @RequestScoped ListarOrdemDeServicoPresenterAdapter listarOrdemDeServicoPresenter() {
        return new ListarOrdemDeServicoPresenterAdapter();
    }

    @Produces @RequestScoped HistoricoEstadoPresenterAdapter historicoEstadoPresenterAdapter() {
        return new HistoricoEstadoPresenterAdapter();
    }

    @Produces @RequestScoped IdentificadorOrdemDeServicoPresenterAdapter aberturaDeOrdemDeServicoPresenter() {
        return new IdentificadorOrdemDeServicoPresenterAdapter();
    }

    @Produces @RequestScoped OrcamentoPresenterAdapter orcamentoPresenterAdapter() {
        return new OrcamentoPresenterAdapter();
    }

    @Produces @RequestScoped MagicLinkConfirmacaoPresenterAdapter magicLinkConfirmacaoPresenterAdapter() {
        return new MagicLinkConfirmacaoPresenterAdapter();
    }

    @Produces @RequestScoped MagicLinkResultadoPresenterAdapter magicLinkResultadoPresenterAdapter() {
        return new MagicLinkResultadoPresenterAdapter();
    }

    @Produces @RequestScoped MagicLinkAcompanhamentoPresenterAdapter magicLinkAcompanhamentoPresenterAdapter() {
        return new MagicLinkAcompanhamentoPresenterAdapter();
    }
}
