package br.com.oficina.atendimento.framework.db.ordem_de_servico;

import br.com.oficina.atendimento.core.entities.ordem_de_servico.OrdemDeServicoFactory;
import br.com.oficina.atendimento.core.entities.ordem_de_servico.TipoDeEstadoDaOrdemDeServico;
import br.com.oficina.atendimento.core.exceptions.OrdemDeServicoNaoEncontradaException;
import br.com.oficina.atendimento.core.interfaces.presenter.dto.ItemPecaDTO;
import br.com.oficina.atendimento.core.interfaces.presenter.dto.ItemServicoDTO;
import br.com.oficina.atendimento.framework.db.ordem_de_servico.entities.OrdemDeServicoEntity;
import br.com.oficina.atendimento.framework.db.ordem_de_servico.entities.OsItemPecaEntity;
import br.com.oficina.atendimento.framework.db.ordem_de_servico.entities.OsItemServicoEntity;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrdemDeServicoDataSourceAdapterTest {

    @Test
    void deveValidarOrdemNaoEncontradaEMapearColecoesVazias() throws Exception {
        var validar = method("validarOrdemDeServicoEncontrada", OrdemDeServicoEntity.class);
        var erro = assertThrows(Exception.class, () -> validar.invoke(null, new Object[]{null}));
        assertInstanceOf(OrdemDeServicoNaoEncontradaException.class, erro.getCause());

        var getPecas = method("getPecas", OrdemDeServicoEntity.class);
        var getServicos = method("getServicos", OrdemDeServicoEntity.class);
        var getHistorico = method("getHistoricoDeEstados", OrdemDeServicoEntity.class);
        var entity = new OrdemDeServicoEntity();
        entity.pecas = new HashSet<>();
        entity.servicos = new HashSet<>();
        entity.historicoDeEstados = new HashSet<>();

        assertTrue(((java.util.List<?>) getPecas.invoke(null, entity)).isEmpty());
        assertTrue(((java.util.List<?>) getServicos.invoke(null, entity)).isEmpty());
        assertTrue(((java.util.List<?>) getHistorico.invoke(null, entity)).isEmpty());
    }


    @Test
    void deveRetornarMesmaEntidadeQuandoOrdemEncontrada() throws Exception {
        var validar = method("validarOrdemDeServicoEncontrada", OrdemDeServicoEntity.class);
        var entity = new OrdemDeServicoEntity();

        var resultado = validar.invoke(null, entity);

        assertSame(entity, resultado);
    }

    @Test
    void deveAtualizarEstadoPecasEServicos() throws Exception {
        var entity = new OrdemDeServicoEntity();
        entity.estadoAtual = TipoDeEstadoDaOrdemDeServico.RECEBIDA;
        entity.pecas = new HashSet<>();
        entity.servicos = new HashSet<>();
        entity.historicoDeEstados = new HashSet<>();

        var ordem = OrdemDeServicoFactory.criarNovo(1L, 2L);
        ordem.iniciarDiagnostico();
        ordem.adicionaPeca(1L, "Filtro", BigDecimal.ONE, BigDecimal.TEN);
        ordem.adicionaServico(2L, "Troca", BigDecimal.ONE, BigDecimal.ONE);

        method("atualizarEstado", OrdemDeServicoEntity.class, br.com.oficina.atendimento.core.entities.ordem_de_servico.OrdemDeServico.class)
                .invoke(null, entity, ordem);
        method("atualizarPecas", OrdemDeServicoEntity.class, br.com.oficina.atendimento.core.entities.ordem_de_servico.OrdemDeServico.class)
                .invoke(null, entity, ordem);
        method("atualizarServicos", OrdemDeServicoEntity.class, br.com.oficina.atendimento.core.entities.ordem_de_servico.OrdemDeServico.class)
                .invoke(null, entity, ordem);

        assertEquals(TipoDeEstadoDaOrdemDeServico.EM_DIAGNOSTICO, entity.estadoAtual);
        assertEquals(1, entity.historicoDeEstados.size());
        assertEquals(1, entity.pecas.size());
        assertEquals(1, entity.servicos.size());
    }

    @Test
    void prePersistDeveInicializarIdECriadoEm() throws Exception {
        var entity = new OrdemDeServicoEntity();
        Method prePersist = OrdemDeServicoEntity.class.getDeclaredMethod("prePersist");
        prePersist.setAccessible(true);
        prePersist.invoke(entity);

        assertNotNull(entity.id);
        assertNotNull(entity.criadoEm);
    }

    @Test
    void dtoFromEntityDeveConsiderarValorTotalPersistido() {
        var pecaEntity = new OsItemPecaEntity();
        pecaEntity.pecaId = 1L;
        pecaEntity.pecaNome = "Peca";
        pecaEntity.quantidade = BigDecimal.ONE;
        pecaEntity.valorUnitario = BigDecimal.TEN;
        pecaEntity.valorTotal = new BigDecimal("10.00");

        var servicoEntity = new OsItemServicoEntity();
        servicoEntity.servicoId = 2L;
        servicoEntity.servicoNome = "Servico";
        servicoEntity.quantidade = BigDecimal.ONE;
        servicoEntity.valorUnitario = BigDecimal.ONE;
        servicoEntity.valorTotal = BigDecimal.ONE;

        assertEquals(new BigDecimal("10.00"), ItemPecaDTO.from(pecaEntity).valorTotal());
        assertEquals(BigDecimal.ONE, ItemServicoDTO.from(servicoEntity).valorTotal());
    }

    private static Method method(String name, Class<?>... types) throws Exception {
        var method = OrdemDeServicoDataSourceAdapter.class.getDeclaredMethod(name, types);
        method.setAccessible(true);
        return method;
    }
}
