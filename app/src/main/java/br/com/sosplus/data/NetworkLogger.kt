package br.com.sosplus.data

import android.os.SystemClock
import android.util.Log
import br.com.sosplus.BuildConfig
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class NetworkCall internal constructor(
    val id: String,
    val method: String,
    val url: String,
    val startedAtMs: Long,
)

object NetworkLogger {
    private const val TAG = "SOSPlus-HTTP"
    private const val MAX_BODY_CHARS = 2_000
    private val simpleField = Regex("^[A-Za-z0-9._:/@+?&=%-]+$")
    private val sensitiveKeys = setOf(
        "authorization", "token", "senha", "senhaatual", "novasenha", "confirmacaosenha",
        "password", "cpf", "cnpj", "email", "nome", "sobrenome", "telefone", "endereco",
        "datanascimento", "tiposanguineo", "genero", "conteudo", "latitude", "longitude",
        "cep", "logradouro", "numero", "bairro", "cidade", "estado", "localizacao",
    )

    fun request(method: String, url: String, body: JSONObject? = null, bodyBytes: Int? = null): NetworkCall {
        val call = NetworkCall(UUID.randomUUID().toString(), method, url, SystemClock.elapsedRealtime())
        val fields = linkedMapOf<String, Any?>(
            "request_id" to call.id,
            "metodo" to method,
            "url" to url,
        )
        when {
            body != null -> fields["body"] = sanitize(body).toString()
            bodyBytes != null -> fields["body_bytes"] = bodyBytes
        }
        write(Log.DEBUG, "DEBUG", "Requisição HTTP enviada", fields)
        return call
    }

    fun response(call: NetworkCall, status: Int, body: String? = null, bodyBytes: Int? = null) {
        val duration = SystemClock.elapsedRealtime() - call.startedAtMs
        val fields = linkedMapOf<String, Any?>(
            "request_id" to call.id,
            "metodo" to call.method,
            "url" to call.url,
            "status" to status,
            "duracao_ms" to duration,
        )
        when {
            body != null -> fields["body"] = sanitize(body)
            bodyBytes != null -> fields["body_bytes"] = bodyBytes
        }
        when {
            status >= 500 -> write(Log.ERROR, "ERROR", "Requisição HTTP concluída com erro", fields)
            status >= 400 -> write(Log.WARN, "WARN", "Requisição HTTP rejeitada", fields)
            else -> write(Log.INFO, "INFO", "Requisição HTTP concluída", fields)
        }
    }

    fun failure(call: NetworkCall, error: Throwable) {
        val duration = SystemClock.elapsedRealtime() - call.startedAtMs
        write(
            Log.ERROR,
            "ERROR",
            "Falha na comunicação HTTP",
            linkedMapOf(
                "request_id" to call.id,
                "metodo" to call.method,
                "url" to call.url,
                "duracao_ms" to duration,
                "erro_tipo" to error.javaClass.simpleName,
                "erro" to error.message,
            ),
        )
    }

    private fun write(priority: Int, level: String, message: String, fields: Map<String, Any?>) {
        if (!BuildConfig.DEBUG) return
        val suffix = fields.entries
            .filter { it.value != null }
            .joinToString(" ") { (key, value) -> "$key=${fieldValue(value)}" }
        Log.println(priority, TAG, "${timestamp()} $level $message${if (suffix.isEmpty()) "" else " $suffix"}")
    }

    private fun timestamp(): String = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date())

    private fun fieldValue(value: Any?): String = when (value) {
        null -> "null"
        is Number, is Boolean -> value.toString()
        else -> value.toString().let { if (simpleField.matches(it)) it else JSONObject.quote(it) }
    }

    private fun sanitize(raw: String): String {
        val sanitized = runCatching {
            when {
                raw.trimStart().startsWith("{") -> sanitize(JSONObject(raw)).toString()
                raw.trimStart().startsWith("[") -> sanitize(JSONArray(raw)).toString()
                else -> raw
            }
        }.getOrDefault(raw)
        return sanitized.take(MAX_BODY_CHARS) + if (sanitized.length > MAX_BODY_CHARS) "…" else ""
    }

    private fun sanitize(value: Any?): Any = when (value) {
        null, JSONObject.NULL -> JSONObject.NULL
        is JSONObject -> JSONObject().also { output ->
            value.keys().forEach { key ->
                output.put(key, if (key.lowercase() in sensitiveKeys) "<redacted>" else sanitize(value.opt(key)))
            }
        }
        is JSONArray -> JSONArray().also { output ->
            for (index in 0 until value.length()) output.put(sanitize(value.opt(index)))
        }
        is String -> value.take(500) + if (value.length > 500) "…" else ""
        else -> value
    }
}
