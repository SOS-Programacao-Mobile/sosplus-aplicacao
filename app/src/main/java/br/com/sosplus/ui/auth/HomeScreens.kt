package br.com.sosplus.ui.auth

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.sosplus.data.Campanha
import br.com.sosplus.data.DadosPessoaisUsuario
import br.com.sosplus.data.DoacaoUsuario
import br.com.sosplus.data.DomainRepository
import br.com.sosplus.data.LocalizacaoUsuario
import br.com.sosplus.data.OrganizacaoPublica
import br.com.sosplus.data.UsuarioAutenticado
import br.com.sosplus.ui.components.BannerAlerta
import br.com.sosplus.ui.components.BarraNavegacaoSos
import br.com.sosplus.ui.components.ChipStatus
import br.com.sosplus.ui.components.DialogoConfirmacao
import br.com.sosplus.ui.components.EstadoCarregando
import br.com.sosplus.ui.components.EstadoErro
import br.com.sosplus.ui.components.EstadoVazio
import br.com.sosplus.ui.components.Formatadores
import br.com.sosplus.ui.components.ItemNavegacao
import br.com.sosplus.ui.components.SosBotaoPrimario
import br.com.sosplus.ui.components.SosBotaoSecundario
import br.com.sosplus.ui.components.SosCampoSenha
import br.com.sosplus.ui.components.SosCampoTexto
import br.com.sosplus.ui.components.SosCard
import br.com.sosplus.ui.components.SosLogo
import br.com.sosplus.ui.components.SosTopBar
import br.com.sosplus.ui.components.TipoAlerta
import br.com.sosplus.ui.theme.BordaPadrao
import br.com.sosplus.ui.theme.CoralAcento
import br.com.sosplus.ui.theme.CorErro
import br.com.sosplus.ui.theme.CorSucesso
import br.com.sosplus.ui.theme.DivisorPadrao
import br.com.sosplus.ui.theme.FundoApp
import br.com.sosplus.ui.theme.FundoCampo
import br.com.sosplus.ui.theme.FundoSuperficie
import br.com.sosplus.ui.theme.FundoSuperficieElevada
import br.com.sosplus.ui.theme.RoxoClaro
import br.com.sosplus.ui.theme.RoxoContainer
import br.com.sosplus.ui.theme.RoxoPrincipal
import br.com.sosplus.ui.theme.TextoPrincipal
import br.com.sosplus.ui.theme.TextoSecundario
import br.com.sosplus.ui.theme.TextoTerciario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

val FundoInicio = FundoApp
val SuperficieInicio = FundoSuperficie
val BordaInicio = BordaPadrao
val RoxoInicio = RoxoPrincipal
val RoxoClaroInicio = RoxoClaro
val TextoInicio = TextoPrincipal
val TextoSecundarioInicio = TextoSecundario
val CorSucessoInicio = CorSucesso

fun moedaCampanha(centavos: Long): String = Formatadores.moeda(centavos)

enum class AreaDoador(
    val icone: ImageVector,
    val rotulo: String,
) {
    Inicio(Icons.Default.Home, "Início"),
    Mapa(Icons.Default.LocationOn, "Mapa"),
    Carteira(Icons.Default.VolunteerActivism, "Doações"),
    Perfil(Icons.Default.Person, "Perfil"),
}

enum class AreaOng(
    val icone: ImageVector,
    val rotulo: String,
) {
    Inicio(Icons.Default.Home, "Início"),
    CampanhaAtual(Icons.Default.Favorite, "Campanha atual"),
    CriarCampanha(Icons.Default.Add, "Criar campanha"),
    Campanhas(Icons.Default.Campaign, "Campanhas"),
    Dashboard(Icons.Default.Dashboard, "Dashboard"),
    Missao(Icons.Default.VolunteerActivism, "Nossa missão"),
    Materiais(Icons.Default.Inventory2, "Materiais"),
    Publicacoes(Icons.Default.DynamicFeed, "Publicações"),
    Perfil(Icons.Default.CorporateFare, "Perfil da ONG"),
}

private enum class OpcaoPerfil(
    val icone: ImageVector,
    val titulo: String,
    val descricao: String,
) {
    Informacoes(Icons.Default.Badge, "Informações pessoais", "Dados cadastrais e informações de saúde"),
    MinhasDoacoes(Icons.Default.Favorite, "Minhas doações", "Histórico das suas contribuições"),
    Pagamentos(Icons.Default.VolunteerActivism, "Minhas doações e carteira", "Histórico de contribuições financeiras e materiais"),
    Senha(Icons.Default.Lock, "Redefinir senha", "Atualize sua credencial com segurança"),
}

private enum class TelaPerfil { Principal, InformacoesPessoais, RedefinirSenha, MinhasDoacoes }

private data class RetornoPerfil(val mensagem: String, val sucesso: Boolean)

// -------------------------------------------------------------------------------------------------
// ROTA PRINCIPAL DOADOR
// -------------------------------------------------------------------------------------------------

@Composable
fun RotaInicioDoador(
    token: String,
    usuario: UsuarioAutenticado,
    fotoPerfil: ByteArray?,
    aoSalvarFotoPerfil: suspend (ByteArray, String) -> Unit,
    aoRedefinirSenha: suspend (String, String, String) -> Unit,
    aoCarregarDadosPessoais: suspend () -> DadosPessoaisUsuario,
    aoSalvarDadosPessoais: suspend (DadosPessoaisUsuario) -> DadosPessoaisUsuario,
    aoCarregarDoacoes: suspend () -> List<DoacaoUsuario>,
    aoSair: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var areaSelecionada by rememberSaveable { mutableStateOf(AreaDoador.Inicio) }
    var telaPerfil by rememberSaveable { mutableStateOf(TelaPerfil.Principal) }
    var campanhaSelecionadaId by rememberSaveable { mutableStateOf<String?>(null) }
    var ongSelecionadaId by rememberSaveable { mutableStateOf<String?>(null) }

    // Gerenciamento de navegação de volta (Heurística 3: User Control and Freedom)
    BackHandler(enabled = campanhaSelecionadaId != null || ongSelecionadaId != null || (areaSelecionada == AreaDoador.Perfil && telaPerfil != TelaPerfil.Principal)) {
        when {
            campanhaSelecionadaId != null -> campanhaSelecionadaId = null
            ongSelecionadaId != null -> ongSelecionadaId = null
            areaSelecionada == AreaDoador.Perfil && telaPerfil != TelaPerfil.Principal -> telaPerfil = TelaPerfil.Principal
        }
    }

    when {
        campanhaSelecionadaId != null -> {
            DetalheCampanha(
                campanhaId = requireNotNull(campanhaSelecionadaId),
                token = token,
                aoVoltar = { campanhaSelecionadaId = null },
                aoAbrirOng = { ongId ->
                    campanhaSelecionadaId = null
                    ongSelecionadaId = ongId
                },
                modifier = modifier,
            )
        }
        ongSelecionadaId != null -> {
            PerfilOngPublico(
                ongId = requireNotNull(ongSelecionadaId),
                token = token,
                aoVoltar = { ongSelecionadaId = null },
                aoAbrirCampanha = { idCamp ->
                    ongSelecionadaId = null
                    campanhaSelecionadaId = idCamp
                },
                modifier = modifier,
            )
        }
        else -> {
            TelaInicioDoador(
                token = token,
                usuario = usuario,
                fotoPerfil = fotoPerfil,
                areaSelecionada = areaSelecionada,
                aoSelecionarArea = {
                    areaSelecionada = it
                    telaPerfil = TelaPerfil.Principal
                },
                aoSalvarFotoPerfil = aoSalvarFotoPerfil,
                aoRedefinirSenha = aoRedefinirSenha,
                aoCarregarDadosPessoais = aoCarregarDadosPessoais,
                aoSalvarDadosPessoais = aoSalvarDadosPessoais,
                aoCarregarDoacoes = aoCarregarDoacoes,
                telaPerfil = telaPerfil,
                aoAlterarTelaPerfil = { telaPerfil = it },
                aoAbrirCampanha = { campanhaSelecionadaId = it },
                aoAbrirOng = { ongSelecionadaId = it },
                aoSair = aoSair,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun TelaInicioDoador(
    token: String,
    usuario: UsuarioAutenticado,
    fotoPerfil: ByteArray?,
    areaSelecionada: AreaDoador,
    aoSelecionarArea: (AreaDoador) -> Unit,
    aoSalvarFotoPerfil: suspend (ByteArray, String) -> Unit,
    aoRedefinirSenha: suspend (String, String, String) -> Unit,
    aoCarregarDadosPessoais: suspend () -> DadosPessoaisUsuario,
    aoSalvarDadosPessoais: suspend (DadosPessoaisUsuario) -> DadosPessoaisUsuario,
    aoCarregarDoacoes: suspend () -> List<DoacaoUsuario>,
    telaPerfil: TelaPerfil,
    aoAlterarTelaPerfil: (TelaPerfil) -> Unit,
    aoAbrirCampanha: (String) -> Unit,
    aoAbrirOng: (String) -> Unit,
    aoSair: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (areaSelecionada == AreaDoador.Mapa) {
        TelaMapaDoador(
            token = token,
            areaSelecionada = areaSelecionada,
            aoSelecionarArea = aoSelecionarArea,
            localizacaoUsuario = usuario.localizacao,
            modifier = modifier,
        )
    } else {
        EstruturaInicioDoador(
            areaSelecionada = areaSelecionada,
            aoSelecionarArea = aoSelecionarArea,
            usuario = usuario,
            exibirCabecalho = areaSelecionada != AreaDoador.Perfil || telaPerfil == TelaPerfil.Principal,
            exibirNavegacao = areaSelecionada != AreaDoador.Perfil || telaPerfil == TelaPerfil.Principal,
            modifier = modifier,
        ) {
            when (areaSelecionada) {
                AreaDoador.Inicio -> {
                    FeedDoadorComAbas(
                        token = token,
                        localizacao = usuario.localizacao,
                        aoAbrirCampanha = aoAbrirCampanha,
                        aoAbrirOng = aoAbrirOng,
                    )
                }
                AreaDoador.Carteira -> CarteiraDoador(token)
                AreaDoador.Perfil -> when (telaPerfil) {
                    TelaPerfil.Principal -> ConteudoPerfil(
                        usuario = usuario,
                        fotoPerfil = fotoPerfil,
                        aoSalvarFotoPerfil = aoSalvarFotoPerfil,
                        aoAbrirCarteira = { aoSelecionarArea(AreaDoador.Carteira) },
                        aoAbrirInformacoes = { aoAlterarTelaPerfil(TelaPerfil.InformacoesPessoais) },
                        aoAbrirRedefinicaoSenha = { aoAlterarTelaPerfil(TelaPerfil.RedefinirSenha) },
                        aoAbrirMinhasDoacoes = { aoAlterarTelaPerfil(TelaPerfil.MinhasDoacoes) },
                        aoSair = aoSair,
                    )
                    TelaPerfil.MinhasDoacoes -> TelaMinhasDoacoes(
                        aoCarregar = aoCarregarDoacoes,
                        aoVoltar = { aoAlterarTelaPerfil(TelaPerfil.Principal) },
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
                AreaDoador.Mapa -> Unit
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// FEED DOADOR COM ABAS SEGMENTADAS (Campanhas, Materiais, Publicações)
// -------------------------------------------------------------------------------------------------

@Composable
private fun FeedDoadorComAbas(
    token: String,
    localizacao: LocalizacaoUsuario?,
    aoAbrirCampanha: (String) -> Unit,
    aoAbrirOng: (String) -> Unit,
) {
    var abaSelecionada by rememberSaveable { mutableIntStateOf(0) }
    val abas = listOf("Campanhas", "Materiais", "Publicações")

    Column(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = FundoSuperficie,
            border = BorderStroke(1.dp, BordaPadrao),
        ) {
            TabRow(
                selectedTabIndex = abaSelecionada,
                containerColor = Color.Transparent,
                contentColor = TextoPrincipal,
                indicator = { posicoes ->
                    if (abaSelecionada < posicoes.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(posicoes[abaSelecionada]),
                            height = 3.dp,
                            color = RoxoPrincipal,
                        )
                    }
                },
                divider = { HorizontalDivider(color = DivisorPadrao) },
            ) {
                abas.forEachIndexed { indice, titulo ->
                    Tab(
                        selected = abaSelecionada == indice,
                        onClick = { abaSelecionada = indice },
                        text = {
                            Text(
                                text = titulo,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (abaSelecionada == indice) FontWeight.Bold else FontWeight.Medium,
                                color = if (abaSelecionada == indice) RoxoClaro else TextoSecundario,
                            )
                        },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (abaSelecionada) {
            0 -> FeedCampanhas(token, localizacao, aoAbrirCampanha)
            1 -> NecessidadesDoador(token)
            2 -> FeedSocialDoador(token)
        }
    }
}

@Composable
private fun FeedCampanhas(
    token: String,
    localizacao: LocalizacaoUsuario?,
    aoAbrirCampanha: (String) -> Unit,
) {
    val repository = remember { DomainRepository() }
    val scope = rememberCoroutineScope()
    var campanhas by remember { mutableStateOf<List<Campanha>>(emptyList()) }
    var cursor by remember { mutableStateOf<String?>(null) }
    var carregando by remember { mutableStateOf(true) }
    var carregandoMais by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf<String?>(null) }
    var campanhaParaDoar by remember { mutableStateOf<Campanha?>(null) }

    suspend fun carregar(proximo: String? = null, substituir: Boolean = false) {
        if (substituir) carregando = true else carregandoMais = true
        erro = null
        try {
            val page = repository.campanhas(localizacao?.cidade, localizacao?.estado, proximo, "ATIVA")
            campanhas = if (substituir) page.itens else campanhas + page.itens
            cursor = page.proximaPagina
        } catch (failure: CancellationException) {
            throw failure
        } catch (failure: Exception) {
            erro = failure.message ?: "Não foi possível carregar as campanhas."
        } finally {
            carregando = false
            carregandoMais = false
        }
    }

    LaunchedEffect(token, localizacao?.cidade, localizacao?.estado) {
        campanhas = emptyList()
        cursor = null
        carregar(substituir = true)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Campanhas ativas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextoPrincipal,
                )
                Text(
                    text = "Apoie causas com recursos financeiros.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario,
                )
            }
            if (campanhas.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = RoxoContainer,
                ) {
                    Text(
                        text = "${campanhas.size} ativas",
                        style = MaterialTheme.typography.labelSmall,
                        color = RoxoClaro,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when {
            carregando -> EstadoCarregando(mensagem = "Buscando campanhas perto de você...")
            erro != null && campanhas.isEmpty() -> EstadoErro(
                mensagem = erro.orEmpty(),
                aoTentarNovamente = { scope.launch { carregar(substituir = true) } },
            )
            campanhas.isEmpty() -> EstadoVazio(
                icone = Icons.Default.Campaign,
                titulo = "Nenhuma campanha no momento",
                descricao = "Não há campanhas ativas cadastradas na sua região agora. Fique de olho para novas iniciativas solidárias!",
            )
            else -> {
                campanhas.forEach { campanha ->
                    CartaoCampanhaFeed(
                        campanha = campanha,
                        aoAbrirDetalhes = { aoAbrirCampanha(campanha.id) },
                        aoAjudar = { campanhaParaDoar = campanha },
                        modifier = Modifier.padding(bottom = 14.dp),
                    )
                }

                if (cursor != null) {
                    SosBotaoSecundario(
                        texto = if (carregandoMais) "Carregando mais..." else "Ver mais campanhas",
                        aoClicar = { scope.launch { carregar(cursor) } },
                        habilitado = !carregandoMais,
                        icone = Icons.Default.Refresh,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, bottom = 12.dp),
                    )
                }
            }
        }
    }

    campanhaParaDoar?.let { campanha ->
        DoacaoSimuladaDialog(
            token = token,
            campanha = campanha,
            aoFechar = { campanhaParaDoar = null },
            aoConcluir = {
                campanhaParaDoar = null
                scope.launch { carregar(substituir = true) }
            },
        )
    }
}

@Composable
private fun CartaoCampanhaFeed(
    campanha: Campanha,
    aoAbrirDetalhes: () -> Unit,
    aoAjudar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SosCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = aoAbrirDetalhes),
    ) {
        Column {
            campanha.imagemId?.let { ImagemDoDominio(it) }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Surface(
                            modifier = Modifier.size(34.dp),
                            shape = CircleShape,
                            color = RoxoContainer,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CorporateFare,
                                    contentDescription = null,
                                    tint = RoxoClaro,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                        Column {
                            Text(
                                text = campanha.ongNome,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TextoPrincipal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = campanha.categoria.nome,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextoSecundario,
                            )
                        }
                    }

                    ChipStatus(status = campanha.status)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = campanha.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextoPrincipal,
                )

                Text(
                    text = campanha.descricao,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSecundario,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )

                val progresso = if (campanha.metaCentavos <= 0) 0f
                else (campanha.arrecadadoCentavos.toFloat() / campanha.metaCentavos.toFloat()).coerceIn(0f, 1f)

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FundoCampo, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Arrecadado",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextoSecundario,
                        )
                        Text(
                            text = "${(progresso * 100).toInt()}% da meta",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = RoxoClaro,
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { progresso },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = RoxoPrincipal,
                        trackColor = BordaPadrao,
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = Formatadores.moeda(campanha.arrecadadoCentavos),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextoPrincipal,
                        )
                        Text(
                            text = "Meta: ${Formatadores.moeda(campanha.metaCentavos)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SosBotaoSecundario(
                        texto = "Detalhes",
                        aoClicar = aoAbrirDetalhes,
                        modifier = Modifier.weight(1f),
                    )
                    if (campanha.status == "ATIVA") {
                        SosBotaoPrimario(
                            texto = "Apoiar",
                            aoClicar = aoAjudar,
                            icone = Icons.Default.Favorite,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// PERFIL DO DOADOR E SUB-TELAS
// -------------------------------------------------------------------------------------------------

@Composable
private fun ConteudoPerfil(
    usuario: UsuarioAutenticado,
    fotoPerfil: ByteArray?,
    aoSalvarFotoPerfil: suspend (ByteArray, String) -> Unit,
    aoAbrirCarteira: () -> Unit,
    aoAbrirInformacoes: () -> Unit,
    aoAbrirRedefinicaoSenha: () -> Unit,
    aoAbrirMinhasDoacoes: () -> Unit,
    aoSair: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var salvandoFoto by remember { mutableStateOf(false) }
    var retorno by remember { mutableStateOf<RetornoPerfil?>(null) }
    var confirmarSaida by remember { mutableStateOf(false) }

    val bitmap by produceState<Bitmap?>(initialValue = null, fotoPerfil) {
        value = withContext(Dispatchers.Default) {
            fotoPerfil?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
        }
    }
    val iniciais = remember(usuario.nome) {
        usuario.nome.trim().split(Regex("\\s+")).take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("").ifBlank { "?" }
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

    SosCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier
                        .size(96.dp)
                        .clickable(enabled = !salvandoFoto) {
                            selecionarFoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                    shape = CircleShape,
                    color = RoxoContainer,
                    border = BorderStroke(2.dp, RoxoClaro),
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = requireNotNull(bitmap).asImageBitmap(),
                            contentDescription = "Foto de perfil de ${usuario.nome}",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.linearGradient(listOf(RoxoPrincipal, Color(0xFF4B4FA0)))),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(iniciais, color = TextoPrincipal, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                if (salvandoFoto) {
                    Surface(
                        modifier = Modifier.size(96.dp),
                        shape = CircleShape,
                        color = FundoApp.copy(alpha = 0.72f),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = RoxoClaro,
                                strokeWidth = 3.dp,
                            )
                        }
                    }
                }
            }

            Text(
                text = usuario.nome,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextoPrincipal,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 14.dp),
            )
            Text(
                text = usuario.email,
                style = MaterialTheme.typography.bodyMedium,
                color = TextoSecundario,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )

            Surface(
                color = CorSucesso.copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text(
                    text = "Doador Solidário",
                    color = CorSucesso,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                )
            }

            SosBotaoSecundario(
                texto = if (fotoPerfil == null) "Adicionar foto de perfil" else "Alterar foto",
                aoClicar = {
                    selecionarFoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                habilitado = !salvandoFoto,
                modifier = Modifier.padding(top = 14.dp),
            )

            retorno?.let { estado ->
                BannerAlerta(
                    mensagem = estado.mensagem,
                    tipo = if (estado.sucesso) TipoAlerta.Sucesso else TipoAlerta.Erro,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(
        text = "CONTA E PREFERÊNCIAS",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = RoxoClaro,
        letterSpacing = 1.sp,
    )

    Spacer(modifier = Modifier.height(10.dp))

    SosCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            OpcaoPerfil.entries.forEachIndexed { indice, opcao ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            when (opcao) {
                                OpcaoPerfil.MinhasDoacoes -> aoAbrirMinhasDoacoes()
                                OpcaoPerfil.Informacoes -> aoAbrirInformacoes()
                                OpcaoPerfil.Pagamentos -> aoAbrirCarteira()
                                OpcaoPerfil.Senha -> aoAbrirRedefinicaoSenha()
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = RoxoContainer,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = opcao.icone,
                                contentDescription = null,
                                tint = RoxoClaro,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 14.dp),
                    ) {
                        Text(
                            text = opcao.titulo,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TextoPrincipal,
                        )
                        Text(
                            text = opcao.descricao,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSecundario,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Abrir",
                        tint = TextoSecundario,
                        modifier = Modifier.size(22.dp),
                    )
                }

                if (indice < OpcaoPerfil.entries.lastIndex) {
                    HorizontalDivider(color = DivisorPadrao, modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    OutlinedButton(
        onClick = { confirmarSaida = true },
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CoralAcento.copy(alpha = 0.6f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralAcento),
    ) {
        Icon(imageVector = Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Encerrar sessão", fontWeight = FontWeight.SemiBold)
    }

    if (confirmarSaida) {
        DialogoConfirmacao(
            titulo = "Sair da conta",
            mensagem = "Tem certeza de que deseja encerrar sua sessão no SOS+? Você precisará fazer login novamente para doar ou ver seu histórico.",
            textoConfirmar = "Sair",
            textoCancelar = "Continuar conectado",
            perigoso = true,
            aoConfirmar = {
                confirmarSaida = false
                aoSair()
            },
            aoCancelar = { confirmarSaida = false },
        )
    }
}

// -------------------------------------------------------------------------------------------------
// TELA REDEFINIR SENHA PERFIL
// -------------------------------------------------------------------------------------------------

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
    var salvando by remember { mutableStateOf(false) }
    var retorno by remember { mutableStateOf<RetornoPerfil?>(null) }

    CabecalhoTelaPerfil(titulo = "Redefinir Senha", aoVoltar = aoVoltar)

    SosCard(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Atualização de Credencial",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextoPrincipal,
            )
            Text(
                text = "Confirme sua senha atual e escolha uma nova senha forte com pelo menos 8 caracteres.",
                style = MaterialTheme.typography.bodySmall,
                color = TextoSecundario,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
            )

            SosCampoTexto(
                valor = email,
                aoMudarValor = {},
                rotulo = "E-mail cadastrado",
                habilitado = false,
                icone = Icons.Default.Person,
            )

            Spacer(modifier = Modifier.height(14.dp))

            SosCampoSenha(
                valor = senhaAtual,
                aoMudarValor = { senhaAtual = it; retorno = null },
                rotulo = "Senha atual",
                habilitado = !salvando,
            )

            Spacer(modifier = Modifier.height(14.dp))

            SosCampoSenha(
                valor = novaSenha,
                aoMudarValor = { novaSenha = it; retorno = null },
                rotulo = "Nova senha (mínimo 8 caracteres)",
                habilitado = !salvando,
            )

            Spacer(modifier = Modifier.height(14.dp))

            SosCampoSenha(
                valor = confirmacao,
                aoMudarValor = { confirmacao = it; retorno = null },
                rotulo = "Confirmar nova senha",
                habilitado = !salvando,
            )

            retorno?.let { estado ->
                BannerAlerta(
                    mensagem = estado.mensagem,
                    tipo = if (estado.sucesso) TipoAlerta.Sucesso else TipoAlerta.Erro,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            SosBotaoPrimario(
                texto = "Atualizar senha",
                aoClicar = {
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
                                retorno = RetornoPerfil("Senha atualizada com sucesso!", true)
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
                carregando = salvando,
                habilitado = !salvando,
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// TELA INFORMAÇÕES PESSOAIS
// -------------------------------------------------------------------------------------------------

@Composable
private fun TelaInformacoesPessoais(
    usuario: UsuarioAutenticado,
    fotoPerfil: ByteArray?,
    aoCarregar: suspend () -> DadosPessoaisUsuario,
    aoSalvar: suspend (DadosPessoaisUsuario) -> DadosPessoaisUsuario,
    aoVoltar: () -> Unit,
) {
    val scope = rememberCoroutineScope()
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
        cpf = Formatadores.formatarCpf(dados.cpf.orEmpty())
        dataNascimento = dataApiParaTela(dados.dataNascimento)
        endereco = dados.endereco.orEmpty()
        tipoSanguineo = dados.tipoSanguineo
        genero = dados.genero
        telefone = Formatadores.formatarTelefone(dados.telefone.orEmpty())
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

    CabecalhoTelaPerfil(titulo = "Informações Pessoais", aoVoltar = aoVoltar)

    if (carregando) {
        EstadoCarregando(mensagem = "Carregando suas informações...")
        return
    }

    if (retorno?.sucesso == false && nome.isBlank()) {
        EstadoErro(
            mensagem = retorno?.mensagem ?: "Falha ao carregar perfil.",
            aoTentarNovamente = { tentativa++ },
            modifier = Modifier.padding(top = 16.dp),
        )
        return
    }

    SosCard(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
        Column(modifier = Modifier.padding(18.dp)) {
            BannerAlerta(
                mensagem = "Campos opcionais podem ser deixados em branco. Tipo sanguíneo e dados de saúde ficam protegidos no armazenamento seguro.",
                tipo = TipoAlerta.Informativo,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            SosCampoTexto(
                valor = nome,
                aoMudarValor = { nome = it; retorno = null },
                rotulo = "Nome *",
                habilitado = !salvando,
                capitalizacao = KeyboardCapitalization.Words,
            )

            Spacer(modifier = Modifier.height(14.dp))

            SosCampoTexto(
                valor = sobrenome,
                aoMudarValor = { sobrenome = it; retorno = null },
                rotulo = "Sobrenome",
                habilitado = !salvando,
                capitalizacao = KeyboardCapitalization.Words,
            )

            Spacer(modifier = Modifier.height(14.dp))

            SosCampoTexto(
                valor = cpf,
                aoMudarValor = {
                    cpf = Formatadores.formatarCpf(it.filter { c -> c.isDigit() })
                    retorno = null
                },
                rotulo = "CPF",
                habilitado = !salvando,
                tipoTeclado = KeyboardType.Number,
            )

            Spacer(modifier = Modifier.height(14.dp))

            SosCampoTexto(
                valor = dataNascimento,
                aoMudarValor = {
                    val digitos = it.filter { c -> c.isDigit() }.take(8)
                    dataNascimento = when {
                        digitos.length <= 2 -> digitos
                        digitos.length <= 4 -> "${digitos.take(2)}/${digitos.substring(2)}"
                        else -> "${digitos.take(2)}/${digitos.substring(2, 4)}/${digitos.substring(4)}"
                    }
                    retorno = null
                },
                rotulo = "Data de nascimento (DD/MM/AAAA)",
                habilitado = !salvando,
                tipoTeclado = KeyboardType.Number,
            )

            Spacer(modifier = Modifier.height(14.dp))

            SosCampoTexto(
                valor = telefone,
                aoMudarValor = {
                    telefone = Formatadores.formatarTelefone(it.filter { c -> c.isDigit() })
                    retorno = null
                },
                rotulo = "Telefone com DDD",
                habilitado = !salvando,
                tipoTeclado = KeyboardType.Phone,
            )

            Spacer(modifier = Modifier.height(14.dp))

            SosCampoTexto(
                valor = endereco,
                aoMudarValor = { endereco = it; retorno = null },
                rotulo = "Endereço completo",
                habilitado = !salvando,
                umaLinha = false,
                linhasMinimas = 2,
            )

            Spacer(modifier = Modifier.height(14.dp))

            CampoSelecaoPerfil(
                rotulo = "Tipo sanguíneo",
                valor = tipoSanguineo,
                opcoes = listOf(
                    null to "Não informar",
                    "A+" to "A+", "A-" to "A-",
                    "B+" to "B+", "B-" to "B-",
                    "AB+" to "AB+", "AB-" to "AB-",
                    "O+" to "O+", "O-" to "O-",
                ),
                habilitado = !salvando,
                aoSelecionar = { tipoSanguineo = it; retorno = null },
            )

            Spacer(modifier = Modifier.height(14.dp))

            CampoSelecaoPerfil(
                rotulo = "Gênero",
                valor = genero,
                opcoes = listOf(
                    null to "Não informar",
                    "MASCULINO" to "Masculino",
                    "FEMININO" to "Feminino",
                    "OUTROS" to "Outros",
                    "PREFIRO_NAO_INFORMAR" to "Prefiro não informar",
                ),
                habilitado = !salvando,
                aoSelecionar = { genero = it; retorno = null },
            )

            retorno?.let {
                BannerAlerta(
                    mensagem = it.mensagem,
                    tipo = if (it.sucesso) TipoAlerta.Sucesso else TipoAlerta.Erro,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            SosBotaoPrimario(
                texto = "Salvar alterações",
                aoClicar = {
                    val dataApi = dataTelaParaApi(dataNascimento)
                    val erroLocal = when {
                        nome.isBlank() -> "Informe seu nome."
                        nome.trim().length + sobrenome.trim().let { if (it.isEmpty()) 0 else it.length + 1 } > 150 ->
                            "Nome e sobrenome devem ter até 150 caracteres juntos."
                        dataNascimento.isNotBlank() && dataApi == null ->
                            "Informe a data no formato DD/MM/AAAA."
                        else -> null
                    }
                    if (erroLocal != null) {
                        retorno = RetornoPerfil(erroLocal, false)
                    } else {
                        salvando = true
                        retorno = null
                        scope.launch {
                            try {
                                val atualizados = aoSalvar(
                                    DadosPessoaisUsuario(
                                        nome = nome.trim(),
                                        sobrenome = sobrenome.trim().ifBlank { null },
                                        cpf = cpf.filter { it.isDigit() }.ifBlank { null },
                                        dataNascimento = dataApi,
                                        endereco = endereco.trim().ifBlank { null },
                                        tipoSanguineo = tipoSanguineo,
                                        genero = genero,
                                        telefone = telefone.filter { it.isDigit() }.ifBlank { null },
                                    ),
                                )
                                preencher(atualizados)
                                retorno = RetornoPerfil("Informações salvas com sucesso!", true)
                            } catch (error: CancellationException) {
                                throw error
                            } catch (error: IOException) {
                                retorno = RetornoPerfil(error.message ?: "Não foi possível salvar os dados.", false)
                            } finally {
                                salvando = false
                            }
                        }
                    }
                },
                carregando = salvando,
                habilitado = !salvando,
            )
        }
    }
}

@Composable
private fun TelaMinhasDoacoes(
    aoCarregar: suspend () -> List<DoacaoUsuario>,
    aoVoltar: () -> Unit,
) {
    // Recarregado da API a cada entrada nesta tela.
    var doacoes by remember { mutableStateOf<List<DoacaoUsuario>?>(null) }
    var erro by remember { mutableStateOf<String?>(null) }
    var tentativa by remember { mutableStateOf(0) }

    LaunchedEffect(tentativa) {
        erro = null
        try {
            doacoes = aoCarregar()
        } catch (error: CancellationException) {
            throw error
        } catch (error: IOException) {
            erro = error.message ?: "Não foi possível carregar suas doações."
        }
    }

    CabecalhoTelaPerfil("Minhas doações", aoVoltar)
    Text(
        "HISTÓRICO",
        color = RoxoClaroInicio,
        style = MaterialTheme.typography.labelMedium,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 16.dp, bottom = 10.dp),
    )

    val lista = doacoes
    when {
        erro != null -> {
            BannerAlerta(
                mensagem = requireNotNull(erro),
                tipo = TipoAlerta.Erro,
                modifier = Modifier.padding(top = 8.dp),
            )
            OutlinedButton(onClick = { tentativa++ }, modifier = Modifier.fillMaxWidth().padding(top = 14.dp)) {
                Text("Tentar novamente")
            }
        }
        lista == null -> Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = RoxoClaroInicio)
        }
        lista.isEmpty() -> Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SuperficieInicio,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, BordaInicio),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(28.dp)) {
                Text("♡", color = RoxoClaroInicio, fontSize = 39.sp)
                Text("Nenhuma doação ainda", color = TextoInicio, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 12.dp))
                Text("Quando você apoiar uma campanha, ela aparecerá aqui.", color = TextoSecundarioInicio, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 7.dp))
            }
        }
        else -> HistoricoDoacoesFiltrado(lista)
    }
}

@Composable
private fun HistoricoDoacoesFiltrado(doacoes: List<DoacaoUsuario>) {
    var periodo by rememberSaveable { mutableStateOf(PeriodoDoacao.Todas) }
    val filtradas = remember(doacoes, periodo) { filtrarDoacoesPorPeriodo(doacoes, periodo) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
    ) {
        PeriodoDoacao.entries.forEach { opcao ->
            ChipPeriodo(opcao.rotulo, selecionado = opcao == periodo) { periodo = opcao }
        }
    }

    val quantidade = if (filtradas.size == 1) "1 doação" else "${filtradas.size} doações"
    Text(
        "$quantidade · ${formatarValorDoacao(totalConfirmadoCentavos(filtradas))} confirmados",
        color = TextoSecundarioInicio,
        fontSize = 12.5.sp,
        modifier = Modifier.padding(top = 14.dp, bottom = 10.dp),
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SuperficieInicio,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, BordaInicio),
    ) {
        if (filtradas.isEmpty()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                Text("Nenhuma doação neste período", color = TextoInicio, fontWeight = FontWeight.SemiBold)
                TextButton(onClick = { periodo = PeriodoDoacao.Todas }, modifier = Modifier.padding(top = 4.dp)) {
                    Text("Ver todas", color = RoxoClaroInicio, fontWeight = FontWeight.SemiBold)
                }
            }
        } else {
            Column {
                filtradas.forEachIndexed { indice, doacao ->
                    ItemDoacao(doacao)
                    if (indice < filtradas.lastIndex) {
                        HorizontalDivider(color = BordaInicio.copy(alpha = 0.65f), modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ChipPeriodo(texto: String, selecionado: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selecionado) RoxoInicio.copy(alpha = 0.28f) else Color.Transparent,
        border = BorderStroke(1.dp, if (selecionado) RoxoClaroInicio else BordaInicio),
    ) {
        Text(
            texto,
            color = if (selecionado) TextoInicio else TextoSecundarioInicio,
            fontSize = 12.5.sp,
            fontWeight = if (selecionado) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
        )
    }
}

@Composable
private fun ItemDoacao(doacao: DoacaoUsuario) {
    val (rotuloStatus, corStatus) = when (doacao.status) {
        "CONFIRMADA" -> "CONFIRMADA" to CorSucessoInicio
        "PENDENTE" -> "PENDENTE" to Color(0xFFFFC46B)
        "CANCELADA" -> "CANCELADA" to Color(0xFFFF8E9B)
        "ESTORNADA" -> "ESTORNADA" to Color(0xFFFF8E9B)
        else -> doacao.status to TextoSecundarioInicio
    }
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                doacao.ongNome,
                color = TextoInicio,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                formatarValorDoacao(doacao.valorCentavos),
                color = TextoInicio,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
        Text(
            "Campanha: ${doacao.campanhaTitulo}",
            color = TextoSecundarioInicio,
            fontSize = 12.5.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 3.dp),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 9.dp),
        ) {
            Text(formatarDataDoacao(doacao.data), color = TextoSecundarioInicio, fontSize = 12.sp)
            EtiquetaDoacao(rotuloStatus, corStatus)
            if (doacao.anonima) EtiquetaDoacao("ANÔNIMA", RoxoClaroInicio)
        }
    }
}

@Composable
private fun EtiquetaDoacao(texto: String, cor: Color) {
    Surface(color = cor.copy(alpha = 0.12f), shape = RoundedCornerShape(20.dp)) {
        Text(
            texto,
            color = cor,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}


// -------------------------------------------------------------------------------------------------
// COMPONENTES AUXILIARES DE FORMULÁRIO DO PERFIL
// -------------------------------------------------------------------------------------------------

@Composable
private fun CabecalhoTelaPerfil(titulo: String, aoVoltar: () -> Unit) {
    SosTopBar(
        titulo = titulo,
        aoVoltar = aoVoltar,
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
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = habilitado) { aberto = true },
            color = FundoCampo,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (aberto) RoxoClaro else BordaPadrao),
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = rotulo,
                    color = if (aberto) RoxoClaro else TextoSecundario,
                    style = MaterialTheme.typography.labelSmall,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = texto,
                        color = TextoPrincipal,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Selecionar",
                        tint = TextoSecundario,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
        DropdownMenu(
            expanded = aberto,
            onDismissRequest = { aberto = false },
            modifier = Modifier.background(FundoSuperficieElevada),
        ) {
            opcoes.forEach { (chave, descricao) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = descricao,
                            color = if (chave == valor) RoxoClaro else TextoPrincipal,
                            fontWeight = if (chave == valor) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                    onClick = {
                        aoSelecionar(chave)
                        aberto = false
                    },
                )
            }
        }
    }
}

private fun dataApiParaTela(value: String?): String = value?.takeIf { it.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) }
    ?.split('-')?.let { "${it[2]}/${it[1]}/${it[0]}" }.orEmpty()

private fun dataTelaParaApi(value: String): String? {
    if (value.isBlank()) return null
    val match = Regex("^(\\d{2})/(\\d{2})/(\\d{4})$").matchEntire(value.trim()) ?: return null
    return "${match.groupValues[3]}-${match.groupValues[2]}-${match.groupValues[1]}"
}

// -------------------------------------------------------------------------------------------------
// ESTRUTURA GERAL COM TOPBAR E BOTTOMBAR DO DOADOR
// -------------------------------------------------------------------------------------------------

@Composable
private fun EstruturaInicioDoador(
    areaSelecionada: AreaDoador,
    aoSelecionarArea: (AreaDoador) -> Unit,
    usuario: UsuarioAutenticado,
    modifier: Modifier = Modifier,
    exibirCabecalho: Boolean = true,
    exibirNavegacao: Boolean = true,
    conteudo: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FundoApp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .padding(bottom = if (exibirNavegacao) 96.dp else 16.dp),
        ) {
            if (exibirCabecalho) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    LogoInicio()

                    Surface(
                        color = RoxoContainer,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, RoxoPrincipal.copy(alpha = 0.3f)),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = RoxoClaro,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = usuario.localizacao?.descricao ?: "Nova Friburgo, RJ",
                                color = TextoPrincipal,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                SosCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Surface(shape = RoundedCornerShape(14.dp), color = RoxoContainer, modifier = Modifier.size(46.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = RoxoClaro, modifier = Modifier.size(24.dp))
                            }
                        }
                        Column {
                            Text("Olá, ${usuario.nome}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextoPrincipal)
                            Text("O que você deseja apoiar hoje?", style = MaterialTheme.typography.bodySmall, color = TextoSecundario, modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
            }

            conteudo()
        }

        if (exibirNavegacao) {
            NavegacaoDoador(
                areaSelecionada = areaSelecionada,
                aoSelecionarArea = aoSelecionarArea,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding(),
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
    val itens = remember(areaSelecionada) {
        AreaDoador.entries.map { area ->
            ItemNavegacao(
                rotulo = area.rotulo,
                icone = area.icone,
                selecionado = area == areaSelecionada,
                onClick = { aoSelecionarArea(area) },
            )
        }
    }

    BarraNavegacaoSos(
        itens = itens,
        modifier = modifier,
    )
}

// -------------------------------------------------------------------------------------------------
// ROTA E TELA DA ONG (ORGANIZAÇÃO EM 5 ABAS)
// -------------------------------------------------------------------------------------------------

@Composable
fun RotaInicioOng(
    token: String,
    usuario: UsuarioAutenticado,
    aoSair: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TelaInicioOng(token, usuario, aoSair, modifier)
}

@Composable
private fun TelaInicioOng(
    token: String,
    usuario: UsuarioAutenticado,
    aoSair: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var areaSelecionada by rememberSaveable { mutableStateOf(AreaOng.Inicio) }
    var confirmarSaida by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BackHandler(enabled = drawerState.isClosed && areaSelecionada != AreaOng.Inicio) {
        areaSelecionada = AreaOng.Inicio
    }

    ModalNavigationDrawer(
        modifier = modifier,
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = FundoSuperficie,
                drawerContentColor = TextoPrincipal,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                ) {
                    SosLogo(modifier = Modifier.padding(vertical = 12.dp))
                    Text(usuario.nome, color = TextoPrincipal, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(usuario.email, color = TextoSecundario, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 2.dp, bottom = 14.dp))
                    HorizontalDivider(color = DivisorPadrao)
                    Spacer(modifier = Modifier.height(10.dp))
                    AreaOng.entries.forEach { area ->
                        NavigationDrawerItem(
                            label = { Text(area.rotulo, maxLines = 1) },
                            icon = { Icon(area.icone, contentDescription = area.rotulo) },
                            selected = area == areaSelecionada,
                            onClick = {
                                areaSelecionada = area
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(vertical = 2.dp),
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = RoxoContainer,
                                selectedIconColor = RoxoClaro,
                                selectedTextColor = TextoPrincipal,
                                unselectedContainerColor = Color.Transparent,
                                unselectedIconColor = TextoTerciario,
                                unselectedTextColor = TextoPrincipal,
                            ),
                        )
                    }
                    HorizontalDivider(color = DivisorPadrao, modifier = Modifier.padding(vertical = 10.dp))
                    NavigationDrawerItem(
                        label = { Text("Sair") },
                        icon = { Icon(Icons.Default.Logout, contentDescription = null) },
                        selected = false,
                        onClick = { confirmarSaida = true; scope.launch { drawerState.close() } },
                        colors = NavigationDrawerItemDefaults.colors(unselectedIconColor = CoralAcento, unselectedTextColor = CoralAcento),
                    )
                }
            }
        },
    ) {
        Column(modifier = Modifier.fillMaxSize().background(FundoApp)) {
            SosTopBar(
                titulo = areaSelecionada.rotulo,
                subtitulo = usuario.nome,
                acoes = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, contentDescription = "Abrir menu", tint = TextoPrincipal)
                    }
                },
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                when (areaSelecionada) {
                    AreaOng.Inicio -> PaginaInicialOng(
                        usuario = usuario,
                        token = token,
                        aoNavegarPara = { areaSelecionada = it },
                    )
                    AreaOng.CampanhaAtual -> CampanhaAtualOng(
                        token = token,
                        aoVerCampanhas = { areaSelecionada = AreaOng.Campanhas },
                        aoCriarCampanha = { areaSelecionada = AreaOng.CriarCampanha },
                    )
                    AreaOng.CriarCampanha -> CampanhasOng(
                        token = token,
                        modo = ModoCampanhasOng.CRIACAO,
                        aoNavegarParaLista = { areaSelecionada = AreaOng.Campanhas },
                    )
                    AreaOng.Campanhas -> CampanhasOng(
                        token = token,
                        aoCriarNova = { areaSelecionada = AreaOng.CriarCampanha },
                    )
                    AreaOng.Dashboard -> DashboardOng(token)
                    AreaOng.Missao -> MissaoOng(token, aoEditar = { areaSelecionada = AreaOng.Perfil })
                    AreaOng.Materiais -> NecessidadesOng(token)
                    AreaOng.Publicacoes -> PublicacoesOng(token)
                    AreaOng.Perfil -> PerfilInstitucionalOng(token, usuario)
                }
            }
        }

        if (confirmarSaida) {
            DialogoConfirmacao(
                titulo = "Encerrar sessão",
                mensagem = "Deseja sair da área institucional de ${usuario.nome}?",
                textoConfirmar = "Sair",
                textoCancelar = "Continuar no sistema",
                perigoso = true,
                aoConfirmar = { confirmarSaida = false; aoSair() },
                aoCancelar = { confirmarSaida = false },
            )
        }
    }
}

@Composable
private fun PaginaInicialOng(
    usuario: UsuarioAutenticado,
    token: String,
    aoNavegarPara: (AreaOng) -> Unit,
) {
    SosCard(modifier = Modifier.fillMaxWidth()) {
        Text("Olá, ${usuario.nome}", color = TextoPrincipal, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Veja o andamento da sua organização e escolha o próximo passo.", color = TextoSecundario, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
    }

    CampanhaAtualOng(
        token = token,
        aoVerCampanhas = { aoNavegarPara(AreaOng.Campanhas) },
        aoCriarCampanha = { aoNavegarPara(AreaOng.CriarCampanha) },
    )

    Text("Atalhos", color = TextoPrincipal, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    AcaoRapidaOng("Criar campanha", "Comece uma nova mobilização", Icons.Default.Add) { aoNavegarPara(AreaOng.CriarCampanha) }
    AcaoRapidaOng("Ver campanhas", "Acompanhe rascunhos e resultados", Icons.Default.Campaign) { aoNavegarPara(AreaOng.Campanhas) }
    AcaoRapidaOng("Ver dashboard", "Consulte o resumo do ano", Icons.Default.Dashboard) { aoNavegarPara(AreaOng.Dashboard) }
    AcaoRapidaOng("Nossa missão", "Apresente a causa da organização", Icons.Default.VolunteerActivism) { aoNavegarPara(AreaOng.Missao) }
}

@Composable
private fun AcaoRapidaOng(titulo: String, descricao: String, icone: ImageVector, onClick: () -> Unit) {
    SosCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icone, contentDescription = null, tint = RoxoClaro, modifier = Modifier.size(24.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, color = TextoPrincipal, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(descricao, color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextoSecundario, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun CampanhaAtualOng(
    token: String,
    aoVerCampanhas: () -> Unit,
    aoCriarCampanha: () -> Unit,
) {
    val repository = remember { DomainRepository() }
    var campanha by remember { mutableStateOf<Campanha?>(null) }
    var carregando by remember { mutableStateOf(true) }
    var erro by remember { mutableStateOf<String?>(null) }
    var tentativa by remember { mutableIntStateOf(0) }

    LaunchedEffect(token, tentativa) {
        carregando = true
        erro = null
        try {
            campanha = repository.minhasCampanhas(token, "ATIVA").firstOrNull()
        } catch (failure: CancellationException) {
            throw failure
        } catch (failure: Exception) {
            erro = failure.message ?: "Não foi possível carregar a campanha atual."
        } finally {
            carregando = false
        }
    }

    SosCard(modifier = Modifier.fillMaxWidth()) {
        Text("Campanha atual", color = TextoPrincipal, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("A campanha ativa mais recente da sua ONG.", color = TextoSecundario, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 3.dp, bottom = 10.dp))
        when {
            carregando -> EstadoCarregando(mensagem = "Carregando campanha...")
            erro != null -> EstadoErro(mensagem = erro.orEmpty(), aoTentarNovamente = { tentativa++ })
            campanha == null -> EstadoVazio(
                titulo = "Nenhuma campanha ativa",
                mensagem = "Crie ou publique uma campanha para acompanhá-la aqui.",
                textoAcao = "Criar campanha",
                aoClicarAcao = aoCriarCampanha,
            )
            else -> {
                val atual = requireNotNull(campanha)
                val progresso = if (atual.metaCentavos > 0) (atual.arrecadadoCentavos.toFloat() / atual.metaCentavos.toFloat()).coerceIn(0f, 1f) else 0f
                Text(atual.titulo, color = TextoPrincipal, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(atual.categoria.nome, color = RoxoClaro, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 3.dp))
                Text(atual.descricao, color = TextoSecundario, style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 8.dp))
                LinearProgressIndicator(
                    progress = { progresso },
                    modifier = Modifier.fillMaxWidth().padding(top = 14.dp).height(8.dp),
                    color = CorSucesso,
                    trackColor = FundoCampo,
                )
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(Formatadores.formatarMoeda(atual.arrecadadoCentavos), color = CorSucesso, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text("Meta ${Formatadores.formatarMoeda(atual.metaCentavos)}", color = TextoSecundario, style = MaterialTheme.typography.labelMedium)
                }
                SosBotaoSecundario(texto = "Ver todas as campanhas", onClick = aoVerCampanhas, modifier = Modifier.padding(top = 12.dp))
            }
        }
    }
}

@Composable
private fun MissaoOng(token: String, aoEditar: () -> Unit) {
    val repository = remember { DomainRepository() }
    var ong by remember { mutableStateOf<OrganizacaoPublica?>(null) }
    var carregando by remember { mutableStateOf(true) }
    var erro by remember { mutableStateOf<String?>(null) }
    var tentativa by remember { mutableIntStateOf(0) }

    LaunchedEffect(token, tentativa) {
        carregando = true
        erro = null
        try {
            ong = repository.perfilOng(token)
        } catch (failure: CancellationException) {
            throw failure
        } catch (failure: Exception) {
            erro = failure.message ?: "Não foi possível carregar a missão da ONG."
        } finally {
            carregando = false
        }
    }

    when {
        carregando -> EstadoCarregando(mensagem = "Carregando missão...")
        erro != null -> EstadoErro(mensagem = erro.orEmpty(), aoTentarNovamente = { tentativa++ })
        else -> SosCard(modifier = Modifier.fillMaxWidth()) {
            Text("Nossa missão", color = TextoPrincipal, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            val descricao = ong?.descricao.orEmpty()
            if (descricao.isBlank()) {
                EstadoVazio(
                    titulo = "Conte a missão da sua ONG",
                    mensagem = "Explique a causa, quem vocês atendem e o impacto que querem gerar.",
                    textoAcao = "Editar perfil institucional",
                    aoClicarAcao = aoEditar,
                )
            } else {
                Text(descricao, color = TextoSecundario, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 12.dp))
                SosBotaoSecundario(texto = "Editar missão", onClick = aoEditar, modifier = Modifier.padding(top = 16.dp))
            }
        }
    }
}

@Composable
private fun PerfilInstitucionalOng(token: String, usuario: UsuarioAutenticado) {
    var abaPerfil by rememberSaveable { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxWidth()) {
        SosCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Surface(shape = CircleShape, color = RoxoContainer, modifier = Modifier.size(58.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CorporateFare, contentDescription = null, tint = RoxoClaro, modifier = Modifier.size(30.dp))
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(usuario.nome, color = TextoPrincipal, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(usuario.email, color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
                    usuario.cnpj?.takeIf(String::isNotBlank)?.let {
                        Text("CNPJ ${Formatadores.formatarCnpj(it)}", color = TextoTerciario, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 3.dp))
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = FundoSuperficie,
            border = BorderStroke(1.dp, BordaPadrao),
        ) {
            TabRow(
                selectedTabIndex = abaPerfil,
                containerColor = Color.Transparent,
                contentColor = TextoPrincipal,
                indicator = { posicoes ->
                    if (abaPerfil < posicoes.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(posicoes[abaPerfil]),
                            height = 3.dp,
                            color = RoxoPrincipal,
                        )
                    }
                },
                divider = { HorizontalDivider(color = DivisorPadrao) },
            ) {
                Tab(
                    selected = abaPerfil == 0,
                    onClick = { abaPerfil = 0 },
                    text = {
                        Text(
                            text = "Dados da Instituição",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (abaPerfil == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (abaPerfil == 0) RoxoClaro else TextoSecundario,
                        )
                    },
                )
                Tab(
                    selected = abaPerfil == 1,
                    onClick = { abaPerfil = 1 },
                    text = {
                        Text(
                            text = "Endereço e Sede",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (abaPerfil == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (abaPerfil == 1) RoxoClaro else TextoSecundario,
                        )
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (abaPerfil == 0) {
            PerfilOngEditor(token)
        } else {
            EnderecoOng(token)
        }
    }
}

// -------------------------------------------------------------------------------------------------
// LOGO SOS+
// -------------------------------------------------------------------------------------------------

@Composable
fun LogoInicio(modifier: Modifier = Modifier) {
    SosLogo(modifier = modifier)
}
