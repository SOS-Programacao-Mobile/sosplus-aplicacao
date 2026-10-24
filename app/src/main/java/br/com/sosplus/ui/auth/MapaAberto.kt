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
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import br.com.sosplus.data.Categoria
import br.com.sosplus.data.DomainRepository
import br.com.sosplus.data.LocalizacaoUsuario
import br.com.sosplus.data.OrganizacaoPublica
import br.com.sosplus.ui.components.SosCampoTexto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
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
    val categorias: List<Categoria> = emptyList(),
)

internal fun filtrarOngsPorCategoria(ongs: List<OngItem>, categoriaId: String?): List<OngItem> =
    if (categoriaId == null) ongs else ongs.filter { ong -> ong.categorias.any { it.id == categoriaId } }

// Ponto central padrão de Nova Friburgo, RJ
val PontoCentralPadrao = GeoPoint(-22.2819, -42.5311)

private const val INTERVALO_GPS_MS = 10_000L
private const val DISTANCIA_MINIMA_GPS_METROS = 12f
private const val INTERVALO_MINIMO_REDESENHO_GPS_MS = 20_000L
private const val INTERVALO_ATUALIZACAO_ONGS_MS = 120_000L
private const val MAX_MARCADORES_ONG_NO_MAPA = 80
private const val TIMEOUT_PRIMEIRA_LOCALIZACAO_MS = 15_000L
private const val IDADE_MAXIMA_ULTIMA_POSICAO_MS = 5 * 60 * 1_000L

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
    token: String? = null,
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
    var ongs by remember { mutableStateOf<List<OngItem>>(emptyList()) }
    var carregandoOngs by remember { mutableStateOf(true) }
    var erroOngs by remember { mutableStateOf<String?>(null) }
    var ongSelecionada by remember { mutableStateOf<OngItem?>(null) }
    var mapaEscuro by remember { mutableStateOf(true) }
    var exibirDialogFiltro by remember { mutableStateOf(false) }
    var filtroCategoria by remember { mutableStateOf<String?>(null) }
    var exibirTodasOngs by remember { mutableStateOf(false) }
    var catalogo by remember { mutableStateOf<List<OrganizacaoPublica>>(emptyList()) }
    var categoriaCatalogo by remember { mutableStateOf<String?>(null) }
    var proximaPaginaCatalogo by remember { mutableStateOf<String?>(null) }
    var carregandoCatalogo by remember { mutableStateOf(false) }
    var erroCatalogo by remember { mutableStateOf<String?>(null) }
    var ongIdAberta by remember { mutableStateOf<String?>(null) }
    var campanhaIdAberta by remember { mutableStateOf<String?>(null) }
    var categoriasDisponiveis by remember { mutableStateOf<List<Categoria>>(emptyList()) }
    val repository = remember { DomainRepository() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        try { categoriasDisponiveis = repository.categorias() }
        catch (error: CancellationException) { throw error }
        catch (_: Exception) { /* O mapa e o catálogo continuam disponíveis sem o filtro. */ }
    }
    fun abrirPerfil(id: String) {
        ongIdAberta = id
    }

    BackHandler(enabled = campanhaIdAberta != null || ongIdAberta != null) {
        if (campanhaIdAberta != null) campanhaIdAberta = null
        else ongIdAberta = null
    }

    if (campanhaIdAberta != null) {
        DetalheCampanha(
            campanhaId = requireNotNull(campanhaIdAberta),
            token = token.orEmpty(),
            aoVoltar = { campanhaIdAberta = null },
            aoAbrirOng = { idOng ->
                campanhaIdAberta = null
                ongIdAberta = idOng
            },
            modifier = modifier,
        )
        return
    }

    if (ongIdAberta != null) {
        PerfilOngPublico(
            ongId = requireNotNull(ongIdAberta),
            token = token.orEmpty(),
            aoVoltar = { ongIdAberta = null },
            aoAbrirCampanha = { idCamp ->
                campanhaIdAberta = idCamp
            },
            modifier = modifier,
        )
        return
    }
    fun carregarCatalogo(cursor: String? = null, reiniciar: Boolean = false) {
        if (carregandoCatalogo) return
        carregandoCatalogo = true
        erroCatalogo = null
        scope.launch {
            try {
                val page = repository.organizacoes(cursor, filtroCategoria)
                catalogo = if (reiniciar) page.itens else catalogo + page.itens
                proximaPaginaCatalogo = page.proximaPagina
                categoriaCatalogo = filtroCategoria
            } catch (error: CancellationException) { throw error }
            catch (error: Exception) { erroCatalogo = error.message ?: "Não foi possível carregar o catálogo." }
            finally { carregandoCatalogo = false }
        }
    }
    var mapaCentralizadoNaPosicao by remember { mutableStateOf(false) }
    var negadaDefinitivamente by rememberSaveable { mutableStateOf(false) }
    var permissaoSolicitadaAoAbrir by rememberSaveable { mutableStateOf(false) }
    var servicoIndisponivel by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        autorizado = permitido()
        if (!autorizado) {
            posicao = null
            // Sem justificativa disponível após uma recusa, o sistema não exibirá mais
            // o diálogo: o usuário só pode liberar o acesso nas configurações do app.
            val activity = context.encontrarActivity()
            negadaDefinitivamente = activity != null &&
                !activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
        } else {
            negadaDefinitivamente = false
        }
    }

    // Ao abrir o mapa, solicita a permissão uma única vez. Se o usuário recusar,
    // o mapa segue funcionando com a última localização salva ou a região padrão.
    LaunchedEffect(Unit) {
        if (!autorizado && !permissaoSolicitadaAoAbrir) {
            permissaoSolicitadaAoAbrir = true
            launcher.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION))
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
                // A permissão pode ter mudado nas configurações enquanto o app estava pausado.
                autorizado = permitido()
                if (autorizado) negadaDefinitivamente = false else posicao = null
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
        servicoIndisponivel = false
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (!disposed && autorizado && ativo && permitido()) {
                    servicoIndisponivel = false
                    if (deveAtualizarPosicao(posicao, location)) posicao = location
                }
            }
            override fun onProviderDisabled(provider: String) {
                if (!disposed) servicoIndisponivel = true
            }
            override fun onProviderEnabled(provider: String) {
                if (!disposed) servicoIndisponivel = false
            }
        }
        if (autorizado && ativo) {
            val provider = LocationManager.NETWORK_PROVIDER
            try {
                if (provider !in manager.allProviders || !manager.isProviderEnabled(provider)) {
                    servicoIndisponivel = true
                }
                // Uma leitura recente já conhecida permite centralizar o mapa sem esperar.
                if (posicao == null) {
                    manager.getLastKnownLocation(provider)
                        ?.takeIf { System.currentTimeMillis() - it.time <= IDADE_MAXIMA_ULTIMA_POSICAO_MS }
                        ?.let { posicao = it }
                }
                if (provider in manager.allProviders) {
                    // Registrado mesmo com o provedor desligado: onProviderEnabled avisa quando voltar.
                    manager.requestLocationUpdates(
                        provider,
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

    // Se nenhuma leitura chegar a tempo, informa que a localização está indisponível
    // em vez de manter o estado "buscando" indefinidamente.
    LaunchedEffect(autorizado, ativo, posicao == null) {
        if (autorizado && ativo && posicao == null) {
            delay(TIMEOUT_PRIMEIRA_LOCALIZACAO_MS)
            if (posicao == null) servicoIndisponivel = true
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

    // A lista remota substitui o estado inteiro, inclusive quando não há resultados.
    LaunchedEffect(ativo) {
        if (ativo) while (true) {
            try {
                val remotas = repository.mapaOngs()
                ongs = remotas.map { ong ->
                    val categorias = ong.categorias
                    OngItem(ong.id, ong.nome, categorias.firstOrNull()?.nome ?: "Sem categoria", "♡",
                        ong.latitude, ong.longitude, categorias = categorias)
                }
                erroOngs = null
            } catch (error: CancellationException) { throw error }
            catch (error: Exception) { erroOngs = error.message ?: "Não foi possível carregar as ONGs." }
            finally { carregandoOngs = false }
            delay(INTERVALO_ATUALIZACAO_ONGS_MS)
        }
    }

    // Filtro ativo de ONGs
    val ongsFiltradas = remember(ongs, filtroCategoria) {
        filtrarOngsPorCategoria(ongs, filtroCategoria)
    }
    val estadoLocalizacao = estadoLocalizacaoMapa(
        autorizado = autorizado,
        negadaDefinitivamente = negadaDefinitivamente,
        temPosicaoAtual = posicao != null,
        servicoIndisponivel = servicoIndisponivel,
    )
    val temLocalizacaoSalva = localizacaoUsuario != null
    val textoStatusGps = textoStatusLocalizacao(estadoLocalizacao, temLocalizacaoSalva)
    val avisoLocalizacao = avisoLocalizacao(estadoLocalizacao, temLocalizacaoSalva)
    val corStatusGps = when (estadoLocalizacao) {
        EstadoLocalizacaoMapa.Ativa -> CorSucessoInicio
        EstadoLocalizacaoMapa.Buscando -> if (temLocalizacaoSalva) CorSucessoInicio else RoxoClaroInicio
        else -> TextoSecundarioInicio
    }
    // Referência para distâncias e para escolher os pins mais próximos: posição
    // atual quando disponível, senão a última salva ou a região padrão.
    val pontoReferencia = posicao?.let { GeoPoint(it.latitude, it.longitude) } ?: pontoPadraoMapa

    // A API pode devolver centenas de ONGs. Exibimos os pins mais próximos e sempre
    // preservamos a seleção atual, evitando uma alocação em massa na thread principal.
    val ongsParaMarcadores = remember(
        ongsFiltradas,
        ongSelecionada?.id,
        pontoReferencia,
    ) {
        if (ongsFiltradas.size <= MAX_MARCADORES_ONG_NO_MAPA) {
            ongsFiltradas
        } else {
            val proximas = ongsFiltradas
                .sortedBy { ong -> GeoPoint(ong.latitude, ong.longitude).distanceToAsDouble(pontoReferencia) }
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
                                    abrirPerfil(ong.id)
                    true
                }
            }
            map.overlays.add(marker)
        }

        if (posicao != null || localizacaoUsuario != null) map.overlays.add(marcadorUsuario)
        map.invalidate()
    }

    // Função de centralização na posição atual ou padrão
    val resolverLocalizacao: () -> Unit = {
        when (estadoLocalizacao) {
            EstadoLocalizacaoMapa.PermissaoNecessaria ->
                launcher.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION))
            EstadoLocalizacaoMapa.PermissaoNegadaDefinitivamente -> abrirConfiguracoesDoApp(context)
            EstadoLocalizacaoMapa.Indisponivel -> abrirConfiguracoesDeLocalizacao(context)
            EstadoLocalizacaoMapa.Buscando, EstadoLocalizacaoMapa.Ativa -> Unit
        }
    }

    val centralizarNoUsuario: () -> Unit = {
        if (!autorizado) resolverLocalizacao()
        map.controller.setZoom(16.2)
        map.controller.animateTo(pontoReferencia)
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
                    text = if (ongsFiltradas.size > MAX_MARCADORES_ONG_NO_MAPA)
                        "Exibindo até $MAX_MARCADORES_ONG_NO_MAPA pins · veja todas no catálogo"
                    else "Encontre causas para apoiar na sua região",
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
                        when {
                            carregandoOngs && ongs.isEmpty() -> CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center), color = RoxoClaroInicio,
                            )
                            erroOngs != null -> Surface(
                                modifier = Modifier.align(Alignment.Center).padding(24.dp),
                                color = SuperficieInicio.copy(alpha = 0.96f), shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, BordaInicio),
                            ) {
                                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(erroOngs ?: "Erro", color = TextoInicio)
                                    if (ongs.isNotEmpty()) Text("Os resultados exibidos podem estar desatualizados.", color = TextoSecundarioInicio)
                                    TextButton(onClick = { carregandoOngs = true; scope.launch {
                                        try { val remote = repository.mapaOngs(); ongs = remote.map { item ->
                                            OngItem(item.id, item.nome, item.categorias.firstOrNull()?.nome ?: "Sem categoria", "♡", item.latitude, item.longitude, categorias = item.categorias)
                                        }; erroOngs = null } catch (e: CancellationException) { throw e }
                                        catch (e: Exception) { erroOngs = e.message ?: "Não foi possível carregar as ONGs." }
                                        finally { carregandoOngs = false }
                                    } }) { Text("Tentar novamente", color = RoxoClaroInicio) }
                                }
                            }
                            ongs.isEmpty() -> Text("Nenhuma ONG com localização cadastrada.", color = TextoInicio,
                                modifier = Modifier.align(Alignment.Center).background(SuperficieInicio.copy(alpha = .9f), RoundedCornerShape(12.dp)).padding(12.dp))
                        }

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
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = RoxoClaroInicio,
                                    modifier = Modifier.size(16.dp),
                                )
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

                        // Aviso quando a permissão foi negada ou a localização está indisponível.
                        // Fica no canto inferior esquerdo, sem cobrir os botões flutuantes.
                        avisoLocalizacao?.let { aviso ->
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(start = 14.dp, end = 72.dp, bottom = 14.dp)
                                    .clickable(onClick = resolverLocalizacao),
                                color = SuperficieInicio.copy(alpha = 0.95f),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, BordaInicio),
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(3.dp),
                                ) {
                                    Text(
                                        text = aviso.mensagem,
                                        color = TextoSecundarioInicio,
                                        fontSize = 11.5.sp,
                                        lineHeight = 14.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = "${aviso.acao} ›",
                                        color = RoxoClaroInicio,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
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
                                    .size(48.dp)
                                    .clickable(onClickLabel = "Centralizar localização") { centralizarNoUsuario() },
                                color = SuperficieInicio.copy(alpha = 0.95f),
                                shape = CircleShape,
                                border = BorderStroke(1.dp, BordaInicio),
                                tonalElevation = 6.dp,
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    IconeMiraGps(cor = TextoInicio)
                                }
                            }

                            // Botão de Camadas (Layers)
                            Surface(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clickable(onClickLabel = "Alternar contraste do mapa") { mapaEscuro = !mapaEscuro },
                                color = SuperficieInicio.copy(alpha = 0.95f),
                                shape = CircleShape,
                                border = BorderStroke(1.dp, BordaInicio),
                                tonalElevation = 6.dp,
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
                    val ongsOrdenadas = remember(ongsFiltradas, pontoReferencia) {
                        ongsFiltradas.sortedBy {
                            GeoPoint(it.latitude, it.longitude).distanceToAsDouble(pontoReferencia)
                        }
                    }
                    val estadoCarrossel = rememberLazyListState()
                    // Quando a ONG mais próxima muda (ex.: chegou a posição atual), volta ao início.
                    LaunchedEffect(ongsOrdenadas.firstOrNull()?.id) {
                        estadoCarrossel.scrollToItem(0)
                    }

                    LazyRow(
                        state = estadoCarrossel,
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
                                    abrirPerfil(ong.id)
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
                        val categorias = listOf(null to "Todas as causas") + categoriasDisponiveis.map { it.id to it.nome }
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
            var buscaCatalogo by remember { mutableStateOf("") }
            LaunchedEffect(exibirTodasOngs, filtroCategoria) {
                if (catalogo.isEmpty() || categoriaCatalogo != filtroCategoria) carregarCatalogo(reiniciar = true)
            }
            val catalogoFiltrado = remember(catalogo, buscaCatalogo) {
                if (buscaCatalogo.isBlank()) catalogo
                else catalogo.filter {
                    it.nome.contains(buscaCatalogo, ignoreCase = true) ||
                    it.categorias.any { cat -> cat.nome.contains(buscaCatalogo, ignoreCase = true) }
                }
            }

            AlertDialog(
                onDismissRequest = { exibirTodasOngs = false },
                containerColor = SuperficieInicio,
                shape = RoundedCornerShape(22.dp),
                title = {
                    Text("Catálogo de ONGs", color = TextoInicio, fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SosCampoTexto(
                            valor = buscaCatalogo,
                            aoMudarValor = { buscaCatalogo = it },
                            rotulo = "Buscar por nome ou causa",
                            icone = Icons.Default.Search,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                        )

                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 340.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(catalogoFiltrado, key = { it.id }) { ong ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            exibirTodasOngs = false
                                            abrirPerfil(ong.id)
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
                                                .size(40.dp)
                                                .background(RoxoInicio, CircleShape),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CorporateFare,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(ong.nome, color = TextoInicio, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(ong.categorias.joinToString { it.nome }.ifBlank { "Sem categoria" }, color = TextoSecundarioInicio, fontSize = 12.sp)
                                        }
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = TextoSecundarioInicio,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                }
                            }
                            item {
                                when {
                                    carregandoCatalogo -> CircularProgressIndicator(color = RoxoClaroInicio)
                                    erroCatalogo != null -> TextButton(onClick = { carregarCatalogo(if (catalogo.isEmpty()) null else proximaPaginaCatalogo, reiniciar = catalogo.isEmpty()) }) {
                                        Text("${erroCatalogo}. Tentar novamente", color = Color(0xFFFF8E9B))
                                    }
                                    catalogoFiltrado.isEmpty() && !carregandoCatalogo -> Text("Nenhuma ONG encontrada com esse filtro.", color = TextoSecundarioInicio)
                                    proximaPaginaCatalogo != null -> TextButton(onClick = { carregarCatalogo(proximaPaginaCatalogo) }) {
                                        Text("Carregar mais", color = RoxoClaroInicio)
                                    }
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
            .width(248.dp)
            .height(82.dp)
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
            // Avatar circular roxo com ícone Material
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(RoxoInicio, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.CorporateFare,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
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
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 1.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = RoxoClaroInicio,
                        modifier = Modifier.size(12.dp),
                    )
                    Text(
                        text = distanciaTexto,
                        color = TextoSecundarioInicio,
                        fontSize = 11.sp,
                    )
                }
            }

            // Seta direita
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ver instituição",
                tint = TextoSecundarioInicio,
                modifier = Modifier.size(20.dp),
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
