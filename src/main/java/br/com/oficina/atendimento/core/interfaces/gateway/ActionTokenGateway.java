package br.com.oficina.atendimento.core.interfaces.gateway;

import br.com.oficina.atendimento.core.entities.ordem_de_servico.AcaoDeMagicLink;

import java.util.UUID;

public interface ActionTokenGateway {

    void validarOuFalhar(String token, AcaoDeMagicLink acao, UUID ordemDeServicoId);
}
