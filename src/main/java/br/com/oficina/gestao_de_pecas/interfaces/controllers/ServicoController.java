package br.com.oficina.gestao_de_pecas.interfaces.controllers;

import br.com.oficina.gestao_de_pecas.core.usecases.catalogo.servico.AdicionarServicoUseCase;
import br.com.oficina.gestao_de_pecas.core.usecases.catalogo.servico.ApagarServicoUseCase;
import br.com.oficina.gestao_de_pecas.core.usecases.catalogo.servico.AtualizarServicoUseCase;
import br.com.oficina.gestao_de_pecas.core.usecases.catalogo.servico.BuscarServicoUseCase;

import java.util.concurrent.CompletableFuture;

public class ServicoController {
    private final AdicionarServicoUseCase adicionarServicoUseCase;
    private final BuscarServicoUseCase buscarServicoUseCase;
    private final AtualizarServicoUseCase atualizarServicoUseCase;
    private final ApagarServicoUseCase apagarServicoUseCase;

    public ServicoController(AdicionarServicoUseCase adicionarServicoUseCase,
                             BuscarServicoUseCase buscarServicoUseCase,
                             AtualizarServicoUseCase atualizarServicoUseCase,
                             ApagarServicoUseCase apagarServicoUseCase) {
        this.adicionarServicoUseCase = adicionarServicoUseCase;
        this.buscarServicoUseCase = buscarServicoUseCase;
        this.atualizarServicoUseCase = atualizarServicoUseCase;
        this.apagarServicoUseCase = apagarServicoUseCase;
    }

    public CompletableFuture<Void> adicionarServico(ServicoController.ServicoRequest servico) {
        var command = new AdicionarServicoUseCase.Command(servico.nome());
        return adicionarServicoUseCase.executar(command);
    }

    public CompletableFuture<Void> buscar(Long id) {
        var command = new BuscarServicoUseCase.Command(id);
        return buscarServicoUseCase.executar(command);
    }

    public CompletableFuture<Void> atualizarServico(Long id, ServicoController.ServicoRequest servicoRequest) {
        var command = new AtualizarServicoUseCase.Command(id, servicoRequest.nome());
        return atualizarServicoUseCase.executar(command);
    }

    public CompletableFuture<Void> excluirServico(Long id) {
        var command = new ApagarServicoUseCase.Command(id);
        return apagarServicoUseCase.executar(command);
    }

    public record ServicoRequest(String nome) {
    }
}
