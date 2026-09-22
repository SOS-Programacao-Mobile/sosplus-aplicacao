package br.com.sosplus.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.sosplus.BuildConfig
import br.com.sosplus.data.NetworkLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun EnderecoOng(token: String) {
    var aberto by rememberSaveable { mutableStateOf(false) }
    var valores by rememberSaveable { mutableStateOf(List(8) { "" }) }
    var mensagem by remember { mutableStateOf("") }
    var salvando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val rotulos = listOf("CEP", "Logradouro", "Número", "Bairro", "Cidade", "UF", "Latitude", "Longitude")
    OutlinedButton(onClick = { aberto = !aberto }, modifier = Modifier.padding(top = 16.dp)) {
        Text("Cadastrar endereço no mapa")
    }
    if (aberto) {
        Text("Informe a sede da ONG. Este endereço e o pin ficarão visíveis aos doadores.")
        rotulos.forEachIndexed { i, rotulo ->
            OutlinedTextField(value = valores[i], onValueChange = { novo ->
                valores = valores.toMutableList().also { it[i] = novo }
            }, label = { Text(rotulo) }, enabled = !salvando, modifier = Modifier.fillMaxWidth())
        }
        Text("Use as coordenadas da sede, não a localização pessoal do responsável.")
        Button(enabled = !salvando, onClick = {
            val lat = valores[6].replace(',', '.').toDoubleOrNull()
            val lon = valores[7].replace(',', '.').toDoubleOrNull()
            if (lat == null || lon == null || !lat.isFinite() || !lon.isFinite() ||
                lat !in -90.0..90.0 || lon !in -180.0..180.0 || valores.take(6).any { it.isBlank() }) {
                mensagem = "Preencha o endereço e coordenadas válidas."
            } else {
                salvando = true
                scope.launch {
                    try {
                        val body = JSONObject()
                        listOf("cep", "logradouro", "numero", "bairro", "cidade", "estado").forEachIndexed { i, key ->
                            body.put(key, if (i == 5) valores[i].trim().uppercase() else valores[i].trim())
                        }
                        body.put("latitude", lat); body.put("longitude", lon)
                        withContext(Dispatchers.IO) {
                            val url = BuildConfig.API_BASE_URL.trimEnd('/') + "/api/v1/ongs/endereco"
                            val call = NetworkLogger.request("PUT", url, body)
                            val c = URL(url).openConnection() as HttpURLConnection
                            try {
                                c.requestMethod = "PUT"; c.connectTimeout = 10000; c.readTimeout = 10000
                                c.setRequestProperty("X-Request-Id", call.id)
                                c.setRequestProperty("Authorization", "Bearer " + token)
                                c.setRequestProperty("Content-Type", "application/json")
                                c.doOutput = true
                                c.outputStream.bufferedWriter().use { it.write(body.toString()) }
                                val code = c.responseCode
                                val text = if (code in 200..299) c.inputStream else c.errorStream
                                val responseBody = text?.bufferedReader()?.use { it.readText() }.orEmpty()
                                NetworkLogger.response(call, code, responseBody)
                                if (code != 200) {
                                    throw IllegalStateException(runCatching { JSONObject(responseBody).getString("error") }.getOrDefault("Não foi possível salvar."))
                                }
                            } catch (error: Exception) {
                                NetworkLogger.failure(call, error)
                                throw error
                            } finally { c.disconnect() }
                        }
                        mensagem = "Endereço publicado! Seu pin aparecerá no mapa dos doadores."
                    } catch (e: kotlinx.coroutines.CancellationException) { throw e }
                    catch (e: Exception) { mensagem = e.message ?: "Falha ao salvar endereço." }
                    finally { salvando = false }
                }
            }
        }) { Text(if (salvando) "Salvando…" else "Publicar endereço da ONG") }
        Text(mensagem)
    }
}
