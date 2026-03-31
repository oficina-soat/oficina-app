package br.com.oficina.atendimento.framework.web.ordem_de_servico;

public enum MagicLinkActionPage {
    APROVAR(
            "aprovar-link",
            "Aprovar orçamento",
            "Confirme a aprovação do orçamento da ordem de serviço.",
            "Confirmar aprovação",
            "Orçamento aprovado",
            "A aprovação do orçamento foi concluída com sucesso.",
            "Não foi possível aprovar o orçamento"),
    RECUSAR(
            "recusar-link",
            "Recusar orçamento",
            "Confirme a recusa do orçamento da ordem de serviço.",
            "Confirmar recusa",
            "Orçamento recusado",
            "A recusa do orçamento foi concluída com sucesso.",
            "Não foi possível recusar o orçamento"),
    ACOMPANHAR(
            "acompanhar-link",
            "Acompanhar ordem de serviço",
            "Consulte o estado atual da ordem de serviço.",
            "",
            "",
            "",
            "Não foi possível acompanhar a ordem de serviço");

    private final String pathSegment;
    private final String tituloConfirmacao;
    private final String textoConfirmacao;
    private final String botaoConfirmacao;
    private final String tituloSucesso;
    private final String textoSucesso;
    private final String tituloErro;

    MagicLinkActionPage(String pathSegment,
                        String tituloConfirmacao,
                        String textoConfirmacao,
                        String botaoConfirmacao,
                        String tituloSucesso,
                        String textoSucesso,
                        String tituloErro) {
        this.pathSegment = pathSegment;
        this.tituloConfirmacao = tituloConfirmacao;
        this.textoConfirmacao = textoConfirmacao;
        this.botaoConfirmacao = botaoConfirmacao;
        this.tituloSucesso = tituloSucesso;
        this.textoSucesso = textoSucesso;
        this.tituloErro = tituloErro;
    }

    public String pathSegment() {
        return pathSegment;
    }

    public String tituloConfirmacao() {
        return tituloConfirmacao;
    }

    public String textoConfirmacao() {
        return textoConfirmacao;
    }

    public String botaoConfirmacao() {
        return botaoConfirmacao;
    }

    public String tituloSucesso() {
        return tituloSucesso;
    }

    public String textoSucesso() {
        return textoSucesso;
    }

    public String tituloErro() {
        return tituloErro;
    }

    public static MagicLinkActionPage fromPath(String path) {
        if (path == null) {
            return null;
        }
        if (path.contains("/aprovar-link")) {
            return APROVAR;
        }
        if (path.contains("/recusar-link")) {
            return RECUSAR;
        }
        if (path.contains("/acompanhar-link")) {
            return ACOMPANHAR;
        }
        return null;
    }
}
