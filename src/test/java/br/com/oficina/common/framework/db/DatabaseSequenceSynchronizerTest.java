package br.com.oficina.common.framework.db;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseSequenceSynchronizerTest {

    @Test
    void deveSincronizarSequencesComIncrementoCompativelComHibernate() throws Exception {
        var syncSql = syncSql();

        assertTrue(syncSql.contains("EXECUTE 'ALTER SEQUENCE public.pessoa_seq INCREMENT BY 50';"));
        assertTrue(syncSql.contains("(SELECT COALESCE(MAX(id), 1) + 50 FROM public.pessoa)"));
        assertTrue(syncSql.contains("(SELECT last_value + 50 FROM public.pessoa_seq)"));
        assertTrue(syncSql.contains("false"));
    }

    private static String syncSql() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        var method = DatabaseSequenceSynchronizer.class.getDeclaredMethod("syncSql");
        method.setAccessible(true);
        return (String) method.invoke(null);
    }
}
