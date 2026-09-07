package br.com.sosplus

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assume.assumeTrue
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AuthFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun cadastroLoginESaidaDoador() = fluxo(false)
    @Test fun cadastroLoginESaidaOng() = fluxo(true)

    private fun campo(label: String, value: String) {
        compose.onNodeWithText(label).performScrollTo().performTextReplacement(value)
        closeSoftKeyboard()
    }

    private fun aguardar(text: String) {
        compose.waitUntil(timeoutMillis = 20_000) {
            compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText(text).performScrollTo().assertIsDisplayed()
    }

    private fun fluxo(ong: Boolean) {
        // Somente com API apontada para um banco isolado de testes.
        assumeTrue(InstrumentationRegistry.getArguments().getString("authE2E") == "true")
        val health = org.json.JSONObject(java.net.URL("${BuildConfig.API_BASE_URL}/api/v1/health/database").readText())
        assertTrue("Teste exige banco isolado *_test", health.getString("database").endsWith("_test"))
        val suffix = System.currentTimeMillis().toString()
        val nome = if (ong) "ONG Teste $suffix" else "Doador Teste $suffix"
        val email = "android-${if (ong) "ong" else "doador"}-$suffix@example.test"
        compose.onNodeWithText("Criar conta de doador").performScrollTo().performClick()
        if (ong) compose.onNodeWithText("ONG").performClick()
        val cadastrar = if (ong) "Cadastrar ONG" else "Criar minha conta"
        compose.onNodeWithText(cadastrar).performScrollTo().performClick()
        aguardar("Preencha todos os campos obrigatórios.")
        campo(if (ong) "Nome da ONG" else "Nome completo", nome)
        campo(if (ong) "E-mail institucional" else "E-mail", email)
        if (ong) campo("CNPJ obrigatório", cnpjDeTeste(suffix.takeLast(12)))
        campo("Senha", "Teste-seguro-123")
        campo("Confirmar senha", "diferente")
        compose.onNodeWithText(cadastrar).performScrollTo().performClick()
        aguardar("As senhas não são iguais.")
        campo("Confirmar senha", "Teste-seguro-123")
        compose.onNodeWithText(cadastrar).performScrollTo().performClick()
        aguardar("Conta salva! Entre com seu e-mail e senha.")
        compose.onNodeWithText("Já tenho uma conta — entrar").performScrollTo().performClick()
        campo("E-mail", email)
        campo("Senha", "senha-errada")
        val entrar = if (ong) "Entrar como ONG" else "Entrar como doador"
        compose.onNodeWithText(entrar).performScrollTo().performClick()
        aguardar("E-mail ou senha incorretos para o perfil selecionado.")
        campo("Senha", "Teste-seguro-123")
        compose.onNodeWithText(entrar).performScrollTo().performClick()
        aguardar("Olá, $nome")
        if (!ong) compose.onNodeWithText("Perfil").performClick()
        compose.onNodeWithText(email, substring = true).assertIsDisplayed()
        compose.onNodeWithText(if (ong) "Sair" else "Sair da conta").performScrollTo().performClick()
        aguardar("Bem-vindo de volta")
    }

    private fun cnpjDeTeste(base: String): String {
        fun digito(value: String): Int {
            val resto = value.reversed().mapIndexed { i, char -> char.digitToInt() * (i % 8 + 2) }.sum() % 11
            return if (resto < 2) 0 else 11 - resto
        }
        val primeiro = base + digito(base)
        return primeiro + digito(primeiro)
    }
}
