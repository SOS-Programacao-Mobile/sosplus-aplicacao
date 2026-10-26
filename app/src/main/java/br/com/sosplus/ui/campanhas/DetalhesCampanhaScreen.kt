package br.com.sosplus.ui.campanhas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.sosplus.ui.theme.SosTheme
import java.util.Locale

private val FundoDetalhes = Color(0xFF090E1B)
private val SuperficieDetalhes = Color(0xFF11182A)
private val BordaDetalhes = Color(0xFF303950)
private val RoxoDetalhes = Color(0xFF803DF1)
private val RoxoClaroDetalhes = Color(0xFF9C64FF)
private val TextoDetalhes = Color(0xFFF8F7FF)
private val TextoSecundarioDetalhes = Color(0xFFAAA8BD)
private val CorSucessoDetalhes = Color(0xFF62DFA4)
private val CorAlertaDetalhes = Color(0xFFFFC168)
private val CorEncerradaDetalhes = Color(0xFF7C8299)

/**
 * Rota com estado da tela de detalhes de campanha (CU-ID-detalhes-campanha).
 *
 * Depende da listagem de campanhas (RotaInicioDoador) para receber a
 * [Campanha] selecionada. A doação em si ainda não integra com a API —
 * o valor arrecadado é apenas atualizado localmente, seguindo o mesmo
 * padrão "preparado para futura integração" usado no restante do app.
 */
@Composable
fun RotaDetalhesCampanha(
    campanha: Campanha,
    aoVoltar: () -> Unit,
    aoAbrirPerfilOng: (Campanha) -> Unit,
    modifier: Modifier = Modifier,
) {
    var arrecadadoAtual by remember(campanha.id) { mutableStateOf(campanha.arrecadado) }
    var mostrarFormularioDoacao by rememberSaveable(campanha.id) { mutableStateOf(false) }
    var valorDoacao by rememberSaveable(campanha.id) { mutableStateOf("") }
    var retorno by remember(campanha.id) { mutableStateOf<String?>(null) }

    TelaDetalhesCampanha(
        campanha = campanha.copy(arrecadado = arrecadadoAtual),
        mostrarFormularioDoacao = mostrarFormularioDoacao,
        valorDoacao = valorDoacao,
        retorno = retorno,
        aoAlterarValorDoacao = {
            valorDoacao = it
            retorno = null
        },
        aoIniciarDoacao = {
            mostrarFormularioDoacao = true
            retorno = null
        },
        aoCancelarDoacao = {
            mostrarFormularioDoacao = false
            valorDoacao = ""
            retorno = null
        },
        aoConfirmarDoacao = {
            when (val resultado = avaliarDoacao(campanha.copy(arrecadado = arrecadadoAtual), valorDoacao)) {
                is ResultadoDoacao.Erro -> retorno = resultado.mensagem
                is ResultadoDoacao.Sucesso -> {
                    arrecadadoAtual = resultado.arrecadadoAtualizado
                    mostrarFormularioDoacao = false
                    valorDoacao = ""
                    retorno = "Obrigado! Sua doação de ${formatarMoeda(resultado.valor)} foi registrada."
                }
            }
        },
        aoAbrirPerfilOng = { aoAbrirPerfilOng(campanha) },
        aoVoltar = aoVoltar,
        modifier = modifier,
    )
}

@Composable
private fun TelaDetalhesCampanha(
    campanha: Campanha,
    mostrarFormularioDoacao: Boolean,
    valorDoacao: String,
    retorno: String?,
    aoAlterarValorDoacao: (String) -> Unit,
    aoIniciarDoacao: () -> Unit,
    aoCancelarDoacao: () -> Unit,
    aoConfirmarDoacao: () -> Unit,
    aoAbrirPerfilOng: () -> Unit,
    aoVoltar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(RoxoDetalhes.copy(alpha = 0.17f), FundoDetalhes),
                    radius = 900f,
                ),
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 20.dp),
    ) {
        TextButton(
            onClick = aoVoltar,
            modifier = Modifier.heightIn(min = 44.dp),
        ) {
            Text(text = "← Voltar", color = TextoSecundarioDetalhes)
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(RoxoDetalhes.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = campanha.simbolo, fontSize = 26.sp)
            }
            Column(modifier = Modifier.padding(start = 14.dp)) {
                // Botão/caminho para acessar o perfil da ONG.
                Text(
                    text = "${campanha.ongNome}  ›",
                    color = RoxoClaroDetalhes,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .clickable(onClick = aoAbrirPerfilOng)
                        .heightIn(min = 30.dp),
                )
                Text(
                    text = campanha.titulo,
                    color = TextoDetalhes,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        RotuloStatus(
            status = campanha.status,
            modifier = Modifier.padding(top = 16.dp),
        )

        Text(
            text = campanha.descricao,
            color = TextoSecundarioDetalhes,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 14.dp),
        )

        CartaoProgresso(
            campanha = campanha,
            modifier = Modifier.padding(top = 20.dp),
        )

        Row(
            modifier = Modifier.padding(top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CartaoData(
                rotulo = "Data inicial",
                valor = campanha.dataInicioTexto,
                modifier = Modifier.weight(1f),
            )
            CartaoData(
                rotulo = "Data final",
                valor = campanha.dataFimTexto,
                modifier = Modifier.weight(1f),
            )
        }

        if (mostrarFormularioDoacao) {
            FormularioDoacao(
                valor = valorDoacao,
                aoAlterarValor = aoAlterarValorDoacao,
                aoConfirmar = aoConfirmarDoacao,
                aoCancelar = aoCancelarDoacao,
                modifier = Modifier.padding(top = 18.dp),
            )
        } else {
            // Botão para realizar uma doação.
            Button(
                onClick = aoIniciarDoacao,
                enabled = campanha.status == StatusCampanha.ATIVA,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoxoDetalhes),
            ) {
                Text(
                    text = if (campanha.status == StatusCampanha.ATIVA) {
                        "Realizar doação"
                    } else {
                        "Campanha indisponível para doação"
                    },
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        if (retorno != null) {
            Text(
                text = retorno,
                color = if (retorno.startsWith("Obrigado")) CorSucessoDetalhes else CorAlertaDetalhes,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 14.dp),
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun RotuloStatus(status: StatusCampanha, modifier: Modifier = Modifier) {
    val cor = when (status) {
        StatusCampanha.ATIVA -> CorSucessoDetalhes
        StatusCampanha.RASCUNHO -> CorAlertaDetalhes
        StatusCampanha.ENCERRADA -> CorEncerradaDetalhes
        StatusCampanha.CANCELADA -> Color(0xFFFF8E9B)
    }
    Box(
        modifier = modifier
            .background(cor.copy(alpha = 0.16f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = "Status da campanha: ${status.rotulo}",
            color = cor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun CartaoProgresso(campanha: Campanha, modifier: Modifier = Modifier) {
    val progresso = if (campanha.metaValor > 0) {
        (campanha.arrecadado / campanha.metaValor).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieDetalhes),
        border = BorderStroke(1.dp, BordaDetalhes),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = "Arrecadado",
                        color = TextoSecundarioDetalhes,
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Text(
                        text = formatarMoeda(campanha.arrecadado),
                        color = RoxoClaroDetalhes,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Meta",
                        color = TextoSecundarioDetalhes,
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Text(
                        text = formatarMoeda(campanha.metaValor),
                        color = TextoDetalhes,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            LinearProgressIndicator(
                progress = { progresso },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(8.dp),
                color = RoxoDetalhes,
                trackColor = BordaDetalhes,
            )
        }
    }
}

@Composable
private fun CartaoData(
    rotulo: String,
    valor: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieDetalhes),
        border = BorderStroke(1.dp, BordaDetalhes),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = rotulo, color = TextoSecundarioDetalhes, style = MaterialTheme.typography.labelSmall)
            Text(
                text = valor,
                color = TextoDetalhes,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun FormularioDoacao(
    valor: String,
    aoAlterarValor: (String) -> Unit,
    aoConfirmar: () -> Unit,
    aoCancelar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SuperficieDetalhes,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BordaDetalhes),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Quanto você quer doar?",
                color = TextoDetalhes,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedTextField(
                value = valor,
                onValueChange = aoAlterarValor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                label = { Text("Valor (R$)") },
                singleLine = true,
                shape = RoundedCornerShape(13.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextoDetalhes,
                    unfocusedTextColor = TextoDetalhes,
                    focusedContainerColor = FundoDetalhes,
                    unfocusedContainerColor = FundoDetalhes,
                    focusedBorderColor = RoxoClaroDetalhes,
                    unfocusedBorderColor = BordaDetalhes,
                    focusedLabelColor = RoxoClaroDetalhes,
                    unfocusedLabelColor = TextoSecundarioDetalhes,
                    cursorColor = RoxoClaroDetalhes,
                ),
            )
            Row(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                TextButton(
                    onClick = aoCancelar,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Cancelar", color = TextoSecundarioDetalhes)
                }
                Button(
                    onClick = aoConfirmar,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoxoDetalhes),
                ) {
                    Text("Confirmar doação")
                }
            }
        }
    }
}

private fun formatarMoeda(valor: Double): String {
    return "R$ " + String.format(Locale("pt", "BR"), "%,.2f", valor)
}

private val campanhaPreview = Campanha(
    id = 1L,
    ongId = 101L,
    ongNome = "Amigos dos Animais",
    ongDescricao = "ONG dedicada ao resgate e cuidado de animais em situação de abandono.",
    titulo = "Ração para 80 animais",
    descricao = "Estamos arrecadando ração e itens de higiene para os 80 animais " +
        "que estão sob nossos cuidados neste momento.",
    simbolo = "🐾",
    metaValor = 8000.0,
    arrecadado = 5440.0,
    dataInicioTexto = "01/09/2026",
    dataFimTexto = "30/09/2026",
    status = StatusCampanha.ATIVA,
)

@Preview(showBackground = true, name = "Campanha ativa")
@Composable
private fun PreviewDetalhesCampanhaAtiva() {
    SosTheme(darkTheme = true, dynamicColor = false) {
        RotaDetalhesCampanha(
            campanha = campanhaPreview,
            aoVoltar = {},
            aoAbrirPerfilOng = {},
        )
    }
}

@Preview(showBackground = true, name = "Formulário de doação aberto")
@Composable
private fun PreviewDetalhesCampanhaFormularioDoacao() {
    SosTheme(darkTheme = true, dynamicColor = false) {
        TelaDetalhesCampanha(
            campanha = campanhaPreview,
            mostrarFormularioDoacao = true,
            valorDoacao = "50",
            retorno = null,
            aoAlterarValorDoacao = {},
            aoIniciarDoacao = {},
            aoCancelarDoacao = {},
            aoConfirmarDoacao = {},
            aoAbrirPerfilOng = {},
            aoVoltar = {},
        )
    }
}

@Preview(showBackground = true, name = "Campanha encerrada")
@Composable
private fun PreviewDetalhesCampanhaEncerrada() {
    SosTheme(darkTheme = true, dynamicColor = false) {
        RotaDetalhesCampanha(
            campanha = campanhaPreview.copy(
                status = StatusCampanha.ENCERRADA,
                arrecadado = campanhaPreview.metaValor,
            ),
            aoVoltar = {},
            aoAbrirPerfilOng = {},
        )
    }
}

/**
 * Resultado da validação de uma tentativa de doação, extraído da árvore de
 * Compose para poder ser testado como função pura (ver
 * DetalhesCampanhaValidationTest).
 */
internal sealed class ResultadoDoacao {
    data class Sucesso(val valor: Double, val arrecadadoAtualizado: Double) : ResultadoDoacao()
    data class Erro(val mensagem: String) : ResultadoDoacao()
}

internal fun avaliarDoacao(campanha: Campanha, valorTexto: String): ResultadoDoacao {
    val valor = valorTexto.trim().replace(",", ".").toDoubleOrNull()
    return when {
        campanha.status != StatusCampanha.ATIVA ->
            ResultadoDoacao.Erro("Esta campanha não está aceitando doações no momento.")

        valor == null || valor <= 0.0 ->
            ResultadoDoacao.Erro("Informe um valor de doação válido.")

        else ->
            ResultadoDoacao.Sucesso(valor = valor, arrecadadoAtualizado = campanha.arrecadado + valor)
    }
}
