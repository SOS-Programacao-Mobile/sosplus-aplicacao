package br.com.sosplus.ui.auth

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import br.com.sosplus.data.LocalizacaoUsuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

private const val TIMEOUT_LOCALIZACAO_REDE_MS = 8_000L
private const val IDADE_MAXIMA_LOCALIZACAO_REDE_MS = 15 * 60 * 1_000L

/**
 * Obtém uma localização aproximada pela rede (Wi-Fi/antenas), sem usar o provedor
 * de satélite. O endereço é resolvido para cidade e estado antes de ser salvo.
 */
suspend fun obterLocalizacaoAtualDaRede(context: Context): LocalizacaoUsuario? {
    val coordenada = obterCoordenadaDaRede(context) ?: return null
    return resolverEndereco(context, coordenada)
}

@Suppress("DEPRECATION") // Necessário para oferecer a leitura única em Android 7 a 10.
private suspend fun obterCoordenadaDaRede(context: Context): Location? = withContext(Dispatchers.Main.immediate) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        return@withContext null
    }

    val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val provider = LocationManager.NETWORK_PROVIDER
    val ultimaConhecida = runCatching { manager.getLastKnownLocation(provider) }.getOrNull()
        ?.takeIf { System.currentTimeMillis() - it.time <= IDADE_MAXIMA_LOCALIZACAO_REDE_MS }

    if (!runCatching { manager.isProviderEnabled(provider) }.getOrDefault(false)) {
        return@withContext ultimaConhecida
    }

    val atual = withTimeoutOrNull(TIMEOUT_LOCALIZACAO_REDE_MS) {
        suspendCancellableCoroutine { continuation ->
            lateinit var listener: LocationListener
            listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    manager.removeUpdates(this)
                    if (continuation.isActive) continuation.resume(location)
                }

                override fun onProviderDisabled(provider: String) {
                    manager.removeUpdates(this)
                    if (continuation.isActive) continuation.resume(null)
                }
            }

            try {
                manager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
                continuation.invokeOnCancellation { manager.removeUpdates(listener) }
            } catch (_: SecurityException) {
                if (continuation.isActive) continuation.resume(null)
            } catch (_: IllegalArgumentException) {
                if (continuation.isActive) continuation.resume(null)
            }
        }
    }

    atual ?: ultimaConhecida
}

@Suppress("DEPRECATION") // A alternativa assíncrona do Geocoder existe apenas no Android 13.
private suspend fun resolverEndereco(context: Context, coordenada: Location): LocalizacaoUsuario? =
    withContext(Dispatchers.IO) {
        runCatching {
            if (!Geocoder.isPresent()) return@withContext null

            val endereco = Geocoder(context, Locale.forLanguageTag("pt-BR"))
                .getFromLocation(coordenada.latitude, coordenada.longitude, 1)
                ?.firstOrNull()
                ?: return@withContext null

            localizacaoDoEndereco(endereco, coordenada)
        }.getOrNull()
    }

private fun localizacaoDoEndereco(endereco: Address, coordenada: Location): LocalizacaoUsuario? {
    val cidade = (endereco.locality ?: endereco.subAdminArea).orEmpty().trim()
    val estado = endereco.adminArea.orEmpty().trim()
    if (cidade.isEmpty() || estado.isEmpty()) return null

    return LocalizacaoUsuario(
        cidade = cidade,
        estado = estado,
        latitude = coordenada.latitude,
        longitude = coordenada.longitude,
    )
}
