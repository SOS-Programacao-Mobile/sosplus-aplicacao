package br.com.sosplus.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val FundoInicio = Color(0xFF090E1B)
private val SuperficieInicio = Color(0xFF11182A)
private val BordaInicio = Color(0xFF303950)
private val RoxoInicio = Color(0xFF803DF1)
private val RoxoClaroInicio = Color(0xFF9C64FF)
private val TextoInicio = Color(0xFFF8F7FF)
private val TextoSecundarioInicio = Color(0xFFAAA8BD)
private val CorSucessoInicio = Color(0xFF62DFA4)

private data class CampanhaLocal(
    val organizacao: String,
    val titulo: String,
    val detalhe: String,
    val simbolo: String,
)

private data class PublicacaoOng(
    val tipo: TipoPublicacao,
    val titulo: String,
    val descricao: String,
)

private enum class TipoPublicacao(val rotulo: String) {
    Campanha("Campanha"),
    Necessidade("Necessidade"),
}

@Composable
fun RotaInicioDoador(
    aoSair: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val campanhas = remember {
        listOf(
            CampanhaLocal(
                organizacao = "Amigos dos Animais",
                titulo = "Ração para 80 animais",
                detalhe = "68% da meta alcançada",
                simbolo = "🐾",
            ),
            CampanhaLocal(
                organizacao = "Projeto Novo Amanhã",
                titulo = "Material escolar",
                detalhe = "Campanha termina em 12 dias",
                simbolo = "📚",
            ),
            CampanhaLocal(
                organizacao = "Casa do Bem",
                titulo = "50 cestas básicas",
                detalhe = "32 famílias já atendidas",
                simbolo = "♡",
            ),
        )
    }
    var retorno by remember { mutableStateOf<String?>(null) }

    TelaInicioDoador(
        campanhas = campanhas,
        retorno = retorno,
        aoAjudar = { campanha ->
            retorno = "Você escolheu ajudar: ${campanha.titulo}."
        },
        aoSair = aoSair,
        modifier = modifier,
    )
}

@Composable
private fun TelaInicioDoador(
    campanhas: List<CampanhaLocal>,
    retorno: String?,
    aoAjudar: (CampanhaLocal) -> Unit,
    aoSair: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EstruturaInicio(
        aoSair = aoSair,
        modifier = modifier,
    ) {
        Text(
            text = "OLÁ, DOADOR",
            color = RoxoClaroInicio,
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 1.1.sp,
        )
        Text(
            text = "Faça a diferença\nperto de você",
            color = TextoInicio,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 34.sp,
            modifier = Modifier.padding(top = 7.dp),
        )
        Text(
            text = "●  ONGs em sua cidade",
            color = TextoSecundarioInicio,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 23.dp, bottom = 5.dp),
        )

        campanhas.forEach { campanha ->
            CartaoCampanha(
                campanha = campanha,
                aoAjudar = { aoAjudar(campanha) },
                modifier = Modifier.padding(top = 11.dp),
            )
        }

        if (retorno != null) {
            Text(
                text = retorno,
                color = CorSucessoInicio,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 14.dp),
            )
        }

        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RoxoInicio),
        ) {
            Text("Ver todas as ONGs", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun CartaoCampanha(
    campanha: CampanhaLocal,
    aoAjudar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieInicio),
        border = BorderStroke(1.dp, BordaInicio),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(RoxoInicio.copy(alpha = 0.2f), RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = campanha.simbolo, fontSize = 20.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = campanha.organizacao,
                    color = RoxoClaroInicio,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = campanha.titulo,
                    color = TextoInicio,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 3.dp),
                )
                Text(
                    text = campanha.detalhe,
                    color = TextoSecundarioInicio,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
            Button(
                onClick = aoAjudar,
                modifier = Modifier.heightIn(min = 44.dp),
                shape = RoundedCornerShape(11.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoxoInicio),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
            ) {
                Text("Ajudar", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun RotaInicioOng(
    aoSair: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Conteúdo temporário em memória, preparado para futura integração com a API.
    val publicacoes = remember {
        mutableStateListOf(
            PublicacaoOng(
                tipo = TipoPublicacao.Necessidade,
                titulo = "50 cestas básicas",
                descricao = "Arrecadação para famílias atendidas neste mês.",
            ),
        )
    }
    var nomeTipoEdicao by rememberSaveable { mutableStateOf<String?>(null) }
    var titulo by rememberSaveable { mutableStateOf("") }
    var descricao by rememberSaveable { mutableStateOf("") }
    var retorno by remember { mutableStateOf<String?>(null) }
    val tipoEdicao = nomeTipoEdicao?.let(TipoPublicacao::valueOf)

    TelaInicioOng(
        publicacoes = publicacoes,
        tipoEdicao = tipoEdicao,
        titulo = titulo,
        descricao = descricao,
        retorno = retorno,
        aoAlterarTitulo = {
            titulo = it
            retorno = null
        },
        aoAlterarDescricao = {
            descricao = it
            retorno = null
        },
        aoAbrirEditor = {
            nomeTipoEdicao = it.name
            titulo = ""
            descricao = ""
            retorno = null
        },
        aoFecharEditor = {
            nomeTipoEdicao = null
            retorno = null
        },
        aoPublicar = {
            when {
                titulo.isBlank() || descricao.isBlank() -> {
                    retorno = "Preencha o título e a descrição."
                }

                tipoEdicao != null -> {
                    publicacoes.add(
                        0,
                        PublicacaoOng(
                            tipo = tipoEdicao,
                            titulo = titulo.trim(),
                            descricao = descricao.trim(),
                        ),
                    )
                    nomeTipoEdicao = null
                    titulo = ""
                    descricao = ""
                    retorno = "Publicação adicionada com sucesso."
                }
            }
        },
        aoSair = aoSair,
        modifier = modifier,
    )
}

@Composable
private fun TelaInicioOng(
    publicacoes: List<PublicacaoOng>,
    tipoEdicao: TipoPublicacao?,
    titulo: String,
    descricao: String,
    retorno: String?,
    aoAlterarTitulo: (String) -> Unit,
    aoAlterarDescricao: (String) -> Unit,
    aoAbrirEditor: (TipoPublicacao) -> Unit,
    aoFecharEditor: () -> Unit,
    aoPublicar: () -> Unit,
    aoSair: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EstruturaInicio(
        aoSair = aoSair,
        modifier = modifier,
    ) {
        Text(
            text = "PAINEL DA ONG",
            color = RoxoClaroInicio,
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 1.1.sp,
        )
        Text(
            text = "Olá, mãos que ajudam",
            color = TextoInicio,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 7.dp),
        )
        Text(
            text = "Divulgue necessidades e acompanhe suas campanhas.",
            color = TextoSecundarioInicio,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 22.dp),
        )

        Button(
            onClick = { aoAbrirEditor(TipoPublicacao.Campanha) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RoxoInicio),
        ) {
            Text("＋  Nova campanha", fontWeight = FontWeight.SemiBold)
        }
        OutlinedButton(
            onClick = { aoAbrirEditor(TipoPublicacao.Necessidade) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, RoxoClaroInicio),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = RoxoClaroInicio),
        ) {
            Text("Cadastrar necessidade", fontWeight = FontWeight.SemiBold)
        }

        if (tipoEdicao != null) {
            EditorPublicacao(
                tipo = tipoEdicao,
                titulo = titulo,
                descricao = descricao,
                aoAlterarTitulo = aoAlterarTitulo,
                aoAlterarDescricao = aoAlterarDescricao,
                aoPublicar = aoPublicar,
                aoCancelar = aoFecharEditor,
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        if (retorno != null) {
            Text(
                text = retorno,
                color = if (retorno.contains("sucesso")) CorSucessoInicio else Color(0xFFFF8E9B),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 13.dp),
            )
        }

        Row(
            modifier = Modifier.padding(top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CartaoResumo(
                rotulo = "Campanhas ativas",
                valor = publicacoes.count { it.tipo == TipoPublicacao.Campanha }.toString(),
                modifier = Modifier.weight(1f),
            )
            CartaoResumo(
                rotulo = "Publicações",
                valor = publicacoes.size.toString(),
                modifier = Modifier.weight(1f),
            )
        }

        Text(
            text = "Publicações recentes",
            color = TextoInicio,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 25.dp, bottom = 3.dp),
        )
        publicacoes.forEach { publicacao ->
            CartaoPublicacao(
                publicacao = publicacao,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

@Composable
private fun EditorPublicacao(
    tipo: TipoPublicacao,
    titulo: String,
    descricao: String,
    aoAlterarTitulo: (String) -> Unit,
    aoAlterarDescricao: (String) -> Unit,
    aoPublicar: () -> Unit,
    aoCancelar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SuperficieInicio,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BordaInicio),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (tipo == TipoPublicacao.Campanha) "Nova campanha" else "Nova necessidade",
                color = TextoInicio,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedTextField(
                value = titulo,
                onValueChange = aoAlterarTitulo,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                label = { Text("Título") },
                singleLine = true,
                shape = RoundedCornerShape(13.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                colors = coresCampoInicio(),
            )
            OutlinedTextField(
                value = descricao,
                onValueChange = aoAlterarDescricao,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                label = { Text("Descrição") },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(13.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                colors = coresCampoInicio(),
            )
            Row(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                TextButton(
                    onClick = aoCancelar,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Cancelar", color = TextoSecundarioInicio)
                }
                Button(
                    onClick = aoPublicar,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoxoInicio),
                ) {
                    Text("Publicar")
                }
            }
        }
    }
}

@Composable
private fun CartaoResumo(
    rotulo: String,
    valor: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieInicio),
        border = BorderStroke(1.dp, BordaInicio),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = rotulo, color = TextoSecundarioInicio, style = MaterialTheme.typography.labelSmall)
            Text(
                text = valor,
                color = RoxoClaroInicio,
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 7.dp),
            )
        }
    }
}

@Composable
private fun CartaoPublicacao(
    publicacao: PublicacaoOng,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieInicio),
        border = BorderStroke(1.dp, BordaInicio),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = publicacao.tipo.rotulo,
                    color = RoxoClaroInicio,
                    style = MaterialTheme.typography.labelSmall,
                )
                Text(
                    text = publicacao.titulo,
                    color = TextoInicio,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 3.dp),
                )
                Text(
                    text = publicacao.descricao,
                    color = TextoSecundarioInicio,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Text(
                text = "Publicada",
                color = CorSucessoInicio,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun EstruturaInicio(
    aoSair: () -> Unit,
    modifier: Modifier = Modifier,
    conteudo: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(RoxoInicio.copy(alpha = 0.17f), FundoInicio),
                    radius = 900f,
                ),
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            LogoInicio()
            TextButton(
                onClick = aoSair,
                modifier = Modifier.heightIn(min = 44.dp),
            ) {
                Text(text = "Sair", color = TextoSecundarioInicio)
            }
        }
        Spacer(modifier = Modifier.height(34.dp))
        conteudo()
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun LogoInicio(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Box(modifier = Modifier.size(28.dp)) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(width = 28.dp, height = 10.dp)
                    .background(RoxoClaroInicio, RoundedCornerShape(6.dp)),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(width = 10.dp, height = 28.dp)
                    .background(RoxoClaroInicio, RoundedCornerShape(6.dp)),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(7.dp)
                    .background(Color(0xFFFF6478), RoundedCornerShape(50)),
            )
        }
        Text(
            text = "SOS+",
            color = TextoInicio,
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun coresCampoInicio() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextoInicio,
    unfocusedTextColor = TextoInicio,
    focusedContainerColor = FundoInicio,
    unfocusedContainerColor = FundoInicio,
    focusedBorderColor = RoxoClaroInicio,
    unfocusedBorderColor = BordaInicio,
    focusedLabelColor = RoxoClaroInicio,
    unfocusedLabelColor = TextoSecundarioInicio,
    cursorColor = RoxoClaroInicio,
)
