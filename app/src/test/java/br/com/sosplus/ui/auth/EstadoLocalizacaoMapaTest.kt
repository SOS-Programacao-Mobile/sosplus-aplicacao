package br.com.sosplus.ui.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EstadoLocalizacaoMapaTest {

    private fun estado(
        autorizado: Boolean = true,
        negadaDefinitivamente: Boolean = false,
        temPosicaoAtual: Boolean = false,
        servicoIndisponivel: Boolean = false,
    ) = estadoLocalizacaoMapa(autorizado, negadaDefinitivamente, temPosicaoAtual, servicoIndisponivel)

    @Test
    fun semPermissaoPedePermissao() {
        assertEquals(EstadoLocalizacaoMapa.PermissaoNecessaria, estado(autorizado = false))
    }

    @Test
    fun recusaDefinitivaLevaAsConfiguracoes() {
        val atual = estado(autorizado = false, negadaDefinitivamente = true)
        assertEquals(EstadoLocalizacaoMapa.PermissaoNegadaDefinitivamente, atual)
        assertEquals("Abrir configurações", avisoLocalizacao(atual, temLocalizacaoSalva = false)?.acao)
    }

    @Test
    fun permissaoConcedidaSemLeituraEstaBuscando() {
        assertEquals(EstadoLocalizacaoMapa.Buscando, estado())
        assertNull(avisoLocalizacao(EstadoLocalizacaoMapa.Buscando, temLocalizacaoSalva = false))
    }

    @Test
    fun servicoDesligadoFicaIndisponivel() {
        val atual = estado(servicoIndisponivel = true)
        assertEquals(EstadoLocalizacaoMapa.Indisponivel, atual)
        assertEquals("Localização indisponível", textoStatusLocalizacao(atual, temLocalizacaoSalva = true))
        assertNotNull(avisoLocalizacao(atual, temLocalizacaoSalva = true))
    }

    @Test
    fun posicaoAtualPrevaleceSobreIndisponibilidade() {
        val atual = estado(temPosicaoAtual = true, servicoIndisponivel = true)
        assertEquals(EstadoLocalizacaoMapa.Ativa, atual)
        assertEquals("Localização ativa", textoStatusLocalizacao(atual, temLocalizacaoSalva = false))
        assertNull(avisoLocalizacao(atual, temLocalizacaoSalva = false))
    }

    @Test
    fun avisoIndicaLocalUsadoComoAlternativa() {
        val comSalva = avisoLocalizacao(EstadoLocalizacaoMapa.PermissaoNecessaria, temLocalizacaoSalva = true)!!
        val semSalva = avisoLocalizacao(EstadoLocalizacaoMapa.PermissaoNecessaria, temLocalizacaoSalva = false)!!
        assertTrue(comSalva.mensagem.contains("último local salvo"))
        assertTrue(semSalva.mensagem.contains("região padrão"))
        assertEquals("Localização desativada", textoStatusLocalizacao(EstadoLocalizacaoMapa.PermissaoNecessaria, false))
        assertEquals("Localização salva", textoStatusLocalizacao(EstadoLocalizacaoMapa.PermissaoNecessaria, true))
    }
}
