package br.com.oficina.atendimento.core.interfaces.presenter.dto;

import br.com.oficina.atendimento.core.entities.veiculo.Veiculo;

public record VeiculoDTO(
        long id,
        String placa,
        String marca,
        String modelo,
        int ano) {
    public static VeiculoDTO fromDomain(Veiculo veiculo) {
        return new VeiculoDTO(
                veiculo.id(),
                veiculo.placa().valor(),
                veiculo.marca().valor(),
                veiculo.modelo().valor(),
                veiculo.ano());
    }
}
