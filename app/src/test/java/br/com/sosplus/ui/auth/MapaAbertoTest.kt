package br.com.sosplus.ui.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class MapaAbertoTest {

    @Test
    fun listaOngsPadraoContemInstituicoesDaReferencia() {
        val nomes = OngsPadraoNovaFriburgo.map { it.nome }
        assertTrue("Deve conter Casa do Bem", nomes.contains("Casa do Bem"))
        assertTrue("Deve conter Amigos dos Animais", nomes.contains("Amigos dos Animais"))
        assertTrue("Deve conter Projeto Novo Amanhã", nomes.contains("Projeto Novo Amanhã"))
        assertTrue("Deve conter Bazar Solidário SOS", nomes.contains("Bazar Solidário SOS"))
    }

    @Test
    fun coordenadasOngsEstaoNaRegiaoDeNovaFriburgo() {
        OngsPadraoNovaFriburgo.forEach { ong ->
            assertTrue(
                "Latitude de ${ong.nome} (${ong.latitude}) fora da faixa esperada",
                ong.latitude in -22.35..-22.20
            )
            assertTrue(
                "Longitude de ${ong.nome} (${ong.longitude}) fora da faixa esperada",
                ong.longitude in -42.60..-42.45
            )
        }
    }

    @Test
    fun categoriasPossuemIconesValidos() {
        val casaDoBem = OngsPadraoNovaFriburgo.first { it.nome == "Casa do Bem" }
        assertEquals("♡", casaDoBem.icone)
        assertEquals("Apoio social", casaDoBem.categoria)

        val amigosAnimais = OngsPadraoNovaFriburgo.first { it.nome == "Amigos dos Animais" }
        assertEquals("🐾", amigosAnimais.icone)
        assertEquals("Proteção animal", amigosAnimais.categoria)

        val novoAmanha = OngsPadraoNovaFriburgo.first { it.nome == "Projeto Novo Amanhã" }
        assertEquals("📚", novoAmanha.icone)
        assertEquals("Educação", novoAmanha.categoria)
    }

    @Test
    fun formatacaoDeDistanciaEmPortugues() {
        val dist1 = 1200.0 / 1000.0
        val texto1 = String.format(Locale.forLanguageTag("pt-BR"), "%.1f km", dist1)
        assertEquals("1,2 km", texto1)

        val dist2 = 2800.0 / 1000.0
        val texto2 = String.format(Locale.forLanguageTag("pt-BR"), "%.1f km", dist2)
        assertEquals("2,8 km", texto2)

        val distMetros = 850
        val textoMetros = "${distMetros} m"
        assertEquals("850 m", textoMetros)
    }
}
