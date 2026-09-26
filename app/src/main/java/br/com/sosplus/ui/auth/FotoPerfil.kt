package br.com.sosplus.ui.auth

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.min

data class FotoPerfilProcessada(
    val conteudo: ByteArray,
    val mimeType: String = "image/jpeg",
)

private const val TAMANHO_FOTO_PERFIL = 512
private const val QUALIDADE_JPEG_PERFIL = 84

/** Decodifica, recorta e comprime a foto fora da thread principal. */
suspend fun processarFotoPerfil(context: Context, uri: Uri): FotoPerfilProcessada =
    withContext(Dispatchers.Default) {
        val bitmap = decodificarImagem(context, uri)
            ?: throw IllegalArgumentException("Não foi possível abrir essa imagem.")
        val lado = min(bitmap.width, bitmap.height)
        val recortada = Bitmap.createBitmap(
            bitmap,
            (bitmap.width - lado) / 2,
            (bitmap.height - lado) / 2,
            lado,
            lado,
        )
        val final = if (lado > TAMANHO_FOTO_PERFIL) {
            Bitmap.createScaledBitmap(recortada, TAMANHO_FOTO_PERFIL, TAMANHO_FOTO_PERFIL, true)
        } else {
            recortada
        }

        try {
            val output = ByteArrayOutputStream()
            if (!final.compress(Bitmap.CompressFormat.JPEG, QUALIDADE_JPEG_PERFIL, output)) {
                throw IllegalArgumentException("Não foi possível preparar essa imagem.")
            }
            val bytes = output.toByteArray()
            if (bytes.isEmpty() || bytes.size > 1024 * 1024) {
                throw IllegalArgumentException("Escolha uma imagem menor.")
            }
            FotoPerfilProcessada(bytes)
        } finally {
            if (final !== recortada) final.recycle()
            if (recortada !== bitmap) recortada.recycle()
            bitmap.recycle()
        }
    }

private fun decodificarImagem(context: Context, uri: Uri): Bitmap? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            val maiorLado = max(info.size.width, info.size.height)
            var amostra = 1
            while (maiorLado / amostra > TAMANHO_FOTO_PERFIL * 2) amostra *= 2
            decoder.setTargetSampleSize(amostra)
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    } else {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
        var amostra = 1
        val maiorLado = max(options.outWidth, options.outHeight)
        while (maiorLado / amostra > TAMANHO_FOTO_PERFIL * 2) amostra *= 2
        options.inJustDecodeBounds = false
        options.inSampleSize = amostra
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
    }
}
