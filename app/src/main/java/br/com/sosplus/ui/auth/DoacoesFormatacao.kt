package br.com.sosplus.ui.auth

import br.com.sosplus.data.DoacaoUsuario
import java.util.Calendar
import java.util.Locale

internal enum class PeriodoDoacao(val rotulo: String) {
    Todas("Todas"),
    UltimosTrintaDias("30 dias"),
    UltimosTresMeses("3 meses"),
    EsteAno("Este ano"),
}

/**
 * Filtra pelo período a partir de [hoje]. As datas da API são AAAA-MM-DD, então
 * comparar os textos equivale a comparar as datas (e evita java.time, que exige API 26).
 */
internal fun filtrarDoacoesPorPeriodo(
    doacoes: List<DoacaoUsuario>,
    periodo: PeriodoDoacao,
    hoje: Calendar = Calendar.getInstance(),
): List<DoacaoUsuario> {
    val inicio = (hoje.clone() as Calendar).apply {
        when (periodo) {
            PeriodoDoacao.Todas -> return doacoes
            PeriodoDoacao.UltimosTrintaDias -> add(Calendar.DAY_OF_MONTH, -30)
            PeriodoDoacao.UltimosTresMeses -> add(Calendar.MONTH, -3)
            PeriodoDoacao.EsteAno -> set(Calendar.DAY_OF_YEAR, 1)
        }
    }
    val limite = String.format(
        Locale.ROOT, "%04d-%02d-%02d",
        inicio.get(Calendar.YEAR), inicio.get(Calendar.MONTH) + 1, inicio.get(Calendar.DAY_OF_MONTH),
    )
    return doacoes.filter { it.data.take(10) >= limite }
}

/** Soma só as confirmadas: pendentes e canceladas não representam dinheiro doado. */
internal fun totalConfirmadoCentavos(doacoes: List<DoacaoUsuario>): Long =
    doacoes.filter { it.status == "CONFIRMADA" }.sumOf { it.valorCentavos }

/** 2500 -> "R$ 25,00"; 123456 -> "R$ 1.234,56". */
internal fun formatarValorDoacao(centavos: Long): String {
    val reais = (centavos / 100).toString().reversed().chunked(3).joinToString(".").reversed()
    return "R$ $reais,${(centavos % 100).toString().padStart(2, '0')}"
}

/** "2026-08-20" -> "20/08/2026"; formatos inesperados são exibidos como vieram. */
internal fun formatarDataDoacao(data: String): String =
    Regex("^(\\d{4})-(\\d{2})-(\\d{2})").find(data)
        ?.destructured?.let { (ano, mes, dia) -> "$dia/$mes/$ano" }
        ?: data
