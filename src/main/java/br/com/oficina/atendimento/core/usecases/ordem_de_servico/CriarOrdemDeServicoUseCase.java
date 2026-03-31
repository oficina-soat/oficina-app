package br.com.oficina.atendimento.core.usecases.ordem_de_servico;

import br.com.oficina.atendimento.core.entities.cliente.DocumentoFactory;
import br.com.oficina.atendimento.core.entities.ordem_de_servico.OrdemDeServicoFactory;
import br.com.oficina.atendimento.core.entities.veiculo.PlacaDeVeiculo;
import br.com.oficina.atendimento.core.interfaces.gateway.ClienteGateway;
import br.com.oficina.atendimento.core.interfaces.gateway.OrdemDeServicoGateway;
import br.com.oficina.atendimento.core.interfaces.gateway.VeiculoGateway;

import java.util.concurrent.CompletableFuture;

public class CriarOrdemDeServicoUseCase {

    private final OrdemDeServicoGateway ordemDeServicoGateway;

    private final VeiculoGateway veiculoGateway;
    private final ClienteGateway clienteGateway;

    public CriarOrdemDeServicoUseCase(OrdemDeServicoGateway ordemDeServicoGateway, ClienteGateway clienteGateway, VeiculoGateway veiculoGateway) {
        this.ordemDeServicoGateway = ordemDeServicoGateway;
        this.clienteGateway = clienteGateway;
        this.veiculoGateway = veiculoGateway;
    }

    public CompletableFuture<Void> executar(Command command) {
        var documento = DocumentoFactory.from(command.documentoDoCliente());
        var placa = new PlacaDeVeiculo(command.placaDoVeiculo());
        return clienteGateway.buscarPorDocumento(documento)
                .thenCompose(cliente -> veiculoGateway.buscarPorPlaca(placa)
                        .thenCompose(veiculo -> {
                            var ordemDeServico = OrdemDeServicoFactory.criarNovo(
                                    cliente.id(),
                                    veiculo.id());
                            return ordemDeServicoGateway.adicionar(ordemDeServico);
                        }));
    }

    public record Command(String documentoDoCliente, String placaDoVeiculo) {
    }
}

