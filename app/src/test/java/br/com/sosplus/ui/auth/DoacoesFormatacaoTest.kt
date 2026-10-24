package br.com.sosplus.ui.auth

import br.com.sosplus.data.DoacaoUsuario
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class DoacoesFormatacaoTest {
    @Test
    fun formataValorEmReais() {
        assertEquals("R$ 25,00", formatarValorDoacao(2500))
        assertEquals("R$ 0,05", formatarValorDoacao(5))
        assertEquals("R$ 1.234,56", formatarValorDoacao(123456))
        assertEquals("R$ 1.000.000,00", formatarValorDoacao(100000000))
    }

    @Test
    fun formataDataNoPadraoBrasileiro() {
        assertEquals("20/08/2026", formatarDataDoacao("2026-08-20"))
        assertEquals("ontem", formatarDataDoacao("ontem"))
    }

    private fun doacao(data: String, status: String = "CONFIRMADA", centavos: Long = 1000) =
        DoacaoUsuario(data, "ONG", "Campanha", centavos, data, status, anonima = false)

    private val hoje = Calendar.getInstance().apply { clear(); set(2026, Calendar.OCTOBER, 11) }

    private val doacoes = listOf(
        doacao("2026-10-11"),
        doacao("2026-09-11"),
        doacao("2026-09-10"),
        doacao("2026-07-11"),
        doacao("2026-07-10"),
        doacao("2026-01-01"),
        doacao("2025-12-31"),
    )

    private fun datas(periodo: PeriodoDoacao) = filtrarDoacoesPorPeriodo(doacoes, periodo, hoje).map { it.data }

    @Test
    fun filtraPorPeriodoIncluindoOsLimites() {
        assertEquals(doacoes.map { it.data }, datas(PeriodoDoacao.Todas))
        assertEquals(listOf("2026-10-11", "2026-09-11"), datas(PeriodoDoacao.UltimosTrintaDias))
        assertEquals(listOf("2026-10-11", "2026-09-11", "2026-09-10", "2026-07-11"), datas(PeriodoDoacao.UltimosTresMeses))
        assertEquals(doacoes.map { it.data }.dropLast(1), datas(PeriodoDoacao.EsteAno))
    }

    @Test
    fun totalSomaSomenteConfirmadas() {
        val lista = listOf(
            doacao("2026-08-20", "CONFIRMADA", 2500),
            doacao("2026-08-18", "CONFIRMADA", 5000),
            doacao("2026-09-02", "PENDENTE", 10000),
            doacao("2026-06-11", "CANCELADA", 1500),
        )
        assertEquals(7500L, totalConfirmadoCentavos(lista))
    }
}
