package br.com.oficina.atendimento.interfaces.presenters;

import br.com.oficina.atendimento.core.interfaces.presenter.OrdemDeServicoPresenter;
import br.com.oficina.atendimento.core.interfaces.presenter.dto.OrdemDeServicoDTO;
import br.com.oficina.atendimento.interfaces.presenters.view_model.OrcamentoViewModel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class OrcamentoPresenterAdapter implements OrdemDeServicoPresenter {

    private OrcamentoViewModel orcamentoViewModel;

    @Override public void present(OrdemDeServicoDTO ordemDeServicoDTO) {
        var itens = new ArrayList<OrcamentoViewModel.ItemView>();
        var totalPecas = new AtomicReference<>(BigDecimal.ZERO);
        ordemDeServicoDTO.pecas()
                .forEach(itemPeca -> {
                    itens.add(new OrcamentoViewModel.ItemView(
                            "PECA",
                            itemPeca.pecaNome(),
                            itemPeca.quantidade(),
                            itemPeca.valorUnitario(),
                            itemPeca.valorTotal()));
                    totalPecas.updateAndGet(total ->
                            total.add(nvl(itemPeca.valorTotal())));
                });

        var totalServicos = new AtomicReference<>(BigDecimal.ZERO);
        ordemDeServicoDTO.servicos()
                .forEach(itemServico -> {
                    itens.add(new OrcamentoViewModel.ItemView(
                            "SERVIÇO",
                            itemServico.servicoNome(),
                            itemServico.quantidade(),
                            itemServico.valorUnitario(),
                            itemServico.valorTotal()));
                    totalServicos.updateAndGet(total ->
                            total.add(nvl(itemServico.valorTotal())));
                });

        var total = totalPecas.get().add(totalServicos.get())
                .setScale(2, RoundingMode.HALF_UP);

        this.orcamentoViewModel = new OrcamentoViewModel(
                ordemDeServicoDTO.id(),
                ordemDeServicoDTO.clienteId(),
                itens,
                total);
    }

    private static BigDecimal nvl(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    public OrcamentoViewModel viewModel() {
        return orcamentoViewModel;
    }
}
