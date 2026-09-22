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
    val localizacao: LocalizacaoUsuario? = null,
)

data class LocalizacaoUsuario(
    val cidade: String,
    val estado: String,
    val latitude: Double,
    val longitude: Double,
) {
    val descricao: String get() = "$cidade, $estado"
}

data class Sessao(val token: String, val usuario: UsuarioAutenticado)

data class DadosPessoaisUsuario(
    val nome: String,
    val sobrenome: String? = null,
    val cpf: String? = null,
    val dataNascimento: String? = null,
    val endereco: String? = null,
    val tipoSanguineo: String? = null,
    val genero: String? = null,
    val telefone: String? = null,
)

data class AtualizacaoDadosPessoais(
    val dados: DadosPessoaisUsuario,
    val usuario: UsuarioAutenticado,
)

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
        return Sessao(token, usuarioDeJson(current))
    }

    suspend fun atualizarLocalizacao(token: String, localizacao: LocalizacaoUsuario): UsuarioAutenticado {
        val response = request("localizacao", "PUT", JSONObject().apply {
            put("cidade", localizacao.cidade)
            put("estado", localizacao.estado)
            put("latitude", localizacao.latitude)
            put("longitude", localizacao.longitude)
        }, token)
        return usuarioDeJson(response.getJSONObject("usuario"))
    }

    suspend fun carregarFotoPerfil(token: String): ByteArray? = withContext(Dispatchers.IO) {
        val url = "${BuildConfig.API_BASE_URL.trimEnd('/')}/api/v1/auth/foto-perfil"
        val call = NetworkLogger.request("GET", url)
        val connection = URL(url)
            .openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.setRequestProperty("X-Request-Id", call.id)
            connection.connectTimeout = 10_000
            connection.readTimeout = 15_000
            connection.instanceFollowRedirects = false
            connection.setRequestProperty("Authorization", "Bearer $token")
            val status = connection.responseCode
            when (status) {
                HttpURLConnection.HTTP_NO_CONTENT -> {
                    NetworkLogger.response(call, status, bodyBytes = 0)
                    null
                }
                HttpURLConnection.HTTP_OK -> connection.inputStream.use { it.readBytes() }.also {
                    NetworkLogger.response(call, status, bodyBytes = it.size)
                }
                else -> {
                    NetworkLogger.response(call, status)
                    throw erroDaResposta(connection, status)
                }
            }
        } catch (error: Exception) {
            NetworkLogger.failure(call, error)
            throw error
        } finally {
            connection.disconnect()
        }
    }

    suspend fun atualizarFotoPerfil(token: String, conteudo: ByteArray, mimeType: String) = withContext(Dispatchers.IO) {
        require(conteudo.isNotEmpty() && conteudo.size <= 1024 * 1024) { "A foto deve ter até 1 MB." }
        val url = "${BuildConfig.API_BASE_URL.trimEnd('/')}/api/v1/auth/foto-perfil"
        val call = NetworkLogger.request("PUT", url, bodyBytes = conteudo.size)
        val connection = URL(url)
            .openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "PUT"
            connection.setRequestProperty("X-Request-Id", call.id)
            connection.connectTimeout = 10_000
            connection.readTimeout = 15_000
            connection.instanceFollowRedirects = false
            connection.doOutput = true
            connection.setFixedLengthStreamingMode(conteudo.size)
            connection.setRequestProperty("Authorization", "Bearer $token")
            connection.setRequestProperty("Content-Type", mimeType)
            connection.outputStream.use { it.write(conteudo) }
            val status = connection.responseCode
            NetworkLogger.response(call, status)
            if (status !in 200..299) throw erroDaResposta(connection, status)
        } catch (error: Exception) {
            NetworkLogger.failure(call, error)
            throw error
        } finally {
            connection.disconnect()
        }
    }

    suspend fun atualizarSenha(token: String, senhaAtual: String, novaSenha: String, confirmacao: String) {
        request("senha", "PUT", JSONObject().apply {
            put("senhaAtual", senhaAtual)
            put("novaSenha", novaSenha)
            put("confirmacaoSenha", confirmacao)
        }, token)
    }

    suspend fun carregarDadosPessoais(token: String): DadosPessoaisUsuario {
        val response = request("dados-pessoais", "GET", token = token)
        return dadosPessoaisDeJson(response.getJSONObject("dadosPessoais"))
    }

    suspend fun atualizarDadosPessoais(token: String, dados: DadosPessoaisUsuario): AtualizacaoDadosPessoais {
        val response = request("dados-pessoais", "PUT", JSONObject().apply {
            put("nome", dados.nome.trim())
            putNullable("sobrenome", dados.sobrenome)
            putNullable("cpf", dados.cpf)
            putNullable("dataNascimento", dados.dataNascimento)
            putNullable("endereco", dados.endereco)
            putNullable("tipoSanguineo", dados.tipoSanguineo)
            putNullable("genero", dados.genero)
            putNullable("telefone", dados.telefone)
        }, token)
        return AtualizacaoDadosPessoais(
            dados = dadosPessoaisDeJson(response.getJSONObject("dadosPessoais")),
            usuario = usuarioDeJson(response.getJSONObject("usuario")),
        )
    }

    suspend fun sair(token: String) {
        request("logout", "POST", token = token)
    }

    private fun usuarioDeJson(json: JSONObject): UsuarioAutenticado {
        val localizacaoJson = json.optJSONObject("localizacao")
        val localizacao = localizacaoJson?.let {
            val cidade = it.optString("cidade").trim()
            val estado = it.optString("estado").trim()
            val latitude = it.optDouble("latitude", Double.NaN)
            val longitude = it.optDouble("longitude", Double.NaN)
            if (cidade.isNotEmpty() && estado.isNotEmpty() && latitude.isFinite() && longitude.isFinite()) {
                LocalizacaoUsuario(cidade, estado, latitude, longitude)
            } else {
                null
            }
        }
        return UsuarioAutenticado(
            id = json.getString("id"),
            nome = json.getString("nome"),
            email = json.getString("email"),
            tipo = json.getString("tipo"),
            cnpj = if (json.isNull("cnpj")) null else json.getString("cnpj"),
            localizacao = localizacao,
        )
    }

    private fun dadosPessoaisDeJson(json: JSONObject) = DadosPessoaisUsuario(
        nome = json.getString("nome"),
        sobrenome = json.stringOuNulo("sobrenome"),
        cpf = json.stringOuNulo("cpf"),
        dataNascimento = json.stringOuNulo("dataNascimento"),
        endereco = json.stringOuNulo("endereco"),
        tipoSanguineo = json.stringOuNulo("tipoSanguineo"),
        genero = json.stringOuNulo("genero"),
        telefone = json.stringOuNulo("telefone"),
    )

    private suspend fun request(path: String, method: String, body: JSONObject? = null, token: String? = null): JSONObject =
        withContext(Dispatchers.IO) {
            val url = "${BuildConfig.API_BASE_URL.trimEnd('/')}/api/v1/auth/$path"
            val call = NetworkLogger.request(method, url, body)
            val connection = URL(url)
                .openConnection() as HttpURLConnection
            try {
                connection.requestMethod = method
                connection.setRequestProperty("X-Request-Id", call.id)
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
                NetworkLogger.response(call, status, text)
                val json = try { if (text.isBlank()) JSONObject() else JSONObject(text) }
                    catch (_: org.json.JSONException) { throw AuthApiException("Resposta inesperada do servidor.") }
                if (status !in 200..299) throw AuthApiException(json.optString("error", "Não foi possível concluir a solicitação."))
                json
            } catch (error: Exception) {
                NetworkLogger.failure(call, error)
                throw error
            } finally {
                connection.disconnect()
            }
        }

    private fun erroDaResposta(connection: HttpURLConnection, status: Int): AuthApiException {
        val text = connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
        val mensagem = runCatching { JSONObject(text).optString("error") }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: "Não foi possível concluir a solicitação ($status)."
        return AuthApiException(mensagem)
    }
}

private fun JSONObject.putNullable(key: String, value: String?) {
    put(key, value?.trim()?.takeIf(String::isNotEmpty) ?: JSONObject.NULL)
}

private fun JSONObject.stringOuNulo(key: String): String? =
    if (isNull(key)) null else optString(key).trim().takeIf(String::isNotEmpty)
