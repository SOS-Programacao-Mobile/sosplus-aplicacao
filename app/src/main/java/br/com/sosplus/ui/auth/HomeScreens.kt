package br.com.sosplus.ui.auth

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.BackHandler
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import br.com.sosplus.data.UsuarioAutenticado
import br.com.sosplus.data.DadosPessoaisUsuario

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

val FundoInicio = Color(0xFF090E1B)
val SuperficieInicio = Color(0xFF11182A)
val BordaInicio = Color(0xFF303950)
val RoxoInicio = Color(0xFF803DF1)
val RoxoClaroInicio = Color(0xFF9C64FF)
val TextoInicio = Color(0xFFF8F7FF)
val TextoSecundarioInicio = Color(0xFFAAA8BD)
val CorSucessoInicio = Color(0xFF62DFA4)

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

enum class AreaDoador(
    val icone: String,
    val rotulo: String,
) {
    Inicio("⌂", "Início"),
    Mapa("⌖", "Mapa"),
    Carteira("▣", "Carteira"),
    Perfil("◉", "Perfil"),
}

private enum class OpcaoPerfil(
    val simbolo: String,
    val titulo: String,
    val descricao: String,
) {
    ConfigurarPerfil("◉", "Configurar perfil", "Foto e preferências da conta"),
    Pagamentos("\$", "Pagamentos e carteira", "Formas de pagamento e histórico"),
    Informacoes("i", "Informações pessoais", "Dados vinculados à sua conta"),
    Senha("↻", "Redefinir senha", "Atualize sua senha com segurança"),
}

private enum class TelaPerfil { Principal, InformacoesPessoais, RedefinirSenha }

private data class RetornoPerfil(val mensagem: String, val sucesso: Boolean)

@Composable
fun RotaInicioDoador(
    usuario: UsuarioAutenticado,
    fotoPerfil: ByteArray?,
    aoSalvarFotoPerfil: suspend (ByteArray, String) -> Unit,
    aoRedefinirSenha: suspend (String, String, String) -> Unit,
    aoCarregarDadosPessoais: suspend () -> DadosPessoaisUsuario,
    aoSalvarDadosPessoais: suspend (DadosPessoaisUsuario) -> DadosPessoaisUsuario,
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
    var telaPerfil by rememberSaveable { mutableStateOf(TelaPerfil.Principal) }

    BackHandler(enabled = areaSelecionada == AreaDoador.Perfil && telaPerfil != TelaPerfil.Principal) {
        telaPerfil = TelaPerfil.Principal
    }

    TelaInicioDoador(
        usuario = usuario,
        fotoPerfil = fotoPerfil,
        campanhas = campanhas,
        retorno = retorno,
        areaSelecionada = areaSelecionada,
        aoAjudar = { campanha ->
            retorno = "Você escolheu ajudar: ${campanha.titulo}."
        },
        aoSelecionarArea = {
            areaSelecionada = it
            telaPerfil = TelaPerfil.Principal
        },
        aoSalvarFotoPerfil = aoSalvarFotoPerfil,
        aoRedefinirSenha = aoRedefinirSenha,
        aoCarregarDadosPessoais = aoCarregarDadosPessoais,
        aoSalvarDadosPessoais = aoSalvarDadosPessoais,
        telaPerfil = telaPerfil,
        aoAlterarTelaPerfil = { telaPerfil = it },
        aoSair = aoSair,
        modifier = modifier,
    )
}

@Composable
private fun TelaInicioDoador(
    usuario: UsuarioAutenticado,
    fotoPerfil: ByteArray?,
    campanhas: List<CampanhaLocal>,
    retorno: String?,
    areaSelecionada: AreaDoador,
    aoAjudar: (CampanhaLocal) -> Unit,
    aoSelecionarArea: (AreaDoador) -> Unit,
    aoSalvarFotoPerfil: suspend (ByteArray, String) -> Unit,
    aoRedefinirSenha: suspend (String, String, String) -> Unit,
    aoCarregarDadosPessoais: suspend () -> DadosPessoaisUsuario,
    aoSalvarDadosPessoais: suspend (DadosPessoaisUsuario) -> DadosPessoaisUsuario,
    telaPerfil: TelaPerfil,
    aoAlterarTelaPerfil: (TelaPerfil) -> Unit,
    aoSair: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (areaSelecionada == AreaDoador.Mapa) {
        TelaMapaDoador(
            areaSelecionada = areaSelecionada,
            aoSelecionarArea = aoSelecionarArea,
            localizacaoUsuario = usuario.localizacao,
            modifier = modifier,
        )
    } else {
        EstruturaInicioDoador(
            areaSelecionada = areaSelecionada,
            aoSelecionarArea = aoSelecionarArea,
            aoSair = aoSair,
            localizacaoUsuario = usuario.localizacao,
            modifier = modifier,
            exibirCabecalho = areaSelecionada != AreaDoador.Perfil || telaPerfil == TelaPerfil.Principal,
            exibirNavegacao = areaSelecionada != AreaDoador.Perfil || telaPerfil == TelaPerfil.Principal,
        ) {
            when (areaSelecionada) {
                AreaDoador.Inicio -> {
                    Text("Olá, ${usuario.nome}", color = TextoInicio, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    FeedDoador(campanhas, retorno, aoAjudar)
                }
                AreaDoador.Carteira -> ConteudoVazio(
                    simbolo = "▣",
                    titulo = "Minha carteira",
                    descricao = "Acompanhe as contribuições e o impacto que você já gerou.",
                )
                AreaDoador.Perfil -> when (telaPerfil) {
                    TelaPerfil.Principal -> ConteudoPerfil(
                        usuario = usuario,
                        fotoPerfil = fotoPerfil,
                        aoSalvarFotoPerfil = aoSalvarFotoPerfil,
                        aoAbrirCarteira = { aoSelecionarArea(AreaDoador.Carteira) },
                        aoAbrirInformacoes = { aoAlterarTelaPerfil(TelaPerfil.InformacoesPessoais) },
                        aoAbrirRedefinicaoSenha = { aoAlterarTelaPerfil(TelaPerfil.RedefinirSenha) },
                        aoSair = aoSair,
                    )
                    TelaPerfil.InformacoesPessoais -> TelaInformacoesPessoais(
                        usuario = usuario,
                        fotoPerfil = fotoPerfil,
                        aoCarregar = aoCarregarDadosPessoais,
                        aoSalvar = aoSalvarDadosPessoais,
                        aoVoltar = { aoAlterarTelaPerfil(TelaPerfil.Principal) },
                    )
                    TelaPerfil.RedefinirSenha -> TelaRedefinirSenhaPerfil(
                        email = usuario.email,
                        aoRedefinirSenha = aoRedefinirSenha,
                        aoVoltar = { aoAlterarTelaPerfil(TelaPerfil.Principal) },
                    )
                }
                AreaDoador.Mapa -> {}
            }
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
    TituloAreaDoador("ONGs no mapa", "Explore instituições próximas de você.")
    MapaAberto()
}

@Composable
private fun ConteudoPerfil(
    usuario: UsuarioAutenticado,
    fotoPerfil: ByteArray?,
    aoSalvarFotoPerfil: suspend (ByteArray, String) -> Unit,
    aoAbrirCarteira: () -> Unit,
    aoAbrirInformacoes: () -> Unit,
    aoAbrirRedefinicaoSenha: () -> Unit,
    aoSair: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var salvandoFoto by remember { mutableStateOf(false) }
    var retorno by remember { mutableStateOf<RetornoPerfil?>(null) }
    var opcaoAberta by remember { mutableStateOf<OpcaoPerfil?>(null) }
    val bitmap by produceState<Bitmap?>(initialValue = null, fotoPerfil) {
        value = withContext(Dispatchers.Default) {
            fotoPerfil?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
        }
    }
    val iniciais = remember(usuario.nome) {
        usuario.nome.trim().split(Regex("\\s+")).take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
            .ifBlank { "?" }
    }

    val selecionarFoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null && !salvandoFoto) {
            salvandoFoto = true
            retorno = null
            scope.launch {
                try {
                    val foto = processarFotoPerfil(context, uri)
                    aoSalvarFotoPerfil(foto.conteudo, foto.mimeType)
                    retorno = RetornoPerfil("Foto de perfil atualizada.", sucesso = true)
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    retorno = RetornoPerfil(error.message ?: "Não foi possível atualizar a foto.", sucesso = false)
                } finally {
                    salvandoFoto = false
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SuperficieInicio,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, BordaInicio),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier
                        .size(104.dp)
                        .clickable(enabled = !salvandoFoto) {
                            selecionarFoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                    shape = CircleShape,
                    color = RoxoInicio.copy(alpha = 0.24f),
                    border = BorderStroke(2.dp, RoxoClaroInicio.copy(alpha = 0.8f)),
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = requireNotNull(bitmap).asImageBitmap(),
                            contentDescription = "Foto de perfil de ${usuario.nome}",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().background(
                                Brush.linearGradient(listOf(RoxoInicio, Color(0xFF4B4FA0))),
                            ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(iniciais, color = TextoInicio, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                if (salvandoFoto) {
                    Surface(
                        modifier = Modifier.size(104.dp),
                        shape = CircleShape,
                        color = FundoInicio.copy(alpha = 0.72f),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = RoxoClaroInicio,
                                strokeWidth = 3.dp,
                            )
                        }
                    }
                }
            }

            Text(
                text = usuario.nome,
                color = TextoInicio,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 14.dp),
            )
            Text(
                text = usuario.email,
                color = TextoSecundarioInicio,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 3.dp),
            )
            Surface(
                color = CorSucessoInicio.copy(alpha = 0.12f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(top = 10.dp),
            ) {
                Text(
                    text = "Conta de doador",
                    color = CorSucessoInicio,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                )
            }
            OutlinedButton(
                onClick = {
                    selecionarFoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                enabled = !salvandoFoto,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, RoxoClaroInicio.copy(alpha = 0.7f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = RoxoClaroInicio),
                modifier = Modifier.padding(top = 14.dp),
            ) {
                Text(if (fotoPerfil == null) "Adicionar foto" else "Alterar foto")
            }
            Text(
                "A imagem é otimizada antes do envio para evitar lentidão.",
                color = TextoSecundarioInicio,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 7.dp),
            )
            retorno?.let { estado ->
                Text(
                    text = estado.mensagem,
                    color = if (estado.sucesso) CorSucessoInicio else Color(0xFFFF8E9B),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
        }
    }

    Text(
        "CONTA E PREFERÊNCIAS",
        color = RoxoClaroInicio,
        style = MaterialTheme.typography.labelMedium,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 24.dp, bottom = 10.dp),
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SuperficieInicio,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, BordaInicio),
    ) {
        Column {
            OpcaoPerfil.entries.forEachIndexed { indice, opcao ->
                AcaoPerfil(
                    opcao = opcao,
                    onClick = {
                        when (opcao) {
                            OpcaoPerfil.Pagamentos -> aoAbrirCarteira()
                            OpcaoPerfil.Informacoes -> aoAbrirInformacoes()
                            OpcaoPerfil.Senha -> aoAbrirRedefinicaoSenha()
                            else -> opcaoAberta = opcao
                        }
                    },
                )
                if (indice < OpcaoPerfil.entries.lastIndex) {
                    HorizontalDivider(color = BordaInicio.copy(alpha = 0.65f), modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }

    OutlinedButton(
        onClick = aoSair,
        modifier = Modifier.fillMaxWidth().padding(top = 18.dp).height(52.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFFF8E9B).copy(alpha = 0.55f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF8E9B)),
    ) {
        Text("Sair da conta", fontWeight = FontWeight.SemiBold)
    }

    opcaoAberta?.let { opcao ->
        DialogoOpcaoPerfil(usuario, opcao, onDismiss = { opcaoAberta = null })
    }
}

@Composable
private fun AcaoPerfil(opcao: OpcaoPerfil, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(42.dp).background(RoxoInicio.copy(alpha = 0.2f), RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(opcao.simbolo, color = RoxoClaroInicio, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(opcao.titulo, color = TextoInicio, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(
                opcao.descricao,
                color = TextoSecundarioInicio,
                fontSize = 11.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Text("›", color = TextoSecundarioInicio, fontSize = 22.sp)
    }
}

@Composable
private fun DialogoOpcaoPerfil(usuario: UsuarioAutenticado, opcao: OpcaoPerfil, onDismiss: () -> Unit) {
    val descricao = when (opcao) {
        OpcaoPerfil.ConfigurarPerfil -> "Sua foto pode ser alterada diretamente no cartão do perfil. Nome e e-mail estão vinculados à conta autenticada."
        OpcaoPerfil.Informacoes -> buildString {
            append("Nome: ${usuario.nome}\n")
            append("E-mail: ${usuario.email}")
            usuario.localizacao?.let { append("\nLocalização: ${it.descricao}") }
        }
        else -> "Esta configuração estará disponível nesta área."
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SuperficieInicio,
        shape = RoundedCornerShape(22.dp),
        title = { Text(opcao.titulo, color = TextoInicio, fontWeight = FontWeight.Bold) },
        text = { Text(descricao, color = TextoSecundarioInicio, lineHeight = 21.sp) },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Entendi", color = RoxoClaroInicio) }
        },
    )
}

@Composable
private fun TelaRedefinirSenhaPerfil(
    email: String,
    aoRedefinirSenha: suspend (String, String, String) -> Unit,
    aoVoltar: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var senhaAtual by remember { mutableStateOf("") }
    var novaSenha by remember { mutableStateOf("") }
    var confirmacao by remember { mutableStateOf("") }
    var senhaAtualVisivel by rememberSaveable { mutableStateOf(false) }
    var novaSenhaVisivel by rememberSaveable { mutableStateOf(false) }
    var confirmacaoVisivel by rememberSaveable { mutableStateOf(false) }
    var salvando by remember { mutableStateOf(false) }
    var retorno by remember { mutableStateOf<RetornoPerfil?>(null) }

    CabecalhoTelaPerfil("Redefinir senha", aoVoltar)
    Text(
        "Confirme sua identidade e crie uma senha nova com pelo menos 8 caracteres.",
        color = TextoSecundarioInicio,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = 8.dp, bottom = 20.dp),
    )
    OutlinedTextField(
        value = email,
        onValueChange = {},
        readOnly = true,
        singleLine = true,
        label = { Text("E-mail da conta") },
        colors = coresCampoPerfil(),
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(modifier = Modifier.height(12.dp))
    CampoSenhaPerfil("Senha atual", senhaAtual, { senhaAtual = it; retorno = null }, !salvando, senhaAtualVisivel) {
        senhaAtualVisivel = !senhaAtualVisivel
    }
    Spacer(modifier = Modifier.height(12.dp))
    CampoSenhaPerfil("Nova senha", novaSenha, { novaSenha = it; retorno = null }, !salvando, novaSenhaVisivel) {
        novaSenhaVisivel = !novaSenhaVisivel
    }
    Spacer(modifier = Modifier.height(12.dp))
    CampoSenhaPerfil("Confirmar nova senha", confirmacao, { confirmacao = it; retorno = null }, !salvando, confirmacaoVisivel) {
        confirmacaoVisivel = !confirmacaoVisivel
    }
    retorno?.let { estado ->
        MensagemPerfil(estado, Modifier.padding(top = 14.dp))
    }
    Button(
        onClick = {
            val erroLocal = when {
                senhaAtual.isBlank() -> "Informe sua senha atual."
                novaSenha.length < 8 -> "A nova senha deve ter pelo menos 8 caracteres."
                novaSenha != confirmacao -> "A confirmação da nova senha não confere."
                novaSenha == senhaAtual -> "A nova senha deve ser diferente da atual."
                else -> null
            }
            if (erroLocal != null) {
                retorno = RetornoPerfil(erroLocal, false)
            } else {
                salvando = true
                retorno = null
                scope.launch {
                    try {
                        aoRedefinirSenha(senhaAtual, novaSenha, confirmacao)
                        senhaAtual = ""
                        novaSenha = ""
                        confirmacao = ""
                        retorno = RetornoPerfil("Senha atualizada com segurança.", true)
                    } catch (error: CancellationException) {
                        throw error
                    } catch (error: IOException) {
                        retorno = RetornoPerfil(error.message ?: "Não foi possível atualizar a senha.", false)
                    } finally {
                        salvando = false
                    }
                }
            }
        },
        enabled = !salvando,
        colors = ButtonDefaults.buttonColors(containerColor = RoxoInicio),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(54.dp),
    ) {
        if (salvando) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = TextoInicio, strokeWidth = 2.dp)
        else Text("Atualizar senha", fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun TelaInformacoesPessoais(
    usuario: UsuarioAutenticado,
    fotoPerfil: ByteArray?,
    aoCarregar: suspend () -> DadosPessoaisUsuario,
    aoSalvar: suspend (DadosPessoaisUsuario) -> DadosPessoaisUsuario,
    aoVoltar: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    // Estes valores são recarregados da API a cada entrada nesta tela.
    var nome by remember { mutableStateOf("") }
    var sobrenome by remember { mutableStateOf("") }
    var cpf by remember { mutableStateOf("") }
    var dataNascimento by remember { mutableStateOf("") }
    var endereco by remember { mutableStateOf("") }
    var tipoSanguineo by remember { mutableStateOf<String?>(null) }
    var genero by remember { mutableStateOf<String?>(null) }
    var telefone by remember { mutableStateOf("") }
    var carregando by remember { mutableStateOf(true) }
    var salvando by remember { mutableStateOf(false) }
    var retorno by remember { mutableStateOf<RetornoPerfil?>(null) }
    var tentativa by remember { mutableStateOf(0) }

    fun preencher(dados: DadosPessoaisUsuario) {
        nome = dados.nome
        sobrenome = dados.sobrenome.orEmpty()
        cpf = dados.cpf.orEmpty()
        dataNascimento = dataApiParaTela(dados.dataNascimento)
        endereco = dados.endereco.orEmpty()
        tipoSanguineo = dados.tipoSanguineo
        genero = dados.genero
        telefone = dados.telefone.orEmpty()
    }

    LaunchedEffect(tentativa) {
        carregando = true
        retorno = null
        try {
            preencher(aoCarregar())
        } catch (error: CancellationException) {
            throw error
        } catch (error: IOException) {
            retorno = RetornoPerfil(error.message ?: "Não foi possível carregar seus dados.", false)
        } finally {
            carregando = false
        }
    }

    CabecalhoTelaPerfil("Informações pessoais", aoVoltar)
    CabecalhoUsuarioCompacto(usuario.nome, fotoPerfil, Modifier.padding(top = 14.dp, bottom = 18.dp))

    if (carregando) {
        Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = RoxoClaroInicio)
        }
        return
    }
    if (retorno?.sucesso == false && nome.isBlank()) {
        MensagemPerfil(requireNotNull(retorno), Modifier.padding(top = 12.dp))
        OutlinedButton(onClick = { tentativa++ }, modifier = Modifier.fillMaxWidth().padding(top = 14.dp)) {
            Text("Tentar novamente")
        }
        return
    }

    Text(
        "Você pode deixar os campos opcionais em branco. O tipo sanguíneo é dado de saúde e também fica protegido no armazenamento.",
        color = TextoSecundarioInicio,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        modifier = Modifier.fillMaxWidth().background(RoxoInicio.copy(alpha = 0.12f), RoundedCornerShape(14.dp)).padding(14.dp),
    )
    Spacer(modifier = Modifier.height(16.dp))
    CampoTextoPerfil("Nome", nome, { nome = it; retorno = null }, KeyboardType.Text, KeyboardCapitalization.Words, !salvando)
    Spacer(modifier = Modifier.height(12.dp))
    CampoTextoPerfil("Sobrenome", sobrenome, { sobrenome = it; retorno = null }, KeyboardType.Text, KeyboardCapitalization.Words, !salvando)
    Spacer(modifier = Modifier.height(12.dp))
    CampoTextoPerfil("CPF", cpf, { cpf = it.take(14); retorno = null }, KeyboardType.Number, habilitado = !salvando)
    Spacer(modifier = Modifier.height(12.dp))
    CampoTextoPerfil("Data de nascimento", dataNascimento, { dataNascimento = it.take(10); retorno = null }, KeyboardType.Number, habilitado = !salvando, placeholder = "DD/MM/AAAA")
    Spacer(modifier = Modifier.height(12.dp))
    CampoTextoPerfil("Telefone com DDD", telefone, { telefone = it.take(20); retorno = null }, KeyboardType.Phone, habilitado = !salvando)
    Spacer(modifier = Modifier.height(12.dp))
    CampoTextoPerfil("Endereço", endereco, { endereco = it; retorno = null }, KeyboardType.Text, KeyboardCapitalization.Sentences, !salvando, singleLine = false)
    Spacer(modifier = Modifier.height(12.dp))
    CampoSelecaoPerfil(
        rotulo = "Tipo sanguíneo",
        valor = tipoSanguineo,
        opcoes = listOf(null to "Não informar", "A+" to "A+", "A-" to "A-", "B+" to "B+", "B-" to "B-", "AB+" to "AB+", "AB-" to "AB-", "O+" to "O+", "O-" to "O-"),
        habilitado = !salvando,
        aoSelecionar = { tipoSanguineo = it; retorno = null },
    )
    Spacer(modifier = Modifier.height(12.dp))
    CampoSelecaoPerfil(
        rotulo = "Gênero",
        valor = genero,
        opcoes = listOf(null to "Não informar", "MASCULINO" to "Masculino", "FEMININO" to "Feminino", "OUTROS" to "Outros", "PREFIRO_NAO_INFORMAR" to "Prefiro não informar"),
        habilitado = !salvando,
        aoSelecionar = { genero = it; retorno = null },
    )
    retorno?.let { MensagemPerfil(it, Modifier.padding(top = 14.dp)) }
    Button(
        onClick = {
            val dataApi = dataTelaParaApi(dataNascimento)
            val erroLocal = when {
                nome.isBlank() -> "Informe seu nome."
                dataNascimento.isNotBlank() && dataApi == null -> "Informe a data no formato DD/MM/AAAA."
                else -> null
            }
            if (erroLocal != null) {
                retorno = RetornoPerfil(erroLocal, false)
            } else {
                salvando = true
                retorno = null
                scope.launch {
                    try {
                        val atualizados = aoSalvar(DadosPessoaisUsuario(
                            nome = nome.trim(),
                            sobrenome = sobrenome.trim().ifBlank { null },
                            cpf = cpf.trim().ifBlank { null },
                            dataNascimento = dataApi,
                            endereco = endereco.trim().ifBlank { null },
                            tipoSanguineo = tipoSanguineo,
                            genero = genero,
                            telefone = telefone.trim().ifBlank { null },
                        ))
                        preencher(atualizados)
                        retorno = RetornoPerfil("Informações atualizadas.", true)
                    } catch (error: CancellationException) {
                        throw error
                    } catch (error: IOException) {
                        retorno = RetornoPerfil(error.message ?: "Não foi possível salvar seus dados.", false)
                    } finally {
                        salvando = false
                    }
                }
            }
        },
        enabled = !salvando,
        colors = ButtonDefaults.buttonColors(containerColor = RoxoInicio),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(54.dp),
    ) {
        if (salvando) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = TextoInicio, strokeWidth = 2.dp)
        else Text("Salvar alterações", fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CabecalhoTelaPerfil(titulo: String, aoVoltar: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = aoVoltar, contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 0.dp, vertical = 8.dp)) {
            Text("‹ Voltar", color = RoxoClaroInicio, fontWeight = FontWeight.SemiBold)
        }
        Text(titulo, color = TextoInicio, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp))
    }
}

@Composable
private fun CabecalhoUsuarioCompacto(nome: String, fotoPerfil: ByteArray?, modifier: Modifier = Modifier) {
    val bitmap by produceState<Bitmap?>(initialValue = null, fotoPerfil) {
        value = withContext(Dispatchers.Default) { fotoPerfil?.let { BitmapFactory.decodeByteArray(it, 0, it.size) } }
    }
    val iniciais = remember(nome) { nome.trim().split(Regex("\\s+")).take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("").ifBlank { "?" } }
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Surface(modifier = Modifier.size(52.dp), shape = CircleShape, color = RoxoInicio.copy(alpha = 0.25f)) {
            if (bitmap != null) {
                Image(requireNotNull(bitmap).asImageBitmap(), "Foto de perfil", Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(iniciais, color = TextoInicio, fontWeight = FontWeight.Bold) }
            }
        }
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(nome, color = TextoInicio, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("Cadastro pessoal", color = TextoSecundarioInicio, fontSize = 12.sp)
        }
    }
}

@Composable
private fun CampoTextoPerfil(
    rotulo: String,
    valor: String,
    aoAlterar: (String) -> Unit,
    tipoTeclado: KeyboardType,
    capitalizacao: KeyboardCapitalization = KeyboardCapitalization.None,
    habilitado: Boolean,
    placeholder: String? = null,
    singleLine: Boolean = true,
) {
    OutlinedTextField(
        value = valor,
        onValueChange = aoAlterar,
        label = { Text(rotulo) },
        placeholder = placeholder?.let { { Text(it) } },
        enabled = habilitado,
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 2,
        keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado, capitalization = capitalizacao),
        colors = coresCampoPerfil(),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun CampoSelecaoPerfil(
    rotulo: String,
    valor: String?,
    opcoes: List<Pair<String?, String>>,
    habilitado: Boolean,
    aoSelecionar: (String?) -> Unit,
) {
    var aberto by remember { mutableStateOf(false) }
    val texto = opcoes.firstOrNull { it.first == valor }?.second ?: "Não informar"
    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.fillMaxWidth().clickable(enabled = habilitado) { aberto = true },
            color = Color.Transparent,
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, if (aberto) RoxoClaroInicio else BordaInicio),
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Text(rotulo, color = if (aberto) RoxoClaroInicio else TextoSecundarioInicio, fontSize = 11.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(texto, color = TextoInicio, style = MaterialTheme.typography.bodyLarge)
                    Text("⌄", color = TextoSecundarioInicio)
                }
            }
        }
        DropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }, modifier = Modifier.background(SuperficieInicio)) {
            opcoes.forEach { (chave, descricao) ->
                DropdownMenuItem(
                    text = { Text(descricao, color = TextoInicio) },
                    onClick = { aoSelecionar(chave); aberto = false },
                )
            }
        }
    }
}

@Composable
private fun MensagemPerfil(retorno: RetornoPerfil, modifier: Modifier = Modifier) {
    Text(
        retorno.mensagem,
        color = if (retorno.sucesso) CorSucessoInicio else Color(0xFFFF8E9B),
        fontSize = 12.sp,
        lineHeight = 18.sp,
        modifier = modifier.fillMaxWidth().background(
            (if (retorno.sucesso) CorSucessoInicio else Color(0xFFFF8E9B)).copy(alpha = 0.1f),
            RoundedCornerShape(12.dp),
        ).padding(12.dp),
    )
}

private fun dataApiParaTela(value: String?): String = value?.takeIf { it.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) }
    ?.split('-')?.let { "${it[2]}/${it[1]}/${it[0]}" }.orEmpty()

private fun dataTelaParaApi(value: String): String? {
    if (value.isBlank()) return null
    val match = Regex("^(\\d{2})/(\\d{2})/(\\d{4})$").matchEntire(value.trim()) ?: return null
    return "${match.groupValues[3]}-${match.groupValues[2]}-${match.groupValues[1]}"
}

@Composable
private fun CampoSenhaPerfil(
    rotulo: String,
    valor: String,
    aoAlterar: (String) -> Unit,
    habilitado: Boolean,
    visivel: Boolean,
    aoAlternarVisibilidade: () -> Unit,
) {
    OutlinedTextField(
        value = valor,
        onValueChange = aoAlterar,
        label = { Text(rotulo) },
        enabled = habilitado,
        singleLine = true,
        visualTransformation = if (visivel) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            TextButton(onClick = aoAlternarVisibilidade, enabled = habilitado) {
                Text(if (visivel) "Ocultar" else "Mostrar", color = RoxoClaroInicio, fontSize = 11.sp)
            }
        },
        colors = coresCampoPerfil(),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun coresCampoPerfil() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextoInicio,
    unfocusedTextColor = TextoInicio,
    focusedBorderColor = RoxoClaroInicio,
    unfocusedBorderColor = BordaInicio,
    focusedLabelColor = RoxoClaroInicio,
    unfocusedLabelColor = TextoSecundarioInicio,
    cursorColor = RoxoClaroInicio,
)

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
private fun EstruturaInicioDoador(
    areaSelecionada: AreaDoador,
    aoSelecionarArea: (AreaDoador) -> Unit,
    aoSair: () -> Unit,
    localizacaoUsuario: br.com.sosplus.data.LocalizacaoUsuario?,
    modifier: Modifier = Modifier,
    exibirCabecalho: Boolean = true,
    exibirNavegacao: Boolean = true,
    conteudo: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize().background(
            Brush.radialGradient(colors = listOf(RoxoInicio.copy(alpha = 0.19f), FundoInicio), radius = 900f),
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 20.dp)
                .padding(bottom = if (exibirNavegacao) 104.dp else 20.dp),
        ) {
            if (exibirCabecalho) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    LogoInicio()
                    Text(
                        text = localizacaoUsuario?.descricao ?: "Sua região",
                        color = TextoSecundarioInicio,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 16.dp),
                    )
                }
                Spacer(modifier = Modifier.height(30.dp))
            }
            conteudo()
        }
        if (exibirNavegacao) {
            NavegacaoDoador(
                areaSelecionada,
                aoSelecionarArea,
                Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }
    }
}

@Composable
fun NavegacaoDoador(
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
                val corElemento = if (selecionada) TextoInicio else TextoSecundarioInicio
                TextButton(
                    onClick = { aoSelecionarArea(area) },
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp)
                        .background(
                            if (selecionada) RoxoInicio.copy(alpha = 0.34f) else Color.Transparent,
                            RoundedCornerShape(20.dp),
                        ),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        when (area) {
                            AreaDoador.Inicio -> IconeInicioNav(cor = corElemento)
                            AreaDoador.Mapa -> IconeMapaNav(cor = corElemento)
                            AreaDoador.Carteira -> IconeCarteiraNav(cor = corElemento)
                            AreaDoador.Perfil -> IconePerfilNav(cor = corElemento)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = area.rotulo,
                            color = corElemento,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RotaInicioOng(
    token: String,
    usuario: UsuarioAutenticado,
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
        token = token,
        usuario = usuario,
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
    token: String,
    usuario: UsuarioAutenticado,
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
            text = "Olá, ${usuario.nome}",
            color = TextoInicio,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 7.dp),
        )
        Text(
            text = "${usuario.email}\nCNPJ: ${usuario.cnpj.orEmpty()}",
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
        EnderecoOng(token)
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
fun LogoInicio(modifier: Modifier = Modifier) {
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
