package br.com.sosplus.data

import br.com.sosplus.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

data class UsuarioAutenticado(
    val id: String,
    val nome: String,
    val email: String,
    val tipo: String,
    val cnpj: String?,
)

data class Sessao(val token: String, val usuario: UsuarioAutenticado)

class AuthApiException(message: String) : IOException(message)

class AuthRepository {
    suspend fun cadastrar(nome: String, email: String, senha: String, confirmacao: String, tipo: String, cnpj: String) {
        request("register", "POST", JSONObject().apply {
            put("nome", nome.trim())
            put("email", email.trim())
            put("senha", senha)
            put("confirmacaoSenha", confirmacao)
            put("tipo", tipo)
            if (tipo == "ONG") put("cnpj", cnpj.trim())
        })
    }

    suspend fun entrar(email: String, senha: String, tipo: String): Sessao {
        val result = request("login", "POST", JSONObject().apply {
            put("email", email.trim())
            put("senha", senha)
            put("tipo", tipo)
        })
        val token = result.getString("token")
        // A área autenticada usa o perfil lido pela rota protegida do servidor.
        val current = request("me", "GET", token = token).getJSONObject("usuario")
        return Sessao(token, UsuarioAutenticado(
            id = current.getString("id"),
            nome = current.getString("nome"),
            email = current.getString("email"),
            tipo = current.getString("tipo"),
            cnpj = if (current.isNull("cnpj")) null else current.getString("cnpj"),
        ))
    }

    suspend fun sair(token: String) {
        request("logout", "POST", token = token)
    }

    private suspend fun request(path: String, method: String, body: JSONObject? = null, token: String? = null): JSONObject =
        withContext(Dispatchers.IO) {
            val connection = URL("${BuildConfig.API_BASE_URL.trimEnd('/')}/api/v1/auth/$path")
                .openConnection() as HttpURLConnection
            try {
                connection.requestMethod = method
                connection.connectTimeout = 10_000
                connection.readTimeout = 15_000
                connection.instanceFollowRedirects = false
                connection.setRequestProperty("Accept", "application/json")
                if (token != null) connection.setRequestProperty("Authorization", "Bearer $token")
                if (body != null) {
                    connection.doOutput = true
                    connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    connection.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(body.toString()) }
                }
                val status = connection.responseCode
                val stream = if (status in 200..299) connection.inputStream else connection.errorStream
                val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                val json = try { if (text.isBlank()) JSONObject() else JSONObject(text) }
                    catch (_: org.json.JSONException) { throw AuthApiException("Resposta inesperada do servidor.") }
                if (status !in 200..299) throw AuthApiException(json.optString("error", "Não foi possível concluir a solicitação."))
                json
            } finally {
                connection.disconnect()
            }
        }
}
