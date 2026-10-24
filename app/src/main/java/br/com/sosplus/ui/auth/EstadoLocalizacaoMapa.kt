package br.com.sosplus.ui.auth

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity

/** Situação da localização do usuário exibida na tela de mapa. */
enum class EstadoLocalizacaoMapa {
    /** Permissão ainda não concedida; o app pode pedir novamente. */
    PermissaoNecessaria,

    /** O usuário negou e marcou "não perguntar novamente"; só pelas configurações. */
    PermissaoNegadaDefinitivamente,

    /** Permissão concedida, aguardando a primeira leitura. */
    Buscando,

    /** Permissão concedida, mas o serviço de localização está desligado ou não respondeu. */
    Indisponivel,

    /** Posição atual obtida. */
    Ativa,
}

fun estadoLocalizacaoMapa(
    autorizado: Boolean,
    negadaDefinitivamente: Boolean,
    temPosicaoAtual: Boolean,
    servicoIndisponivel: Boolean,
): EstadoLocalizacaoMapa = when {
    !autorizado && negadaDefinitivamente -> EstadoLocalizacaoMapa.PermissaoNegadaDefinitivamente
    !autorizado -> EstadoLocalizacaoMapa.PermissaoNecessaria
    temPosicaoAtual -> EstadoLocalizacaoMapa.Ativa
    servicoIndisponivel -> EstadoLocalizacaoMapa.Indisponivel
    else -> EstadoLocalizacaoMapa.Buscando
}

fun textoStatusLocalizacao(estado: EstadoLocalizacaoMapa, temLocalizacaoSalva: Boolean): String = when (estado) {
    EstadoLocalizacaoMapa.Ativa -> "Localização ativa"
    EstadoLocalizacaoMapa.Buscando -> if (temLocalizacaoSalva) "Localização salva" else "Buscando localização…"
    EstadoLocalizacaoMapa.Indisponivel -> "Localização indisponível"
    EstadoLocalizacaoMapa.PermissaoNecessaria,
    EstadoLocalizacaoMapa.PermissaoNegadaDefinitivamente,
    -> if (temLocalizacaoSalva) "Localização salva" else "Localização desativada"
}

/**
 * Mensagem curta do aviso sobre o mapa, ou `null` quando não há o que avisar. O mapa
 * continua exibindo [referencia] — a última localização salva ou a região padrão.
 */
fun avisoLocalizacao(estado: EstadoLocalizacaoMapa, temLocalizacaoSalva: Boolean): AvisoLocalizacao? {
    val referencia = if (temLocalizacaoSalva) "último local salvo" else "região padrão"
    return when (estado) {
        EstadoLocalizacaoMapa.PermissaoNecessaria -> AvisoLocalizacao(
            mensagem = "Sem permissão de localização. Mostrando $referencia.",
            acao = "Permitir",
        )
        EstadoLocalizacaoMapa.PermissaoNegadaDefinitivamente -> AvisoLocalizacao(
            mensagem = "Permissão negada. Mostrando $referencia.",
            acao = "Abrir configurações",
        )
        EstadoLocalizacaoMapa.Indisponivel -> AvisoLocalizacao(
            mensagem = "Localização indisponível. Mostrando $referencia.",
            acao = "Ativar localização",
        )
        EstadoLocalizacaoMapa.Buscando,
        EstadoLocalizacaoMapa.Ativa,
        -> null
    }
}

data class AvisoLocalizacao(val mensagem: String, val acao: String)

fun Context.encontrarActivity(): ComponentActivity? {
    var atual: Context? = this
    while (atual is ContextWrapper) {
        if (atual is ComponentActivity) return atual
        atual = atual.baseContext
    }
    return null
}

fun abrirConfiguracoesDoApp(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
}

fun abrirConfiguracoesDeLocalizacao(context: Context) {
    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
        .onFailure { abrirConfiguracoesDoApp(context) }
}
