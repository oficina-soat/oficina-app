package br.com.oficina.atendimento.framework.service;

public class NotificacaoClientException extends RuntimeException {

    private final int statusCode;
    private final String responseBody;

    public NotificacaoClientException(String operacao, int statusCode, String responseBody) {
        super("Falha ao invocar serviço de notificação em %s: status=%d, body=%s"
                .formatted(operacao, statusCode, responseBody));
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int statusCode() {
        return statusCode;
    }

    public String responseBody() {
        return responseBody;
    }
}
