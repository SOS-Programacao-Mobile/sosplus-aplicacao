package br.com.sosplus.ui.auth

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Looper
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import br.com.sosplus.BuildConfig
import br.com.sosplus.data.LocalizacaoUsuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.net.HttpURLConnection
import java.net.URL
import br.com.sosplus.data.NetworkLogger
import java.util.Locale

data class OngItem(
    val id: String,
    val nome: String,
    val categoria: String,
    val icone: String,
    val latitude: Double,
    val longitude: Double,
    val descricao: String = "",
    val endereco: String = "Nova Friburgo, RJ",
)

val OngsPadraoNovaFriburgo = listOf(
    OngItem(
        id = "1",
        nome = "Casa do Bem",
        categoria = "Apoio social",
        icone = "♡",
        latitude = -22.2750,
        longitude = -42.5360,
        descricao = "Arrecadação de alimentos e amparo a famílias em vulnerabilidade social.",
    ),
    OngItem(
        id = "2",
        nome = "Amigos dos Animais",
        categoria = "Proteção animal",
        icone = "🐾",
        latitude = -22.2770,
        longitude = -42.5220,
        descricao = "Resgate, reabilitação e campanhas de adoção responsável na região serrana.",
    ),
    OngItem(
        id = "3",
        nome = "Projeto Novo Amanhã",
        categoria = "Educação",
        icone = "📚",
        latitude = -22.2880,
        longitude = -42.5180,
        descricao = "Reforço escolar, oficinas culturais e apoio ao desenvolvimento infantil.",
    ),
    OngItem(
        id = "4",
        nome = "Bazar Solidário SOS",
        categoria = "Alimentação & Agasalho",
        icone = "🛍",
        latitude = -22.2890,
        longitude = -42.5410,
        descricao = "Distribuição de cestas básicas e agasalhos para comunidades locais.",
    ),
)

// Ponto central padrão de Nova Friburgo, RJ
val PontoCentralPadrao = GeoPoint(-22.2819, -42.5311)

private const val INTERVALO_GPS_MS = 10_000L
private const val DISTANCIA_MINIMA_GPS_METROS = 12f
private const val INTERVALO_MINIMO_REDESENHO_GPS_MS = 20_000L
private const val INTERVALO_ATUALIZACAO_ONGS_MS = 120_000L
private const val MAX_MARCADORES_ONG_NO_MAPA = 80

private data class ChaveIconeOng(
    val icone: String,
    val nomeEtiqueta: String?,
    val selecionado: Boolean,
)

/**
 * Evita recompor o mapa para leituras duplicadas do GPS — algo comum quando os
 * provedores de rede e GPS entregam a mesma coordenada em sequência.
 */
private fun deveAtualizarPosicao(atual: Location?, nova: Location): Boolean {
    if (atual == null) return true
    if (atual.distanceTo(nova) >= DISTANCIA_MINIMA_GPS_METROS) return true

    return nova.time - atual.time >= INTERVALO_MINIMO_REDESENHO_GPS_MS
}

/**
 * Cria o marcador do usuário com anel azul neon brilhante e cápsula "Sua localização".
 */
fun criarMarcadorUsuario(context: Context): Drawable {
    val density = context.resources.displayMetrics.density
    val width = (130 * density).toInt()
    val height = (74 * density).toInt()
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)

    val centerX = width / 2f
    val centerY = 24f * density

    // Halo externo difuso azul
    val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#3B82F6")
        alpha = 65
        style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX, centerY, 21f * density, haloPaint)

    // Anel azul neon brilhante
    val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#3B82F6")
        style = Paint.Style.STROKE
        strokeWidth = 3.5f * density
    }
    canvas.drawCircle(centerX, centerY, 13f * density, ringPaint)

    // Ponto central branco
    val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX, centerY, 6f * density, dotPaint)

    // Cápsula "Sua localização"
    val text = "Sua localização"
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = 10f * density
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    val textWidth = textPaint.measureText(text)
    val pillPadX = 8f * density
    val pillTop = centerY + 14f * density
    val pillBottom = pillTop + 20f * density
    val pillLeft = centerX - (textWidth / 2f) - pillPadX
    val pillRight = centerX + (textWidth / 2f) + pillPadX
    val pillRadius = 10f * density
    val pillRect = RectF(pillLeft, pillTop, pillRight, pillBottom)

    val pillBackground = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#26295C")
        style = Paint.Style.FILL
    }
    val pillBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#4B559C")
        style = Paint.Style.STROKE
        strokeWidth = 1.2f * density
    }

    canvas.drawRoundRect(pillRect, pillRadius, pillRadius, pillBackground)
    canvas.drawRoundRect(pillRect, pillRadius, pillRadius, pillBorder)

    val fontMetrics = textPaint.fontMetrics
    val textY = pillRect.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2f
    canvas.drawText(text, centerX, textY, textPaint)

    return BitmapDrawable(context.resources, bitmap)
}

/**
 * Cria o marcador de ONG no mapa: medalhão circular roxo com símbolo branco e etiqueta com nome.
 */
fun criarMarcadorOng(
    context: Context,
    nome: String,
    icone: String,
    selecionado: Boolean = false,
): Drawable {
    val density = context.resources.displayMetrics.density
    val circleRadius = (if (selecionado) 22f else 18f) * density
    val circleDiameter = circleRadius * 2f

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = 10.5f * density
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    val textWidth = textPaint.measureText(nome)
    val pillPadX = 8f * density
    val pillWidth = textWidth + (pillPadX * 2f)

    val width = maxOf(circleDiameter + 24f * density, pillWidth + 12f * density).toInt()
    val height = (circleDiameter + 36f * density).toInt()
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)

    val centerX = width / 2f
    val circleCenterY = circleRadius + 4f * density

    // Brilho de seleção
    if (selecionado) {
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#9C64FF")
            alpha = 100
            style = Paint.Style.FILL
        }
        canvas.drawCircle(centerX, circleCenterY, circleRadius + 5f * density, glowPaint)
    }

    // Fundo do círculo roxo
    val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor(if (selecionado) "#9C64FF" else "#803DF1")
        style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX, circleCenterY, circleRadius, circlePaint)

    // Borda do círculo
    val circleBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2f * density
        alpha = 230
    }
    canvas.drawCircle(centerX, circleCenterY, circleRadius, circleBorder)

    // Símbolo central
    val symbolPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = (if (selecionado) 17f else 14.5f) * density
        textAlign = Paint.Align.CENTER
    }
    val symbolMetrics = symbolPaint.fontMetrics
    val symbolY = circleCenterY - (symbolMetrics.ascent + symbolMetrics.descent) / 2f
    canvas.drawText(icone, centerX, symbolY, symbolPaint)

    // Cápsula de texto com o nome da ONG
    val pillTop = circleCenterY + circleRadius + 3.5f * density
    val pillHeight = 19f * density
    val pillBottom = pillTop + pillHeight
    val pillLeft = centerX - (pillWidth / 2f)
    val pillRight = centerX + (pillWidth / 2f)
    val pillRadius = 9.5f * density
    val pillRect = RectF(pillLeft, pillTop, pillRight, pillBottom)

    val pillBackground = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#0F1424")
        alpha = 240
        style = Paint.Style.FILL
    }
    val pillBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor(if (selecionado) "#803DF1" else "#303950")
        style = Paint.Style.STROKE
        strokeWidth = 1.2f * density
    }
    canvas.drawRoundRect(pillRect, pillRadius, pillRadius, pillBackground)
    canvas.drawRoundRect(pillRect, pillRadius, pillRadius, pillBorderPaint)

    val fontMetrics = textPaint.fontMetrics
    val textY = pillRect.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2f
    canvas.drawText(nome, centerX, textY, textPaint)

    return BitmapDrawable(context.resources, bitmap)
}

/** Marcador leve e reutilizável para ONGs não selecionadas. */
private fun criarMarcadorOngCompacto(context: Context, icone: String): Drawable {
    val density = context.resources.displayMetrics.density
    val tamanho = (44f * density).toInt()
    val bitmap = Bitmap.createBitmap(tamanho, tamanho, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    val centro = tamanho / 2f

    val sombra = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#9C64FF")
        alpha = 80
    }
    canvas.drawCircle(centro, centro, 20f * density, sombra)

    val fundo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#803DF1")
    }
    canvas.drawCircle(centro, centro, 16.5f * density, fundo)

    val borda = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 1.8f * density
        alpha = 225
    }
    canvas.drawCircle(centro, centro, 16.5f * density, borda)

    val simbolo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = 15f * density
        textAlign = Paint.Align.CENTER
    }
    val metrics = simbolo.fontMetrics
    val textoY = centro - (metrics.ascent + metrics.descent) / 2f
    canvas.drawText(icone, centro, textoY, simbolo)

    return BitmapDrawable(context.resources, bitmap)
}

/**
 * Tela de mapa completa alinhada à interface de referência do SOS+.
 */
@Composable
fun TelaMapaDoador(
    areaSelecionada: AreaDoador,
    aoSelecionarArea: (AreaDoador) -> Unit,
    localizacaoUsuario: LocalizacaoUsuario? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val manager = remember { context.getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    val pontoPadraoMapa = localizacaoUsuario?.let { GeoPoint(it.latitude, it.longitude) } ?: PontoCentralPadrao
    val nomeLocalidade = localizacaoUsuario?.descricao ?: "Sua região"

    fun permitido() = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED

    var autorizado by remember { mutableStateOf(permitido()) }
    var ativo by remember { mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) }
    var posicao by remember { mutableStateOf<Location?>(null) }
    var ongs by remember { mutableStateOf(OngsPadraoNovaFriburgo) }
    var ongSelecionada by remember { mutableStateOf<OngItem?>(null) }
    var mapaEscuro by remember { mutableStateOf(true) }
    var exibirDialogFiltro by remember { mutableStateOf(false) }
    var filtroCategoria by remember { mutableStateOf<String?>(null) }
    var exibirTodasOngs by remember { mutableStateOf(false) }
    var mapaCentralizadoNaPosicao by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        autorizado = permitido()
        if (!autorizado) {
            posicao = null
        }
    }

    // Fontes de mapa: Dark Matter (idêntico à referência) e OpenStreetMap padrão
    val darkTileSource = remember {
        XYTileSource(
            "CartoDark",
            0, 20, 256, ".png",
            arrayOf(
                "https://a.basemaps.cartocdn.com/rastertiles/dark_all/",
                "https://b.basemaps.cartocdn.com/rastertiles/dark_all/",
                "https://c.basemaps.cartocdn.com/rastertiles/dark_all/",
                "https://d.basemaps.cartocdn.com/rastertiles/dark_all/",
            ),
            "© OpenStreetMap contributors, © CARTO",
        )
    }

    val osmTileSource = remember {
        XYTileSource(
            "OpenStreetMap",
            0, 19, 256, ".png",
            arrayOf("https://tile.openstreetmap.org/"),
            "© OpenStreetMap contributors",
        )
    }

    val map = remember {
        Configuration.getInstance().apply {
            userAgentValue = "SOSPlus/1.0 (br.com.sosplus)"
            osmdroidBasePath = java.io.File(context.cacheDir, "osm")
            osmdroidTileCache = java.io.File(context.cacheDir, "osm/tiles")
            expirationExtendedDuration = 7L * 24 * 60 * 60 * 1000
        }
        MapView(context).apply {
            setTileSource(if (mapaEscuro) darkTileSource else osmTileSource)
            setMultiTouchControls(true)
            // Evita criar bitmaps ampliados em telas de alta densidade, reduzindo
            // pressão de memória durante zoom e rolagem do mapa.
            isTilesScaledToDpi = false
            // Nível de zoom maior inicial focando em ruas e bairros
            controller.setZoom(15.8)
            controller.setCenter(pontoPadraoMapa)
        }
    }

    // Os bitmaps dos marcadores são caros para desenhar. Mantê-los em cache evita
    // recriá-los a cada recomposição da tela ou atualização de localização.
    val iconesOng = remember(context) { mutableMapOf<ChaveIconeOng, Drawable>() }
    val marcadorUsuario = remember(map, context) {
        Marker(map).apply {
            icon = criarMarcadorUsuario(context)
            position = pontoPadraoMapa
            setAnchor(0.5f, 0.32f)
            setOnMarkerClickListener { _, _ ->
                position?.let { ponto ->
                    map.controller.setZoom(16.5)
                    map.controller.animateTo(ponto)
                }
                true
            }
        }
    }

    // Gerenciamento de ciclo de vida do MapView
    DisposableEffect(lifecycle, map) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                autorizado = permitido()
                ativo = true
                map.onResume()
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                ativo = false
                map.onPause()
            }
        }
        lifecycle.addObserver(observer)
        if (ativo) map.onResume()
        onDispose {
            lifecycle.removeObserver(observer)
            map.onPause()
            map.onDetach()
        }
    }

    // Localização aproximada por rede (Wi-Fi/antenas), sem provedor de satélite.
    DisposableEffect(autorizado, ativo) {
        var disposed = false
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (!disposed && autorizado && ativo && permitido() && deveAtualizarPosicao(posicao, location)) {
                    posicao = location
                }
            }
            override fun onProviderDisabled(provider: String) {}
            override fun onProviderEnabled(provider: String) {}
        }
        if (autorizado && ativo) {
            try {
                val providers = listOf(LocationManager.NETWORK_PROVIDER)
                    .filter { manager.allProviders.contains(it) }
                providers.forEach {
                    manager.requestLocationUpdates(
                        it,
                        INTERVALO_GPS_MS,
                        DISTANCIA_MINIMA_GPS_METROS,
                        listener,
                        Looper.getMainLooper(),
                    )
                }
            } catch (_: SecurityException) {
                autorizado = false
                posicao = null
            } catch (_: IllegalArgumentException) {}
        }
        onDispose {
            disposed = true
            manager.removeUpdates(listener)
        }
    }

    // A posição muda com frequência. Atualizamos somente o marcador e centralizamos
    // automaticamente uma vez, sem interromper a navegação manual no mapa.
    LaunchedEffect(posicao) {
        posicao?.let {
            marcadorUsuario.position = GeoPoint(it.latitude, it.longitude)
            if (marcadorUsuario !in map.overlays) map.overlays.add(marcadorUsuario)

            if (!mapaCentralizadoNaPosicao) {
                map.controller.setZoom(16.0)
                map.controller.animateTo(marcadorUsuario.position)
                mapaCentralizadoNaPosicao = true
            }
            map.invalidate()
        }
    }

    // Enquanto uma nova leitura não chega, o mapa usa a última localização salva
    // no login como centro e como referência para distância das ONGs.
    LaunchedEffect(localizacaoUsuario?.latitude, localizacaoUsuario?.longitude) {
        if (posicao == null && localizacaoUsuario != null) {
            marcadorUsuario.position = pontoPadraoMapa
            if (marcadorUsuario !in map.overlays) map.overlays.add(marcadorUsuario)
            map.controller.setCenter(pontoPadraoMapa)
            map.invalidate()
        }
    }

    // Atualiza tile source ao alternar camada
    LaunchedEffect(mapaEscuro) {
        map.setTileSource(if (mapaEscuro) darkTileSource else osmTileSource)
        map.invalidate()
    }

    // Busca ONGs do backend (e mescla com as ONGs de referência)
    LaunchedEffect(ativo) {
        if (ativo) while (true) {
            try {
                val novasOngs = withContext(Dispatchers.IO) {
                    val urlStr = BuildConfig.API_BASE_URL.trimEnd('/') + "/api/v1/ongs/mapa"
                    val call = NetworkLogger.request("GET", urlStr)
                    val c = URL(urlStr).openConnection() as HttpURLConnection
                    try {
                        c.connectTimeout = 8000
                        c.readTimeout = 8000
                        c.setRequestProperty("X-Request-Id", call.id)
                        val status = c.responseCode
                        val stream = if (status in 200..299) c.inputStream else c.errorStream
                        val responseBody = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                        NetworkLogger.response(call, status, responseBody)
                        if (status == 200) {
                            val resp = JSONObject(responseBody)
                            val array = resp.getJSONArray("ongs")
                            List(array.length()) { i ->
                                val o = array.getJSONObject(i)
                                OngItem(
                                    id = o.optString("id", (i + 10).toString()),
                                    nome = o.getString("nome"),
                                    categoria = "Apoio social",
                                    icone = "♡",
                                    latitude = o.getDouble("latitude"),
                                    longitude = o.getDouble("longitude"),
                                )
                            }
                        } else emptyList()
                    } catch (error: Exception) {
                        NetworkLogger.failure(call, error)
                        throw error
                    } finally {
                        c.disconnect()
                    }
                }
                if (novasOngs.isNotEmpty()) {
                    val existentesIds = novasOngs.map { it.id }.toSet()
                    val mescladas = novasOngs + OngsPadraoNovaFriburgo.filter { it.id !in existentesIds }
                    // Só altera o estado se a resposta trouxe uma mudança real. Assim,
                    // um polling sem novidades não força uma nova montagem do mapa.
                    if (mescladas != ongs) ongs = mescladas
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (_: Exception) {}
            delay(INTERVALO_ATUALIZACAO_ONGS_MS)
        }
    }

    // Filtro ativo de ONGs
    val ongsFiltradas = remember(ongs, filtroCategoria) {
        if (filtroCategoria == null) ongs else ongs.filter { it.categoria == filtroCategoria }
    }
    val textoStatusGps = when {
        !autorizado && localizacaoUsuario != null -> "Localização salva"
        !autorizado -> "Localização desativada"
        posicao == null && localizacaoUsuario != null -> "Localização salva"
        posicao == null -> "Buscando localização…"
        else -> "Localização ativa"
    }
    val corStatusGps = when {
        !autorizado -> TextoSecundarioInicio
        posicao == null && localizacaoUsuario != null -> CorSucessoInicio
        posicao == null -> RoxoClaroInicio
        else -> CorSucessoInicio
    }

    // A API pode devolver centenas de ONGs. Exibimos os pins mais próximos e sempre
    // preservamos a seleção atual, evitando uma alocação em massa na thread principal.
    val ongsParaMarcadores = remember(
        ongsFiltradas,
        ongSelecionada?.id,
        localizacaoUsuario?.latitude,
        localizacaoUsuario?.longitude,
    ) {
        if (ongsFiltradas.size <= MAX_MARCADORES_ONG_NO_MAPA) {
            ongsFiltradas
        } else {
            val proximas = ongsFiltradas
                .sortedBy { ong -> GeoPoint(ong.latitude, ong.longitude).distanceToAsDouble(pontoPadraoMapa) }
                .take(MAX_MARCADORES_ONG_NO_MAPA)
            val selecionada = ongsFiltradas.firstOrNull { it.id == ongSelecionada?.id }
            if (selecionada != null && selecionada !in proximas) {
                proximas.dropLast(1) + selecionada
            } else {
                proximas
            }
        }
    }

    // Atualiza a camada somente quando os pins visíveis, o filtro ou a seleção
    // mudam. Pins comuns compartilham um bitmap compacto por ícone; somente a
    // seleção ganha etiqueta completa com o nome da ONG.
    LaunchedEffect(ongsParaMarcadores, ongSelecionada?.id) {
        map.overlays.clear()
        val chaveSelecionada = ongsParaMarcadores
            .firstOrNull { it.id == ongSelecionada?.id }
            ?.let { ChaveIconeOng(it.icone, it.nome, selecionado = true) }
        iconesOng.keys.removeAll { it.selecionado && it != chaveSelecionada }

        ongsParaMarcadores.forEach { ong ->
            val selecionada = ong.id == ongSelecionada?.id
            val chaveIcone = ChaveIconeOng(
                icone = ong.icone,
                nomeEtiqueta = if (selecionada) ong.nome else null,
                selecionado = selecionada,
            )
            val marker = Marker(map).apply {
                position = GeoPoint(ong.latitude, ong.longitude)
                icon = iconesOng.getOrPut(chaveIcone) {
                    if (selecionada) criarMarcadorOng(context, ong.nome, ong.icone, selecionado = true)
                    else criarMarcadorOngCompacto(context, ong.icone)
                }
                setAnchor(0.5f, if (selecionada) 0.35f else 0.5f)
                setOnMarkerClickListener { _, _ ->
                    ongSelecionada = ong
                    map.controller.setZoom(16.5)
                    map.controller.animateTo(position)
                    true
                }
            }
            map.overlays.add(marker)
        }

        if (posicao != null || localizacaoUsuario != null) map.overlays.add(marcadorUsuario)
        map.invalidate()
    }

    // Função de centralização na posição atual ou padrão
    val centralizarNoUsuario: () -> Unit = {
        if (!autorizado) {
            launcher.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION))
        }
        val destino = posicao?.let { GeoPoint(it.latitude, it.longitude) } ?: pontoPadraoMapa
        map.controller.setZoom(16.2)
        map.controller.animateTo(destino)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(RoxoInicio.copy(alpha = 0.18f), FundoInicio),
                    radius = 900f,
                ),
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // Top Bar: Logo SOS+ e Botão Circular de Filtro
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                LogoInicio()

                // Botão de filtros circular igual à imagem de referência
                IconButton(
                    onClick = { exibirDialogFiltro = true },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SuperficieInicio)
                        .padding(1.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SuperficieInicio, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        IconeFiltroSliders(cor = if (filtroCategoria != null) RoxoClaroInicio else TextoInicio)
                    }
                }
            }

            // Título e Subtítulo
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                Text(
                    text = "ONGs perto de você",
                    color = TextoInicio,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Encontre causas para apoiar na sua região",
                    color = TextoSecundarioInicio,
                    fontSize = 13.5.sp,
                    modifier = Modifier.padding(top = 3.dp),
                )
                Row(
                    modifier = Modifier.padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        color = corStatusGps.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, corStatusGps.copy(alpha = 0.42f)),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(corStatusGps, CircleShape),
                            )
                            Text(
                                text = textoStatusGps,
                                color = if (autorizado) TextoInicio else TextoSecundarioInicio,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                    Surface(
                        color = RoxoInicio.copy(alpha = 0.17f),
                        shape = RoundedCornerShape(20.dp),
                    ) {
                        Text(
                            text = "${ongsFiltradas.size} ONGs exibidas",
                            color = RoxoClaroInicio,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        )
                    }
                }
            }

            // Área Central do Mapa
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SuperficieInicio),
                    border = BorderStroke(1.dp, BordaInicio),
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // osmdroid MapView
                        AndroidView(factory = { map }, modifier = Modifier.fillMaxSize())

                        // Pílula flutuante com a cidade/estado salvos no último login.
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(14.dp),
                            color = SuperficieInicio.copy(alpha = 0.92f),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, BordaInicio),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text("📍", fontSize = 13.sp)
                                Text(
                                    text = nomeLocalidade,
                                    color = TextoInicio,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }

                        // Botões Flutuantes no canto inferior direito
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            // Botão de Centralização GPS (mira) com zoom ampliado
                            Surface(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clickable { centralizarNoUsuario() },
                                color = SuperficieInicio.copy(alpha = 0.95f),
                                shape = CircleShape,
                                border = BorderStroke(1.dp, BordaInicio),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    IconeMiraGps(cor = TextoInicio)
                                }
                            }

                            // Botão de Camadas (Layers)
                            Surface(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clickable { mapaEscuro = !mapaEscuro },
                                color = SuperficieInicio.copy(alpha = 0.95f),
                                shape = CircleShape,
                                border = BorderStroke(1.dp, BordaInicio),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    IconeCamadasMapa(cor = if (mapaEscuro) RoxoClaroInicio else TextoInicio)
                                }
                            }
                        }
                    }
                }
            }

            // Painel Inferior: "ONGs mais próximas"
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SuperficieInicio,
                shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
                border = BorderStroke(1.dp, BordaInicio),
            ) {
                Column(
                    modifier = Modifier.padding(top = 10.dp, bottom = 12.dp),
                ) {
                    // Puxador central (drag handle)
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(38.dp)
                            .height(4.dp)
                            .background(BordaInicio.copy(alpha = 0.8f), RoundedCornerShape(2.dp)),
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Linha do título e botão "Ver todas >"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "ONGs mais próximas",
                            color = TextoInicio,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )

                        Surface(
                            modifier = Modifier.clickable { exibirTodasOngs = true },
                            color = RoxoInicio,
                            shape = RoundedCornerShape(20.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = "Ver todas",
                                    color = TextoInicio,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = "›",
                                    color = TextoInicio,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Carrossel Horizontal de Cards de ONGs
                    val pontoReferencia = posicao?.let { GeoPoint(it.latitude, it.longitude) } ?: pontoPadraoMapa
                    val ongsOrdenadas = remember(ongsFiltradas, pontoReferencia) {
                        ongsFiltradas.sortedBy {
                            GeoPoint(it.latitude, it.longitude).distanceToAsDouble(pontoReferencia)
                        }
                    }

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(ongsOrdenadas, key = { it.id }) { ong ->
                            val distanciaMetros = GeoPoint(ong.latitude, ong.longitude).distanceToAsDouble(pontoReferencia)
                            val distanciaTexto = if (distanciaMetros < 1000) {
                                "${distanciaMetros.toInt()} m"
                            } else {
                                String.format(Locale.forLanguageTag("pt-BR"), "%.1f km", distanciaMetros / 1000.0)
                            }
                            val isSelecionada = ong.id == ongSelecionada?.id

                            CartaoOngCarrossel(
                                ong = ong,
                                distanciaTexto = distanciaTexto,
                                isSelecionada = isSelecionada,
                                onClick = {
                                    ongSelecionada = ong
                                    map.controller.setZoom(16.5)
                                    map.controller.animateTo(GeoPoint(ong.latitude, ong.longitude))
                                },
                            )
                        }
                    }
                }
            }

            // Barra de Navegação Inferior
            NavegacaoDoador(
                areaSelecionada = areaSelecionada,
                aoSelecionarArea = aoSelecionarArea,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }

        // Diálogo de Filtro de Causas
        if (exibirDialogFiltro) {
            AlertDialog(
                onDismissRequest = { exibirDialogFiltro = false },
                containerColor = SuperficieInicio,
                shape = RoundedCornerShape(22.dp),
                title = {
                    Text("Filtrar por causa", color = TextoInicio, fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        val categorias = listOf(
                            null to "Todas as causas",
                            "Apoio social" to "♡  Apoio social",
                            "Proteção animal" to "🐾  Proteção animal",
                            "Educação" to "📚  Educação",
                            "Alimentação & Agasalho" to "🛍  Alimentação & Agasalho",
                        )
                        categorias.forEach { (cat, rotulo) ->
                            val selecionado = filtroCategoria == cat
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        filtroCategoria = cat
                                        exibirDialogFiltro = false
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = if (selecionado) RoxoInicio.copy(alpha = 0.35f) else Color.Transparent,
                                border = if (selecionado) BorderStroke(1.dp, RoxoClaroInicio) else null,
                            ) {
                                Text(
                                    text = rotulo,
                                    color = if (selecionado) TextoInicio else TextoSecundarioInicio,
                                    fontSize = 14.sp,
                                    fontWeight = if (selecionado) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { exibirDialogFiltro = false }) {
                        Text("Fechar", color = RoxoClaroInicio)
                    }
                },
            )
        }

        // Diálogo "Ver todas as ONGs"
        if (exibirTodasOngs) {
            AlertDialog(
                onDismissRequest = { exibirTodasOngs = false },
                containerColor = SuperficieInicio,
                shape = RoundedCornerShape(22.dp),
                title = {
                    Text("Todas as ONGs na região", color = TextoInicio, fontWeight = FontWeight.Bold)
                },
                text = {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(ongsFiltradas, key = { it.id }) { ong ->
                            val pontoReferencia = posicao?.let { GeoPoint(it.latitude, it.longitude) } ?: pontoPadraoMapa
                            val distMetros = GeoPoint(ong.latitude, ong.longitude).distanceToAsDouble(pontoReferencia)
                            val distTexto = String.format(Locale.forLanguageTag("pt-BR"), "%.1f km", distMetros / 1000.0)

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        ongSelecionada = ong
                                        exibirTodasOngs = false
                                        map.controller.setZoom(16.5)
                                        map.controller.animateTo(GeoPoint(ong.latitude, ong.longitude))
                                    },
                                color = Color(0xFF161C2E),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, BordaInicio),
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .background(RoxoInicio, CircleShape),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(ong.icone, color = Color.White, fontSize = 16.sp)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(ong.nome, color = TextoInicio, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(ong.categoria, color = TextoSecundarioInicio, fontSize = 12.sp)
                                    }
                                    Text("📍 $distTexto", color = RoxoClaroInicio, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { exibirTodasOngs = false }) {
                        Text("Fechar", color = RoxoClaroInicio)
                    }
                },
            )
        }
    }
}

/**
 * Card de cada ONG no carrossel horizontal, seguindo o padrão exato da imagem de referência.
 */
@Composable
private fun CartaoOngCarrossel(
    ong: OngItem,
    distanciaTexto: String,
    isSelecionada: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .width(240.dp)
            .height(78.dp)
            .clickable(onClick = onClick),
        color = Color(0xFF151B2E),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, if (isSelecionada) RoxoClaroInicio else BordaInicio),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Avatar circular roxo com símbolo
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(RoxoInicio, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = ong.icone,
                    color = Color.White,
                    fontSize = 18.sp,
                )
            }

            // Textos: Nome, Categoria e Distância
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = ong.nome,
                    color = TextoInicio,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = ong.categoria,
                    color = TextoSecundarioInicio,
                    fontSize = 11.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.padding(top = 1.dp),
                ) {
                    Text("📍", fontSize = 10.sp)
                    Text(
                        text = distanciaTexto,
                        color = TextoSecundarioInicio,
                        fontSize = 11.sp,
                    )
                }
            }

            // Seta direita
            Text(
                text = "›",
                color = TextoSecundarioInicio,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

// -----------------------------------------------------------------------------------------
// Ícones desenhados com Canvas (nitidez impecável, sem dependências externas)
// -----------------------------------------------------------------------------------------

@Composable
fun IconeFiltroSliders(cor: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(19.dp)) {
        val stroke = 1.8.dp.toPx()
        val radius = 2.4.dp.toPx()

        // Linha 1
        drawLine(cor, Offset(2.dp.toPx(), 4.dp.toPx()), Offset(17.dp.toPx(), 4.dp.toPx()), strokeWidth = stroke, cap = StrokeCap.Round)
        drawCircle(cor, radius = radius, center = Offset(7.dp.toPx(), 4.dp.toPx()))

        // Linha 2
        drawLine(cor, Offset(2.dp.toPx(), 9.5.dp.toPx()), Offset(17.dp.toPx(), 9.5.dp.toPx()), strokeWidth = stroke, cap = StrokeCap.Round)
        drawCircle(cor, radius = radius, center = Offset(13.5.dp.toPx(), 9.5.dp.toPx()))

        // Linha 3
        drawLine(cor, Offset(2.dp.toPx(), 15.dp.toPx()), Offset(17.dp.toPx(), 15.dp.toPx()), strokeWidth = stroke, cap = StrokeCap.Round)
        drawCircle(cor, radius = radius, center = Offset(9.dp.toPx(), 15.dp.toPx()))
    }
}

@Composable
fun IconeMiraGps(cor: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(20.dp)) {
        val stroke = 1.8.dp.toPx()
        val center = Offset(size.width / 2, size.height / 2)
        val radius = 6.5.dp.toPx()

        drawCircle(cor, radius = radius, center = center, style = Stroke(stroke))
        drawCircle(cor, radius = 2.dp.toPx(), center = center)

        // Marcas nos 4 eixos
        drawLine(cor, Offset(center.x, center.y - radius - 2.5.dp.toPx()), Offset(center.x, center.y - radius + 1.dp.toPx()), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(cor, Offset(center.x, center.y + radius - 1.dp.toPx()), Offset(center.x, center.y + radius + 2.5.dp.toPx()), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(cor, Offset(center.x - radius - 2.5.dp.toPx(), center.y), Offset(center.x - radius + 1.dp.toPx(), center.y), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(cor, Offset(center.x + radius - 1.dp.toPx(), center.y), Offset(center.x + radius + 2.5.dp.toPx(), center.y), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

@Composable
fun IconeCamadasMapa(cor: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(20.dp)) {
        val stroke = 1.7.dp.toPx()
        val w = size.width

        // Losango superior
        val path1 = Path().apply {
            moveTo(w / 2f, 3.dp.toPx())
            lineTo(w - 2.5.dp.toPx(), 7.dp.toPx())
            lineTo(w / 2f, 11.dp.toPx())
            lineTo(2.5.dp.toPx(), 7.dp.toPx())
            close()
        }
        drawPath(path1, cor, style = Stroke(stroke, cap = StrokeCap.Round))

        // Camada intermediária
        val path2 = Path().apply {
            moveTo(2.5.dp.toPx(), 11.dp.toPx())
            lineTo(w / 2f, 15.dp.toPx())
            lineTo(w - 2.5.dp.toPx(), 11.dp.toPx())
        }
        drawPath(path2, cor, style = Stroke(stroke, cap = StrokeCap.Round))

        // Camada inferior
        val path3 = Path().apply {
            moveTo(2.5.dp.toPx(), 15.dp.toPx())
            lineTo(w / 2f, 19.dp.toPx())
            lineTo(w - 2.5.dp.toPx(), 15.dp.toPx())
        }
        drawPath(path3, cor, style = Stroke(stroke, cap = StrokeCap.Round))
    }
}

@Composable
fun IconeInicioNav(cor: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(22.dp)) {
        val stroke = 1.8.dp.toPx()
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(3.dp.toPx(), 10.dp.toPx())
            lineTo(w / 2f, 3.5.dp.toPx())
            lineTo(w - 3.dp.toPx(), 10.dp.toPx())
            lineTo(w - 5.dp.toPx(), 10.dp.toPx())
            lineTo(w - 5.dp.toPx(), h - 3.dp.toPx())
            lineTo(5.dp.toPx(), h - 3.dp.toPx())
            lineTo(5.dp.toPx(), 10.dp.toPx())
            close()
        }
        drawPath(path, cor, style = Stroke(stroke, cap = StrokeCap.Round))
    }
}

@Composable
fun IconeMapaNav(cor: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(22.dp)) {
        val stroke = 1.8.dp.toPx()
        val h = size.height

        // Painel 1
        val p1 = Path().apply {
            moveTo(3.dp.toPx(), 5.5.dp.toPx())
            lineTo(8.5.dp.toPx(), 3.5.dp.toPx())
            lineTo(8.5.dp.toPx(), h - 5.5.dp.toPx())
            lineTo(3.dp.toPx(), h - 3.5.dp.toPx())
            close()
        }
        // Painel 2
        val p2 = Path().apply {
            moveTo(8.5.dp.toPx(), 3.5.dp.toPx())
            lineTo(13.5.dp.toPx(), 5.5.dp.toPx())
            lineTo(13.5.dp.toPx(), h - 3.5.dp.toPx())
            lineTo(8.5.dp.toPx(), h - 5.5.dp.toPx())
            close()
        }
        // Painel 3
        val p3 = Path().apply {
            moveTo(13.5.dp.toPx(), 5.5.dp.toPx())
            lineTo(19.dp.toPx(), 3.5.dp.toPx())
            lineTo(19.dp.toPx(), h - 5.5.dp.toPx())
            lineTo(13.5.dp.toPx(), h - 3.5.dp.toPx())
            close()
        }
        drawPath(p1, cor, style = Stroke(stroke))
        drawPath(p2, cor, style = Stroke(stroke))
        drawPath(p3, cor, style = Stroke(stroke))
    }
}

@Composable
fun IconeCarteiraNav(cor: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(22.dp)) {
        val stroke = 1.8.dp.toPx()
        val w = size.width
        val h = size.height

        drawRoundRect(
            cor,
            topLeft = Offset(3.dp.toPx(), 5.dp.toPx()),
            size = Size(w - 6.dp.toPx(), h - 9.dp.toPx()),
            cornerRadius = CornerRadius(4.dp.toPx()),
            style = Stroke(stroke),
        )
        drawLine(cor, Offset(3.dp.toPx(), 9.5.dp.toPx()), Offset(w - 3.dp.toPx(), 9.5.dp.toPx()), strokeWidth = stroke)
        drawCircle(cor, radius = 1.8.dp.toPx(), center = Offset(w - 6.5.dp.toPx(), 12.dp.toPx()))
    }
}

@Composable
fun IconePerfilNav(cor: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(22.dp)) {
        val stroke = 1.8.dp.toPx()
        val w = size.width
        val h = size.height

        // Cabeça
        drawCircle(cor, radius = 3.8.dp.toPx(), center = Offset(w / 2f, 7.5.dp.toPx()), style = Stroke(stroke))
        // Tronco
        val path = Path().apply {
            moveTo(4.dp.toPx(), h - 3.5.dp.toPx())
            quadraticTo(w / 2f, 13.dp.toPx(), w - 4.dp.toPx(), h - 3.5.dp.toPx())
        }
        drawPath(path, cor, style = Stroke(stroke, cap = StrokeCap.Round))
    }
}

/**
 * Compatibilidade com chamadas antigas para MapaAberto.
 */
@Composable
fun MapaAberto() {
    TelaMapaDoador(
        areaSelecionada = AreaDoador.Mapa,
        aoSelecionarArea = {},
    )
}
