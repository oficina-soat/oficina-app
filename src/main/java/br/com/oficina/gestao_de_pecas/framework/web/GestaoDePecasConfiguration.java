package br.com.oficina.gestao_de_pecas.framework.web;

import br.com.oficina.gestao_de_pecas.core.interfaces.EstoqueGateway;
import br.com.oficina.gestao_de_pecas.core.interfaces.PecaGateway;
import br.com.oficina.gestao_de_pecas.core.interfaces.PecaPresenter;
import br.com.oficina.gestao_de_pecas.core.interfaces.ServicoGateway;
import br.com.oficina.gestao_de_pecas.core.interfaces.ServicoPresenter;
import br.com.oficina.gestao_de_pecas.core.usecases.catalogo.peca.AdicionarPecaUseCase;
import br.com.oficina.gestao_de_pecas.core.usecases.catalogo.peca.ApagarPecaUseCase;
import br.com.oficina.gestao_de_pecas.core.usecases.catalogo.peca.AtualizarPecaUseCase;
import br.com.oficina.gestao_de_pecas.core.usecases.catalogo.peca.BuscarPecaUseCase;
import br.com.oficina.gestao_de_pecas.core.usecases.catalogo.servico.AdicionarServicoUseCase;
import br.com.oficina.gestao_de_pecas.core.usecases.catalogo.servico.ApagarServicoUseCase;
import br.com.oficina.gestao_de_pecas.core.usecases.catalogo.servico.AtualizarServicoUseCase;
import br.com.oficina.gestao_de_pecas.core.usecases.catalogo.servico.BuscarServicoUseCase;
import br.com.oficina.gestao_de_pecas.core.usecases.estoque.AcrescentarEstoqueUseCase;
import br.com.oficina.gestao_de_pecas.core.usecases.estoque.BaixarEstoquePorConsumoUseCase;
import br.com.oficina.gestao_de_pecas.interfaces.controllers.EstoqueController;
import br.com.oficina.gestao_de_pecas.interfaces.controllers.PecaController;
import br.com.oficina.gestao_de_pecas.interfaces.controllers.ServicoController;
import br.com.oficina.gestao_de_pecas.interfaces.presenters.PecaPresenterAdapter;
import br.com.oficina.gestao_de_pecas.interfaces.presenters.ServicoPresenterAdapter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class GestaoDePecasConfiguration {

    @Produces PecaController pecaController(PecaGateway pecaGateway,
                                            EstoqueGateway estoqueGateway,
                                            PecaPresenter pecaPresenter) {
        return new PecaController(
                new AdicionarPecaUseCase(pecaGateway, estoqueGateway),
                new BuscarPecaUseCase(pecaGateway, pecaPresenter),
                new AtualizarPecaUseCase(pecaGateway),
                new ApagarPecaUseCase(pecaGateway, estoqueGateway));
    }

    @Produces @RequestScoped PecaPresenterAdapter pecaPresenter() {
        return new PecaPresenterAdapter();
    }

    @Produces ServicoController servicoController(ServicoGateway servicoGateway,
                                                  ServicoPresenter servicoPresenter) {
        return new ServicoController(
                new AdicionarServicoUseCase(servicoGateway),
                new BuscarServicoUseCase(servicoGateway, servicoPresenter),
                new AtualizarServicoUseCase(servicoGateway),
                new ApagarServicoUseCase(servicoGateway));
    }

    @Produces @RequestScoped ServicoPresenterAdapter servicoPresenter() {
        return new ServicoPresenterAdapter();
    }

    @Produces EstoqueController estoqueController(EstoqueGateway estoqueGateway) {
        return new EstoqueController(
                new AcrescentarEstoqueUseCase(estoqueGateway),
                new BaixarEstoquePorConsumoUseCase(estoqueGateway));
    }
}
