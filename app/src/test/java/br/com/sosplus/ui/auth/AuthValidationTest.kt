package br.com.sosplus.ui.auth

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TesteValidacaoAutenticacao {
    @Test
    fun aceitaCadastroCompletoDosDoisPerfis() {
        assertTrue(validarCadastro(PerfilUsuario.Doador, "Ana", "ana@example.com", "senha123", "senha123").sucesso)
        assertTrue(validarCadastro(PerfilUsuario.Ong, "ONG", "ong@example.com", "senha123", "senha123", "11.222.333/0001-81").sucesso)
    }

    @Test
    fun rejeitaCamposAusentesESenhasDiferentes() {
        for (perfil in PerfilUsuario.entries) {
            val fields = listOf("Nome", "contato@example.com", "senha123", "senha123")
            for (i in fields.indices) {
                val invalid = fields.toMutableList().apply { this[i] = " " }
                assertFalse(validarCadastro(perfil, invalid[0], invalid[1], invalid[2], invalid[3], "11222333000181").sucesso)
            }
            assertFalse(validarCadastro(perfil, "Nome", "a@example.com", "senha123", "outrasenha", "11222333000181").sucesso)
        }
        assertFalse(validarCadastro(PerfilUsuario.Ong, "ONG", "a@example.com", "senha123", "senha123").sucesso)
    }

    @Test
    fun validaCnpjNumericoEAlfanumerico() {
        assertTrue(cnpjValido("11.222.333/0001-81"))
        assertTrue(cnpjValido("00.000.000/E08G-12"))
        assertFalse(cnpjValido("11.222.333/0001-80"))
        assertFalse(cnpjValido("00.000.000/0000-00"))
        assertFalse(cnpjValido("11222333000181!"))
    }

    @Test
    fun rejeitaEmailESenhaInvalidos() {
        assertFalse(validarCadastro(PerfilUsuario.Doador, "Ana", "a@b", "senha123", "senha123").sucesso)
        assertFalse(validarCadastro(PerfilUsuario.Doador, "Ana", "a@example.com", "1234", "1234").sucesso)
    }
}
