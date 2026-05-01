package br.com.oficina.common.framework.observability;

import br.com.oficina.atendimento.core.entities.ordem_de_servico.TipoDeEstadoDaOrdemDeServico;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.DistributionSummary;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class QuarkusAppObservability implements AppObservability {

    private static final Logger LOG = Logger.getLogger(QuarkusAppObservability.class);

    private final MeterRegistry meterRegistry;
    private final boolean enabled;
    private final boolean metricsEnabled;
    private final boolean tracingEnabled;
    private final String serviceName;
    private final String deploymentEnvironment;

    public QuarkusAppObservability(
            MeterRegistry meterRegistry,
            @ConfigProperty(name = "oficina.observability.enabled", defaultValue = "true") boolean enabled,
            @ConfigProperty(name = "oficina.observability.metrics.enabled", defaultValue = "true") boolean metricsEnabled,
            @ConfigProperty(name = "oficina.observability.tracing.enabled", defaultValue = "true") boolean tracingEnabled,
            @ConfigProperty(name = "quarkus.application.name", defaultValue = "oficina-app") String serviceName,
            @ConfigProperty(name = "oficina.observability.deployment-environment", defaultValue = "lab") String deploymentEnvironment) {
        this.meterRegistry = meterRegistry;
        this.enabled = enabled;
        this.metricsEnabled = metricsEnabled;
        this.tracingEnabled = tracingEnabled;
        this.serviceName = serviceName;
        this.deploymentEnvironment = deploymentEnvironment;
    }

    @Override
    public void onOrderCreated(UUID ordemDeServicoId, TipoDeEstadoDaOrdemDeServico statusAtual) {
        if (!enabled) {
            return;
        }

        recordCounter("os_created_total", Tags.of(
                "service", serviceName,
                "env", deploymentEnvironment));

        annotateCurrentSpan(Map.of(
                "ordem_servico.id", ordemDeServicoId.toString(),
                "ordem_servico.status", statusAtual.name()));

        var fields = baseFields();
        fields.put("ordem_servico_id", ordemDeServicoId);
        fields.put("ordem_servico_status", statusAtual.name());
        try (var ignored = ObservabilityMdcScope.with(fields)) {
            LOG.info("Ordem de servico criada");
        }
    }

    @Override
    public void onOrderTransition(UUID ordemDeServicoId,
                                  TipoDeEstadoDaOrdemDeServico statusAnterior,
                                  TipoDeEstadoDaOrdemDeServico statusNovo,
                                  Instant statusAnteriorDesde,
                                  Instant statusNovoDesde) {
        if (!enabled) {
            return;
        }

        long durationMs = Math.max(0L, Duration.between(statusAnteriorDesde, statusNovoDesde).toMillis());

        recordCounter("os_status_transition_total", Tags.of(
                "service", serviceName,
                "env", deploymentEnvironment,
                "from_status", statusAnterior.name(),
                "to_status", statusNovo.name()));

        recordHistogram("os_status_duration_ms", Tags.of(
                "service", serviceName,
                "env", deploymentEnvironment,
                "status", statusAnterior.name()), durationMs);

        annotateCurrentSpan(Map.of(
                "ordem_servico.id", ordemDeServicoId.toString(),
                "ordem_servico.status", statusNovo.name(),
                "ordem_servico.status_anterior", statusAnterior.name(),
                "ordem_servico.status_novo", statusNovo.name()));

        var fields = baseFields();
        fields.put("ordem_servico_id", ordemDeServicoId);
        fields.put("ordem_servico_status", statusNovo.name());
        fields.put("ordem_servico_status_anterior", statusAnterior.name());
        fields.put("ordem_servico_status_novo", statusNovo.name());
        fields.put("ordem_servico_status_duration_ms", durationMs);
        try (var ignored = ObservabilityMdcScope.with(fields)) {
            LOG.info("Transicao de ordem de servico concluida");
        }
    }

    @Override
    public void onIntegrationSuccess(String integration, String operation, long latencyMs) {
        if (!enabled) {
            return;
        }

        recordHistogram("integration_latency_ms", Tags.of(
                "service", serviceName,
                "env", deploymentEnvironment,
                "integration", integration,
                "operation", operation), latencyMs);

        annotateCurrentSpan(Map.of(
                "integration.name", integration,
                "integration.operation", operation,
                "integration.status", "success"));
    }

    @Override
    public void onIntegrationFailure(String integration,
                                     String operation,
                                     String failureType,
                                     long latencyMs,
                                     Throwable throwable) {
        if (!enabled) {
            return;
        }

        recordHistogram("integration_latency_ms", Tags.of(
                "service", serviceName,
                "env", deploymentEnvironment,
                "integration", integration,
                "operation", operation), latencyMs);

        recordCounter("integration_failures_total", Tags.of(
                "service", serviceName,
                "env", deploymentEnvironment,
                "integration", integration,
                "operation", operation,
                "failure_type", failureType));

        var span = Span.current();
        if (tracingEnabled) {
            span.setAttribute("integration.name", integration);
            span.setAttribute("integration.operation", operation);
            span.setAttribute("integration.status", "failure");
            span.recordException(throwable);
            span.setStatus(StatusCode.ERROR);
        }

        var fields = baseFields();
        fields.put("integration.name", integration);
        fields.put("integration.operation", operation);
        fields.put("integration.status", "failure");
        fields.put("integration_name", integration);
        fields.put("integration_operation", operation);
        fields.put("integration_status", "failure");
        fields.put("integration_failure_type", failureType);
        fields.put("error.type", throwable.getClass().getSimpleName());
        fields.put("error.message", throwable.getMessage());
        fields.putAll(ObservabilityMdcScope.currentTraceContextFields());
        try (var ignored = ObservabilityMdcScope.with(fields)) {
            LOG.error("Falha em integracao externa", throwable);
        }
    }

    private Map<String, Object> baseFields() {
        var fields = new LinkedHashMap<String, Object>();
        fields.putAll(ObservabilityMdcScope.currentTraceContextFields());
        return fields;
    }

    private void recordCounter(String name, Tags tags) {
        if (!metricsEnabled) {
            return;
        }
        Counter.builder(name).tags(tags).register(meterRegistry).increment();
    }

    private void recordHistogram(String name, Tags tags, long value) {
        if (!metricsEnabled) {
            return;
        }
        DistributionSummary.builder(name)
                .baseUnit("milliseconds")
                .tags(tags)
                .register(meterRegistry)
                .record(value);
    }

    private void annotateCurrentSpan(Map<String, String> attributes) {
        if (!tracingEnabled) {
            return;
        }
        Span span = Span.current();
        attributes.forEach(span::setAttribute);
    }
}
