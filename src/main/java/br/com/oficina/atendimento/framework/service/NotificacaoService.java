package br.com.oficina.atendimento.framework.service;

import br.com.oficina.common.framework.observability.AppObservability;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

@ApplicationScoped
public class NotificacaoService {

    private static final String INTEGRATION_NAME = "notificacao";
    private static final String INTEGRATION_OPERATION = "enviar_email";

    private final NotificacaoClient notificacaoClient;
    private final boolean mock;
    @Inject
    AppObservability appObservability = AppObservability.noop();
    @Inject
    Tracer tracer = GlobalOpenTelemetry.getTracer("oficina-app");

    public NotificacaoService(@RestClient NotificacaoClient notificacaoClient,
                              @ConfigProperty(name = "oficina.notificacao.mock", defaultValue = "false") boolean mock) {
        this.notificacaoClient = notificacaoClient;
        this.mock = mock;
    }

    public CompletableFuture<Void> enviar(String mensagem, String assunto, String emailDestino) {
        if (mock) {
            return CompletableFuture.completedFuture(null);
        }

        long startedAt = System.nanoTime();
        Span span = tracer.spanBuilder("notificacao.enviar_email")
                .setSpanKind(SpanKind.CLIENT)
                .startSpan();
        span.setAttribute("integration.name", INTEGRATION_NAME);
        span.setAttribute("integration.operation", INTEGRATION_OPERATION);

        try (Scope ignored = span.makeCurrent()) {
            return notificacaoClient.enviarEmail(new NotificacaoClient.EnviarEmailRequest(
                            emailDestino,
                            assunto,
                            mensagem))
                    .onItem().invoke(() -> {
                        span.setAttribute("integration.status", "success");
                        appObservability.onIntegrationSuccess(
                                INTEGRATION_NAME,
                                INTEGRATION_OPERATION,
                                elapsedMillis(startedAt));
                    })
                    .onFailure().invoke(throwable -> {
                        span.setAttribute("integration.status", "failure");
                        span.recordException(throwable);
                        span.setStatus(StatusCode.ERROR);
                        appObservability.onIntegrationFailure(
                                INTEGRATION_NAME,
                                INTEGRATION_OPERATION,
                                classifyFailure(throwable),
                                elapsedMillis(startedAt),
                                unwrap(throwable));
                    })
                    .eventually(() -> span.end())
                    .replaceWithVoid()
                    .subscribeAsCompletionStage()
                    .toCompletableFuture();
        }
    }

    private static long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000L;
    }

    private static Throwable unwrap(Throwable throwable) {
        if (throwable instanceof CompletionException completionException && completionException.getCause() != null) {
            return completionException.getCause();
        }
        return throwable;
    }

    private static String classifyFailure(Throwable throwable) {
        Throwable cause = unwrap(throwable);
        if (cause instanceof NotificacaoClientException exception) {
            if (exception.statusCode() >= 400 && exception.statusCode() < 500) {
                return "upstream_4xx";
            }
            if (exception.statusCode() >= 500) {
                return "upstream_5xx";
            }
            return "unexpected_response";
        }
        if (cause instanceof TimeoutException || cause instanceof SocketTimeoutException) {
            return "timeout";
        }
        if (cause instanceof ConnectException || cause instanceof UnknownHostException) {
            return "connection_error";
        }
        return "internal_error";
    }
}
