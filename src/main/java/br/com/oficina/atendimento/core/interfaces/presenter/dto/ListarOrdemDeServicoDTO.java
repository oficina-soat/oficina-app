package br.com.oficina.atendimento.core.interfaces.presenter.dto;

import br.com.oficina.common.PageResult;

public record ListarOrdemDeServicoDTO(PageResult<OrdemDeServicoDTO> pageResult) {
}
