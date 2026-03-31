package br.com.oficina.atendimento.interfaces.presenters.view_model;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrcamentoViewModel(
        UUID ordemServicoId,
        long clienteId,
        List<ItemView> itens,
        BigDecimal total) {
    public String texto() {
        var sb = new StringBuilder();
        sb.append("Orçamento - OS ").append(this.ordemServicoId()).append("\n");
        sb.append("Cliente: ").append(this.clienteId()).append("\n\n");
        sb.append("Itens:\n");

        for (var itemView : this.itens()) {
            sb.append("- [").append(itemView.tipo()).append("] ")
                    .append(itemView.descricao()).append(" | qtd=").append(itemView.quantidade())
                    .append(" | unit=").append(itemView.valorUnitario())
                    .append(" | totalItems=").append(itemView.valorTotal())
                    .append("\n");
        }

        sb.append("\nTOTAL: ").append(this.total()).append("\n");
        sb.append("\nObservação: este orçamento pode sofrer alterações até a aprovação.\n");
        return sb.toString();
    }

    public record ItemView(
            String tipo,
            String descricao,
            BigDecimal quantidade,
            BigDecimal valorUnitario,
            BigDecimal valorTotal) {
    }
}
