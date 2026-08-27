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
    val etiqueta: String,
    val progresso: Int,
    val corInicial: Color,
    val corFinal: Color,
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

private enum class AreaDoador(
    val icone: String,
    val rotulo: String,
) {
    Inicio("⌂", "Início"),
    Mapa("⌖", "Mapa"),
    Carteira("▣", "Carteira"),
    Perfil("◉", "Perfil"),
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
                detalhe = "Precisamos manter a alimentação dos resgatados neste mês.",
                simbolo = "🐾",
                etiqueta = "CAMPANHA URGENTE",
                progresso = 68,
                corInicial = Color(0xFF553628),
                corFinal = Color(0xFFD69D4A),
            ),
            CampanhaLocal(
                organizacao = "Projeto Novo Amanhã",
                titulo = "Material escolar para 50 crianças",
                detalhe = "Ajude estudantes da região a começarem o período com o essencial.",
                simbolo = "📚",
                etiqueta = "EDUCAÇÃO",
                progresso = 42,
                corInicial = Color(0xFF1A5D79),
                corFinal = Color(0xFF5547A1),
            ),
            CampanhaLocal(
                organizacao = "Casa do Bem",
                titulo = "50 cestas básicas",
                detalhe = "Arrecadação para famílias atendidas pela ONG neste mês.",
                simbolo = "♡",
                etiqueta = "ALIMENTAÇÃO",
                progresso = 64,
                corInicial = Color(0xFF7A3E5B),
                corFinal = Color(0xFFC87485),
            ),
        )
    }
    var retorno by remember { mutableStateOf<String?>(null) }
    var areaSelecionada by rememberSaveable { mutableStateOf(AreaDoador.Inicio) }

    TelaInicioDoador(
        campanhas = campanhas,
        retorno = retorno,
        areaSelecionada = areaSelecionada,
        aoAjudar = { campanha ->
            retorno = "Você escolheu ajudar: ${campanha.titulo}."
        },
        aoSelecionarArea = { areaSelecionada = it },
        aoSair = aoSair,
        modifier = modifier,
    )
}

@Composable
private fun TelaInicioDoador(
    campanhas: List<CampanhaLocal>,
    retorno: String?,
    areaSelecionada: AreaDoador,
    aoAjudar: (CampanhaLocal) -> Unit,
    aoSelecionarArea: (AreaDoador) -> Unit,
    aoSair: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EstruturaInicioDoador(
        areaSelecionada = areaSelecionada,
        aoSelecionarArea = aoSelecionarArea,
        aoSair = aoSair,
        modifier = modifier,
    ) {
        when (areaSelecionada) {
            AreaDoador.Inicio -> FeedDoador(campanhas, retorno, aoAjudar)
            AreaDoador.Mapa -> ConteudoMapa()
            AreaDoador.Carteira -> ConteudoVazio(
                simbolo = "▣",
                titulo = "Minha carteira",
                descricao = "Acompanhe as contribuições e o impacto que você já gerou.",
            )
            AreaDoador.Perfil -> ConteudoPerfil(aoSair)
        }
    }
}

@Composable
private fun FeedDoador(
    campanhas: List<CampanhaLocal>,
    retorno: String?,
    aoAjudar: (CampanhaLocal) -> Unit,
) {
    Text("CAMPANHAS PERTO DE VOCÊ", color = RoxoClaroInicio, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.1.sp)
    Text(
        text = "Faça a diferença\nperto de você",
        color = TextoInicio,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 34.sp,
        modifier = Modifier.padding(top = 7.dp),
    )
    Text(
        text = "Conheça as ONGs que estão mobilizando a comunidade.",
        color = TextoSecundarioInicio,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = 8.dp),
    )
    campanhas.forEach { campanha ->
        CartaoCampanhaFeed(campanha, { aoAjudar(campanha) }, Modifier.padding(top = 14.dp))
    }
    if (retorno != null) {
        Text(retorno, color = CorSucessoInicio, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 14.dp))
    }
}

@Composable
private fun CartaoCampanhaFeed(
    campanha: CampanhaLocal,
    aoAjudar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieInicio),
        border = BorderStroke(1.dp, BordaInicio),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Brush.linearGradient(listOf(campanha.corInicial, campanha.corFinal))),
            ) {
                Text(
                    text = campanha.etiqueta,
                    color = TextoInicio,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(15.dp)
                        .background(FundoInicio.copy(alpha = 0.35f), RoundedCornerShape(30.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                )
                Text(campanha.simbolo, fontSize = 48.sp, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 30.dp))
                Text(
                    text = campanha.titulo,
                    color = TextoInicio,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 25.sp,
                    modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth(0.72f).padding(15.dp),
                )
            }
            Column(modifier = Modifier.padding(15.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(31.dp).background(RoxoInicio.copy(alpha = 0.26f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center,
                    ) { Text(campanha.simbolo, fontSize = 15.sp) }
                    Column(modifier = Modifier.padding(start = 9.dp)) {
                        Text(campanha.organizacao, color = TextoInicio, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Text("Há pouco tempo · sua cidade", color = TextoSecundarioInicio, style = MaterialTheme.typography.labelSmall)
                    }
                }
                Text(
                    text = campanha.detalhe,
                    color = TextoSecundarioInicio,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 12.dp),
                )
                Row(modifier = Modifier.padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Meta arrecadada", color = TextoSecundarioInicio, style = MaterialTheme.typography.labelSmall)
                            Text("${campanha.progresso}%", color = RoxoClaroInicio, style = MaterialTheme.typography.labelSmall)
                        }
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(top = 5.dp).height(6.dp)
                                .background(BordaInicio, RoundedCornerShape(10.dp)),
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth(campanha.progresso / 100f).height(6.dp)
                                    .background(RoxoClaroInicio, RoundedCornerShape(10.dp)),
                            )
                        }
                    }
                    Button(
                        onClick = aoAjudar,
                        modifier = Modifier.padding(start = 13.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoxoInicio),
                    ) { Text("Ajudar", style = MaterialTheme.typography.labelMedium) }
                }
            }
        }
    }
}

@Composable
private fun ConteudoMapa() {
    TituloAreaDoador("ONGs no mapa", "Explore instituições e campanhas próximas de você.")
    Box(
        modifier = Modifier.fillMaxWidth().height(300.dp).padding(top = 18.dp)
            .background(Brush.linearGradient(listOf(Color(0xFF173651), Color(0xFF285749))), RoundedCornerShape(22.dp)),
    ) {
        Text("⌖", color = TextoInicio.copy(alpha = 0.4f), fontSize = 42.sp, modifier = Modifier.align(Alignment.Center))
        PinoMapa("🐾", Modifier.align(Alignment.TopStart).padding(start = 58.dp, top = 72.dp))
        PinoMapa("♥", Modifier.align(Alignment.Center).padding(start = 72.dp, top = 40.dp))
        PinoMapa("📚", Modifier.align(Alignment.BottomEnd).padding(end = 56.dp, bottom = 63.dp))
    }
}

@Composable
private fun ConteudoPerfil(aoSair: () -> Unit) {
    ConteudoVazio("◉", "Meu perfil", "Gerencie seus dados e veja as ONGs que você apoia.")
    OutlinedButton(
        onClick = aoSair,
        modifier = Modifier.fillMaxWidth().padding(top = 18.dp).height(48.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, BordaInicio),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextoSecundarioInicio),
    ) { Text("Sair da conta") }
}

@Composable
private fun ConteudoVazio(simbolo: String, titulo: String, descricao: String) {
    TituloAreaDoador(titulo, descricao)
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 22.dp),
        color = SuperficieInicio,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, BordaInicio),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(28.dp)) {
            Text(simbolo, color = RoxoClaroInicio, fontSize = 39.sp)
            Text("Em breve", color = TextoInicio, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 12.dp))
            Text("Este espaço será preenchido conforme você usar o SOS+.", color = TextoSecundarioInicio, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 7.dp))
        }
    }
}

@Composable
private fun TituloAreaDoador(titulo: String, descricao: String) {
    Text(titulo, color = TextoInicio, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
    Text(descricao, color = TextoSecundarioInicio, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
}

@Composable
private fun PinoMapa(simbolo: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(38.dp).background(RoxoInicio, RoundedCornerShape(19.dp)),
        contentAlignment = Alignment.Center,
    ) { Text(simbolo, fontSize = 16.sp) }
}

@Composable
private fun EstruturaInicioDoador(
    areaSelecionada: AreaDoador,
    aoSelecionarArea: (AreaDoador) -> Unit,
    aoSair: () -> Unit,
    modifier: Modifier = Modifier,
    conteudo: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize().background(
            Brush.radialGradient(colors = listOf(RoxoInicio.copy(alpha = 0.19f), FundoInicio), radius = 900f),
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 20.dp).padding(bottom = 104.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                LogoInicio()
                Text("Nova Friburgo, RJ", color = TextoSecundarioInicio, style = MaterialTheme.typography.labelSmall)
            }
            Spacer(modifier = Modifier.height(30.dp))
            conteudo()
        }
        NavegacaoDoador(
            areaSelecionada,
            aoSelecionarArea,
            Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
        )
    }
}

@Composable
private fun NavegacaoDoador(
    areaSelecionada: AreaDoador,
    aoSelecionarArea: (AreaDoador) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SuperficieInicio.copy(alpha = 0.97f),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, BordaInicio),
    ) {
        Row(modifier = Modifier.padding(7.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            AreaDoador.entries.forEach { area ->
                val selecionada = area == areaSelecionada
                TextButton(
                    onClick = { aoSelecionarArea(area) },
                    modifier = Modifier.weight(1f).height(58.dp)
                        .background(if (selecionada) RoxoInicio.copy(alpha = 0.34f) else Color.Transparent, RoundedCornerShape(20.dp)),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(area.icone, color = if (selecionada) TextoInicio else TextoSecundarioInicio, fontSize = 19.sp)
                        Text(area.rotulo, color = if (selecionada) TextoInicio else TextoSecundarioInicio, style = MaterialTheme.typography.labelSmall)
                    }
                }
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
