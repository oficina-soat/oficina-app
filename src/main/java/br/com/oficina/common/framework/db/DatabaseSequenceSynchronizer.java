package br.com.oficina.common.framework.db;

import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.util.List;

@ApplicationScoped
@Startup
@SuppressWarnings("deprecation")
public class DatabaseSequenceSynchronizer {

    private static final Logger LOG = Logger.getLogger(DatabaseSequenceSynchronizer.class);
    private static final Duration STARTUP_TIMEOUT = Duration.ofSeconds(30);
    private static final List<SequenceTarget> SEQUENCES = List.of(
            new SequenceTarget("pessoa_seq", "pessoa"),
            new SequenceTarget("papel_seq", "papel"),
            new SequenceTarget("usuario_seq", "usuario"),
            new SequenceTarget("cliente_seq", "cliente"),
            new SequenceTarget("veiculo_seq", "veiculo"),
            new SequenceTarget("estado_ordem_servico_seq", "estado_ordem_servico"),
            new SequenceTarget("peca_seq", "peca"),
            new SequenceTarget("os_item_peca_seq", "os_item_peca"),
            new SequenceTarget("servico_seq", "servico"),
            new SequenceTarget("os_item_servico_seq", "os_item_servico"),
            new SequenceTarget("estoque_movimento_seq", "estoque_movimento"));

    @Inject
    PgPool pgPool;

    @PostConstruct
    void onStart() {
        synchronizeBlocking();
        LOG.infof("Sequences sincronizadas com os IDs atuais de %d tabelas.", SEQUENCES.size());
    }

    public void synchronizeBlocking() {
        synchronize()
                .await()
                .atMost(STARTUP_TIMEOUT);
    }

    public Uni<Void> synchronize() {
        return pgPool.preparedQuery(syncSql())
                .execute()
                .replaceWithVoid();
    }

    private static String syncSql() {
        var statements = new StringBuilder("DO $$ BEGIN\n");
        for (var sequence : SEQUENCES) {
            statements.append("""
                    PERFORM setval(
                        'public.%s',
                        GREATEST(
                            (SELECT COALESCE(MAX(id), 1) FROM public.%s),
                            (SELECT last_value FROM public.%s)
                        ),
                        true
                    );
                    """.formatted(sequence.sequenceName(), sequence.tableName(), sequence.sequenceName()));
        }
        statements.append("END $$");
        return statements.toString();
    }

    private record SequenceTarget(String sequenceName, String tableName) {
    }
}
