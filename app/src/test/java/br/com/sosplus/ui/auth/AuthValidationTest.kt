package br.com.sosplus.ui.auth

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TesteValidacaoAutenticacao {
    @Test
    fun aceitaCredenciaisDeDemonstracaoParaCadaPerfil() {
        assertTrue(
            credenciaisSaoValidas(
                perfil = PerfilUsuario.Doador,
                usuario = "admin",
                senha = "1234",
            ),
        )
        assertTrue(
            credenciaisSaoValidas(
                perfil = PerfilUsuario.Ong,
                usuario = "ong",
                senha = "1234",
            ),
        )
    }

    @Test
    fun rejeitaCredenciaisIncorretas() {
        assertFalse(
            credenciaisSaoValidas(
                perfil = PerfilUsuario.Doador,
                usuario = "admin",
                senha = "0000",
            ),
        )
        assertFalse(
            credenciaisSaoValidas(
                perfil = PerfilUsuario.Ong,
                usuario = "admin",
                senha = "1234",
            ),
        )
    }
}
