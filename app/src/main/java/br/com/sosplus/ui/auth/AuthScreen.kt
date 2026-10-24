package br.com.sosplus.ui.auth

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import br.com.sosplus.data.AuthApiException
import br.com.sosplus.data.AuthRepository
import br.com.sosplus.data.Sessao
import br.com.sosplus.data.SessionEvents
import br.com.sosplus.ui.components.BannerAlerta
import br.com.sosplus.ui.components.Formatadores
import br.com.sosplus.ui.components.SosBotaoPrimario
import br.com.sosplus.ui.components.SosBotaoSecundario
import br.com.sosplus.ui.components.SosBotaoTexto
import br.com.sosplus.ui.components.SosCampoSenha
import br.com.sosplus.ui.components.SosCampoTexto
import br.com.sosplus.ui.components.SosLogo
import br.com.sosplus.ui.components.TipoAlerta
import br.com.sosplus.ui.theme.BordaFocada
import br.com.sosplus.ui.theme.BordaPadrao
import br.com.sosplus.ui.theme.CoralAcento
import br.com.sosplus.ui.theme.FundoApp
import br.com.sosplus.ui.theme.FundoCampo
import br.com.sosplus.ui.theme.FundoSuperficie
import br.com.sosplus.ui.theme.FundoSuperficieElevada
import br.com.sosplus.ui.theme.RoxoClaro
import br.com.sosplus.ui.theme.RoxoContainer
import br.com.sosplus.ui.theme.RoxoPrincipal
import br.com.sosplus.ui.theme.TextoDesabilitado
import br.com.sosplus.ui.theme.TextoPrincipal
import br.com.sosplus.ui.theme.TextoSecundario
import br.com.sosplus.ui.theme.TextoTerciario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.io.IOException

enum class DestinoAutenticacao {
    Login,
    Cadastro,
    InicioDoador,
    InicioOng,
}

enum class PerfilUsuario {
    Doador,
    Ong,
}

data class RetornoAutenticacao(
    val mensagem: String,
    val sucesso: Boolean = false,
)

@Composable
fun AplicativoSos(modifier: Modifier = Modifier) {
    // A sessão fica apenas em memória; nenhuma senha ou token é gravado no dispositivo.
    var destino by rememberSaveable { mutableStateOf(DestinoAutenticacao.Login) }
    var perfilSelecionado by rememberSaveable { mutableStateOf(PerfilUsuario.Doador) }
    var emailPreenchido by rememberSaveable { mutableStateOf("") }
    var sessao by remember { mutableStateOf<Sessao?>(null) }
    var fotoPerfil by remember { mutableStateOf<ByteArray?>(null) }
    var avisoLogin by rememberSaveable { mutableStateOf<String?>(null) }
    var tipoAvisoLogin by rememberSaveable { mutableStateOf(TipoAlerta.Info) }

    val repository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(sessao?.token) {
        SessionEvents.expiradas.collect { token ->
            if (sessao?.token == token) {
                avisoLogin = "Sua sessão expirou. Entre novamente."
                tipoAvisoLogin = TipoAlerta.Aviso
                sessao = null
                fotoPerfil = null
                destino = DestinoAutenticacao.Login
            }
        }
    }

    val sair: () -> Unit = {
        avisoLogin = null
        val token = sessao?.token
        sessao = null
        fotoPerfil = null
        destino = DestinoAutenticacao.Login
        if (token != null) scope.launch {
            try { repository.sair(token) }
            catch (error: CancellationException) { throw error }
            catch (_: IOException) { /* A sessão local já foi encerrada; a remota expira em 24 horas. */ }
        }
    }

    AtualizarLocalizacaoDaSessao(
        sessao = sessao,
        repository = repository,
        aoAtualizarUsuario = { token, usuarioAtualizado ->
            sessao?.takeIf { it.token == token }?.let { sessaoAtual ->
                sessao = sessaoAtual.copy(usuario = usuarioAtualizado)
            }
        },
    )

    LaunchedEffect(sessao?.token) {
        val sessaoAtual = sessao
        fotoPerfil = if (sessaoAtual == null) {
            null
        } else {
            try {
                repository.carregarFotoPerfil(sessaoAtual.token)
            } catch (error: CancellationException) {
                throw error
            } catch (_: IOException) {
                null
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = FundoApp,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { scaffoldPadding ->
        when (destino) {
            DestinoAutenticacao.Login -> RotaLogin(
                aviso = avisoLogin,
                tipoAviso = tipoAvisoLogin,
                emailInicial = emailPreenchido,
                modifier = Modifier.padding(scaffoldPadding),
                perfilSelecionado = perfilSelecionado,
                aoAlterarPerfil = { perfilSelecionado = it },
                aoCriarConta = { destino = DestinoAutenticacao.Cadastro },
                aoConcluirLogin = { autenticacao ->
                    avisoLogin = null
                    sessao = autenticacao
                    destino = if (autenticacao.usuario.tipo == "DOADOR") {
                        DestinoAutenticacao.InicioDoador
                    } else {
                        DestinoAutenticacao.InicioOng
                    }
                },
            )

            DestinoAutenticacao.Cadastro -> RotaCadastro(
                modifier = Modifier.padding(scaffoldPadding),
                perfilSelecionado = perfilSelecionado,
                aoAlterarPerfil = { perfilSelecionado = it },
                aoVoltarLogin = { destino = DestinoAutenticacao.Login },
                aoConcluirCadastro = { emailCadastrado, perfilCadastrado ->
                    emailPreenchido = emailCadastrado
                    perfilSelecionado = perfilCadastrado
                    avisoLogin = "Conta criada com sucesso! Faça login para continuar."
                    tipoAvisoLogin = TipoAlerta.Sucesso
                    destino = DestinoAutenticacao.Login
                },
            )

            DestinoAutenticacao.InicioDoador -> RotaInicioDoador(
                token = requireNotNull(sessao).token,
                usuario = requireNotNull(sessao).usuario,
                fotoPerfil = fotoPerfil,
                aoSalvarFotoPerfil = { conteudo, mimeType ->
                    val token = requireNotNull(sessao).token
                    repository.atualizarFotoPerfil(token, conteudo, mimeType)
                    if (sessao?.token == token) fotoPerfil = conteudo
                },
                aoRedefinirSenha = { senhaAtual, novaSenha, confirmacao ->
                    repository.atualizarSenha(
                        requireNotNull(sessao).token,
                        senhaAtual,
                        novaSenha,
                        confirmacao,
                    )
                },
                aoCarregarDadosPessoais = {
                    repository.carregarDadosPessoais(requireNotNull(sessao).token)
                },
                aoSalvarDadosPessoais = { dados ->
                    val token = requireNotNull(sessao).token
                    val atualizacao = repository.atualizarDadosPessoais(token, dados)
                    sessao?.takeIf { it.token == token }?.let { atual ->
                        sessao = atual.copy(usuario = atualizacao.usuario)
                    }
                    atualizacao.dados
                },
                aoCarregarDoacoes = {
                    val sessaoAtual = requireNotNull(sessao)
                    repository.carregarDoacoes(sessaoAtual.token, sessaoAtual.usuario.id)
                },
                modifier = Modifier.padding(scaffoldPadding),
                aoSair = sair,
            )

            DestinoAutenticacao.InicioOng -> RotaInicioOng(
                token = requireNotNull(sessao).token,
                usuario = requireNotNull(sessao).usuario,
                modifier = Modifier.padding(scaffoldPadding),
                aoSair = sair,
            )
        }
    }
}

@Composable
private fun AtualizarLocalizacaoDaSessao(
    sessao: Sessao?,
    repository: AuthRepository,
    aoAtualizarUsuario: (token: String, usuario: br.com.sosplus.data.UsuarioAutenticado) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun atualizar(sessaoAtual: Sessao) {
        scope.launch {
            try {
                val localizacao = obterLocalizacaoAtualDaRede(context) ?: return@launch
                val usuarioAtualizado = repository.atualizarLocalizacao(sessaoAtual.token, localizacao)
                aoAtualizarUsuario(sessaoAtual.token, usuarioAtualizado)
            } catch (error: CancellationException) {
                throw error
            } catch (_: IOException) {
                // A autenticação continua funcionando mesmo se a localização ou a API
                // estiverem indisponíveis; a localização já salva permanece como padrão.
            }
        }
    }

    val solicitarPermissao = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { permitido ->
        if (permitido) sessao?.let(::atualizar)
    }

    LaunchedEffect(sessao?.token) {
        val sessaoAtual = sessao ?: return@LaunchedEffect
        val permitido = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        if (permitido) atualizar(sessaoAtual)
        else solicitarPermissao.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
    }
}

@Composable
private fun RotaLogin(
    aviso: String? = null,
    tipoAviso: TipoAlerta = TipoAlerta.Info,
    emailInicial: String = "",
    perfilSelecionado: PerfilUsuario,
    aoAlterarPerfil: (PerfilUsuario) -> Unit,
    aoCriarConta: () -> Unit,
    aoConcluirLogin: (Sessao) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("login_email", android.content.Context.MODE_PRIVATE) }
    var usuario by rememberSaveable {
        mutableStateOf(emailInicial.ifBlank { preferences.getString("email", "").orEmpty() })
    }
    var senha by remember { mutableStateOf("") }
    var senhaVisivel by rememberSaveable { mutableStateOf(false) }
    var lembrarUsuario by rememberSaveable { mutableStateOf(preferences.contains("email")) }
    var recuperandoSenha by remember { mutableStateOf(false) }
    var retorno by remember(aviso) { mutableStateOf(aviso?.let { RetornoAutenticacao(it, sucesso = tipoAviso == TipoAlerta.Sucesso) }) }
    var carregando by remember { mutableStateOf(false) }
    var exibirDialogoTermos by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val repository = remember { AuthRepository() }

    LaunchedEffect(emailInicial) {
        if (emailInicial.isNotBlank()) {
            usuario = emailInicial
        }
    }

    TelaLogin(
        perfilSelecionado = perfilSelecionado,
        usuario = usuario,
        senha = senha,
        senhaVisivel = senhaVisivel,
        lembrarUsuario = lembrarUsuario,
        retorno = retorno,
        carregando = carregando,
        aoAlterarPerfil = {
            if (!carregando) {
                aoAlterarPerfil(it)
                retorno = null
            }
        },
        aoAlterarUsuario = {
            usuario = it
            retorno = null
        },
        aoAlterarSenha = {
            senha = it
            retorno = null
        },
        aoAlternarVisibilidadeSenha = { senhaVisivel = !senhaVisivel },
        aoAlterarLembrarUsuario = { lembrarUsuario = it },
        aoRecuperarSenha = { recuperandoSenha = true },
        aoExibirTermos = { exibirDialogoTermos = true },
        aoEntrar = {
            if (!carregando) {
                if (usuario.isBlank() || senha.isBlank()) {
                    retorno = RetornoAutenticacao("Preencha o e-mail e a senha.")
                } else {
                    carregando = true
                    retorno = null
                    val emailEnviado = usuario.trim()
                    val senhaEnviada = senha
                    val tipo = if (perfilSelecionado == PerfilUsuario.Doador) "DOADOR" else "ONG"
                    scope.launch {
                        try {
                            val sessao = repository.entrar(emailEnviado, senhaEnviada, tipo)
                            preferences.edit().apply {
                                if (lembrarUsuario) putString("email", sessao.usuario.email) else remove("email")
                            }.apply()
                            senha = ""
                            aoConcluirLogin(sessao)
                        } catch (error: CancellationException) { throw error }
                        catch (error: IOException) { retorno = erroDaApi(error) }
                        finally { carregando = false }
                    }
                }
            }
        },
        aoCriarConta = { if (!carregando) aoCriarConta() },
        modifier = modifier,
    )

    if (recuperandoSenha) {
        RecuperarSenhaDialog(
            emailInicial = usuario,
            aoFechar = { recuperandoSenha = false },
            aoConcluir = {
                recuperandoSenha = false
                senha = ""
                retorno = RetornoAutenticacao("Senha atualizada! Entre com sua nova senha.", sucesso = true)
            },
        )
    }

    if (exibirDialogoTermos) {
        DialogoTermosEPrivacidade(aoFechar = { exibirDialogoTermos = false })
    }
}

@Composable
private fun RotaCadastro(
    perfilSelecionado: PerfilUsuario,
    aoAlterarPerfil: (PerfilUsuario) -> Unit,
    aoVoltarLogin: () -> Unit,
    aoConcluirCadastro: (email: String, perfil: PerfilUsuario) -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler { aoVoltarLogin() }

    var nome by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var cnpj by rememberSaveable { mutableStateOf("") }
    var cpf by rememberSaveable { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confirmacao by remember { mutableStateOf("") }
    var senhaVisivel by rememberSaveable { mutableStateOf(false) }
    var retorno by remember { mutableStateOf<RetornoAutenticacao?>(null) }
    var carregando by remember { mutableStateOf(false) }
    var exibirDialogoTermos by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val repository = remember { AuthRepository() }

    TelaCadastro(
        perfilSelecionado = perfilSelecionado,
        nome = nome,
        email = email,
        cnpj = cnpj,
        cpf = cpf,
        senha = senha,
        confirmacao = confirmacao,
        senhaVisivel = senhaVisivel,
        retorno = retorno,
        carregando = carregando,
        aoAlterarPerfil = {
            if (!carregando) {
                aoAlterarPerfil(it)
                nome = ""
                email = ""
                senha = ""
                confirmacao = ""
                cnpj = ""
                cpf = ""
                retorno = null
            }
        },
        aoAlterarCnpj = { cnpj = Formatadores.mascaraCnpj(it); retorno = null },
        aoAlterarCpf = { cpf = Formatadores.mascaraCpf(it); retorno = null },
        aoAlterarNome = { nome = it; retorno = null },
        aoAlterarEmail = { email = it; retorno = null },
        aoAlterarSenha = { senha = it; retorno = null },
        aoAlterarConfirmacao = { confirmacao = it; retorno = null },
        aoAlternarVisibilidadeSenha = { senhaVisivel = !senhaVisivel },
        aoExibirTermos = { exibirDialogoTermos = true },
        aoCriarConta = {
            if (!carregando) {
                val validacao = validarCadastro(perfilSelecionado, nome, email, senha, confirmacao, cnpj, cpf)
                retorno = if (validacao.sucesso) null else validacao
                if (validacao.sucesso) {
                    carregando = true
                    val tipo = if (perfilSelecionado == PerfilUsuario.Doador) "DOADOR" else "ONG"
                    scope.launch {
                        try {
                            repository.cadastrar(nome, email, senha, confirmacao, tipo, cnpj, cpf)
                            aoConcluirCadastro(email, perfilSelecionado)
                        } catch (error: CancellationException) { throw error }
                        catch (error: IOException) { retorno = erroDaApi(error) }
                        finally { carregando = false }
                    }
                }
            }
        },
        aoVoltarLogin = { if (!carregando) aoVoltarLogin() },
        modifier = modifier,
    )

    if (exibirDialogoTermos) {
        DialogoTermosEPrivacidade(aoFechar = { exibirDialogoTermos = false })
    }
}

@Composable
private fun TelaLogin(
    perfilSelecionado: PerfilUsuario,
    usuario: String,
    senha: String,
    senhaVisivel: Boolean,
    lembrarUsuario: Boolean,
    retorno: RetornoAutenticacao?,
    aoAlterarPerfil: (PerfilUsuario) -> Unit,
    aoAlterarUsuario: (String) -> Unit,
    aoAlterarSenha: (String) -> Unit,
    aoAlternarVisibilidadeSenha: () -> Unit,
    aoAlterarLembrarUsuario: (Boolean) -> Unit,
    aoRecuperarSenha: () -> Unit,
    aoExibirTermos: () -> Unit,
    aoEntrar: () -> Unit,
    aoCriarConta: () -> Unit,
    carregando: Boolean = false,
    modifier: Modifier = Modifier,
) {
    EstruturaTelaAutenticacao(
        cabecalhoCompacto = false,
        modifier = modifier,
    ) { tecladoVisivel ->
        Text(
            text = "Bem-vindo de volta",
            color = TextoPrincipal,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        if (!tecladoVisivel) {
            Text(
                text = "Escolha seu perfil para continuar no SOS+.",
                color = TextoSecundario,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
            )

            Spacer(modifier = Modifier.height(14.dp))

            SeletorPerfil(
                perfilSelecionado = perfilSelecionado,
                aoAlterarPerfil = aoAlterarPerfil,
            )
        }

        Spacer(modifier = Modifier.height(if (tecladoVisivel) 8.dp else 16.dp))

        SosCampoTexto(
            valor = usuario,
            aoAlterarValor = aoAlterarUsuario,
            rotulo = "E-mail",
            placeholder = "Digite seu e-mail cadastrado",
            iconeInicio = Icons.Default.Email,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            enabled = !carregando,
        )

        Spacer(modifier = Modifier.height(12.dp))

        SosCampoSenha(
            valor = senha,
            aoAlterarValor = aoAlterarSenha,
            rotulo = "Senha",
            placeholder = "Digite sua senha",
            senhaVisivel = senhaVisivel,
            aoAlternarVisibilidade = aoAlternarVisibilidadeSenha,
            enabled = !carregando,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { aoAlterarLembrarUsuario(!lembrarUsuario) }
            ) {
                Checkbox(
                    checked = lembrarUsuario,
                    onCheckedChange = aoAlterarLembrarUsuario,
                    colors = CheckboxDefaults.colors(
                        checkedColor = RoxoPrincipal,
                        uncheckedColor = TextoSecundario,
                        checkmarkColor = Color.White,
                    ),
                )
                Text(
                    text = "Lembrar e-mail",
                    color = TextoSecundario,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            TextButton(
                onClick = aoRecuperarSenha,
                modifier = Modifier.heightIn(min = 48.dp),
            ) {
                Text(
                    text = "Esqueci minha senha",
                    color = RoxoClaro,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        if (retorno != null) {
            BannerAlerta(
                mensagem = retorno.mensagem,
                tipo = if (retorno.sucesso) TipoAlerta.Sucesso else TipoAlerta.Erro,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }

        SosBotaoPrimario(
            texto = if (carregando) "Entrando..." else if (perfilSelecionado == PerfilUsuario.Doador) {
                "Entrar como doador"
            } else {
                "Entrar como ONG"
            },
            onClick = aoEntrar,
            carregando = carregando,
        )

        if (!tecladoVisivel) {
            Spacer(modifier = Modifier.height(10.dp))

            SosBotaoSecundario(
                texto = if (perfilSelecionado == PerfilUsuario.Doador) {
                    "Criar conta de doador"
                } else {
                    "Criar conta de ONG"
                },
                onClick = aoCriarConta,
                enabled = !carregando,
            )

            Spacer(modifier = Modifier.height(14.dp))

            TextoTermos(aoClicarTermos = aoExibirTermos)
        }
    }
}

@Composable
private fun TelaCadastro(
    perfilSelecionado: PerfilUsuario,
    nome: String,
    email: String,
    cnpj: String,
    cpf: String,
    senha: String,
    confirmacao: String,
    senhaVisivel: Boolean,
    retorno: RetornoAutenticacao?,
    aoAlterarPerfil: (PerfilUsuario) -> Unit,
    aoAlterarNome: (String) -> Unit,
    aoAlterarEmail: (String) -> Unit,
    aoAlterarCnpj: (String) -> Unit,
    aoAlterarCpf: (String) -> Unit,
    aoAlterarSenha: (String) -> Unit,
    aoAlterarConfirmacao: (String) -> Unit,
    aoAlternarVisibilidadeSenha: () -> Unit,
    aoExibirTermos: () -> Unit,
    aoCriarConta: () -> Unit,
    aoVoltarLogin: () -> Unit,
    carregando: Boolean = false,
    modifier: Modifier = Modifier,
) {
    EstruturaTelaAutenticacao(
        cabecalhoCompacto = true,
        modifier = modifier,
    ) { tecladoVisivel ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = if (perfilSelecionado == PerfilUsuario.Doador) "Crie sua conta" else "Cadastre sua ONG",
                color = TextoPrincipal,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            IconButton(onClick = aoVoltarLogin) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar para o login",
                    tint = TextoSecundario,
                )
            }
        }

        if (!tecladoVisivel) {
            Text(
                text = if (perfilSelecionado == PerfilUsuario.Doador) {
                    "Encontre organizações e apoie causas na sua região."
                } else {
                    "Divulgue campanhas e receba doações para sua comunidade."
                },
                color = TextoSecundario,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 2.dp),
            )

            Spacer(modifier = Modifier.height(14.dp))

            SeletorPerfil(
                perfilSelecionado = perfilSelecionado,
                aoAlterarPerfil = aoAlterarPerfil,
            )
        }

        Spacer(modifier = Modifier.height(if (tecladoVisivel) 8.dp else 14.dp))

        SosCampoTexto(
            valor = nome,
            aoAlterarValor = aoAlterarNome,
            rotulo = if (perfilSelecionado == PerfilUsuario.Doador) "Nome completo" else "Nome da organização",
            placeholder = if (perfilSelecionado == PerfilUsuario.Doador) "Como podemos chamar você?" else "Nome oficial da ONG",
            iconeInicio = Icons.Default.Person,
            enabled = !carregando,
        )

        Spacer(modifier = Modifier.height(10.dp))

        SosCampoTexto(
            valor = email,
            aoAlterarValor = aoAlterarEmail,
            rotulo = if (perfilSelecionado == PerfilUsuario.Doador) "E-mail" else "E-mail institucional",
            placeholder = if (perfilSelecionado == PerfilUsuario.Doador) "voce@exemplo.com" else "contato@ong.org.br",
            iconeInicio = Icons.Default.Email,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            enabled = !carregando,
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (perfilSelecionado == PerfilUsuario.Ong) {
            SosCampoTexto(
                valor = cnpj,
                aoAlterarValor = aoAlterarCnpj,
                rotulo = "CNPJ da organização",
                placeholder = "00.000.000/0000-00",
                iconeInicio = Icons.Default.Badge,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                dica = "Informe os 14 dígitos do CNPJ",
                enabled = !carregando,
            )
        } else {
            SosCampoTexto(
                valor = cpf,
                aoAlterarValor = aoAlterarCpf,
                rotulo = "CPF (opcional)",
                placeholder = "000.000.000-00",
                iconeInicio = Icons.Default.Badge,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                dica = "Opcional para recibos de doação",
                enabled = !carregando,
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        SosCampoSenha(
            valor = senha,
            aoAlterarValor = aoAlterarSenha,
            rotulo = "Senha",
            placeholder = "Mínimo de 8 caracteres",
            senhaVisivel = senhaVisivel,
            aoAlternarVisibilidade = aoAlternarVisibilidadeSenha,
            dica = if (senha.isNotEmpty() && senha.length < 8) "Faltam ${8 - senha.length} caracteres" else null,
            enabled = !carregando,
        )

        Spacer(modifier = Modifier.height(10.dp))

        val confirmacaoIncorreta = confirmacao.isNotEmpty() && confirmacao != senha
        SosCampoSenha(
            valor = confirmacao,
            aoAlterarValor = aoAlterarConfirmacao,
            rotulo = "Confirmar senha",
            placeholder = "Repita a senha",
            senhaVisivel = senhaVisivel,
            aoAlternarVisibilidade = aoAlternarVisibilidadeSenha,
            erro = if (confirmacaoIncorreta) "As senhas não coincidem" else null,
            enabled = !carregando,
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (retorno != null) {
            BannerAlerta(
                mensagem = retorno.mensagem,
                tipo = if (retorno.sucesso) TipoAlerta.Sucesso else TipoAlerta.Erro,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }

        SosBotaoPrimario(
            texto = if (carregando) "Criando conta..." else if (perfilSelecionado == PerfilUsuario.Doador) {
                "Criar minha conta"
            } else {
                "Cadastrar ONG"
            },
            onClick = aoCriarConta,
            carregando = carregando,
        )

        if (!tecladoVisivel) {
            Spacer(modifier = Modifier.height(8.dp))

            SosBotaoTexto(
                texto = "Já tenho uma conta — entrar",
                onClick = aoVoltarLogin,
                modifier = Modifier.fillMaxWidth(),
            )

            TextoTermos(aoClicarTermos = aoExibirTermos)
        }
    }
}

@Composable
private fun SeletorPerfil(
    perfilSelecionado: PerfilUsuario,
    aoAlterarPerfil: (PerfilUsuario) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        OpcaoPerfilCard(
            titulo = "Doador",
            subtitulo = "Quero ajudar causas",
            icone = Icons.Default.VolunteerActivism,
            selecionado = perfilSelecionado == PerfilUsuario.Doador,
            onClick = { aoAlterarPerfil(PerfilUsuario.Doador) },
            modifier = Modifier.weight(1f),
        )
        OpcaoPerfilCard(
            titulo = "ONG",
            subtitulo = "Represento uma ONG",
            icone = Icons.Default.CorporateFare,
            selecionado = perfilSelecionado == PerfilUsuario.Ong,
            onClick = { aoAlterarPerfil(PerfilUsuario.Ong) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun OpcaoPerfilCard(
    titulo: String,
    subtitulo: String,
    icone: ImageVector,
    selecionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val corBorda by animateColorAsState(
        targetValue = if (selecionado) RoxoClaro else BordaPadrao,
        animationSpec = tween(180),
        label = "borda-perfil",
    )
    val corFundo by animateColorAsState(
        targetValue = if (selecionado) RoxoContainer.copy(alpha = 0.5f) else FundoCampo,
        animationSpec = tween(180),
        label = "fundo-perfil",
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .heightIn(min = 68.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, corBorda),
        color = corFundo,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (selecionado) RoxoPrincipal.copy(alpha = 0.3f) else FundoSuperficieElevada,
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icone,
                    contentDescription = null,
                    tint = if (selecionado) RoxoClaro else TextoSecundario,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column {
                Text(
                    text = titulo,
                    color = TextoPrincipal,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (selecionado) FontWeight.Bold else FontWeight.SemiBold,
                )
                Text(
                    text = subtitulo,
                    color = TextoSecundario,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun EstruturaTelaAutenticacao(
    cabecalhoCompacto: Boolean,
    modifier: Modifier = Modifier,
    conteudo: @Composable ColumnScope.(tecladoVisivel: Boolean) -> Unit,
) {
    val densidade = LocalDensity.current
    val tecladoVisivel = WindowInsets.ime.getBottom(densidade) > 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FundoApp)
            .imePadding(),
    ) {
        if (!tecladoVisivel) {
            CabecalhoPrincipal(compacto = cabecalhoCompacto)
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .offset(y = if (tecladoVisivel) 0.dp else (-22).dp),
            color = FundoSuperficie,
            shape = RoundedCornerShape(
                topStart = if (tecladoVisivel) 0.dp else 28.dp,
                topEnd = if (tecladoVisivel) 0.dp else 28.dp,
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(
                        horizontal = 22.dp,
                        vertical = if (tecladoVisivel) 14.dp else 22.dp,
                    ),
            ) {
                conteudo(tecladoVisivel)
            }
        }
    }
}

@Composable
private fun CabecalhoPrincipal(
    compacto: Boolean,
    modifier: Modifier = Modifier,
) {
    val alturaCabecalho = if (compacto) 175.dp else 235.dp
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(alturaCabecalho)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0F0E2A),
                        Color(0xFF231057),
                        Color(0xFF431688),
                    ),
                    start = Offset.Zero,
                    end = Offset(900f, 900f),
                ),
            )
            .statusBarsPadding()
            .padding(horizontal = 22.dp, vertical = 14.dp),
    ) {
        SosLogo(
            modifier = Modifier.align(Alignment.TopStart),
            tamanhoIcone = 36.dp,
            tamanhoTextoSp = 28,
        )

        if (!compacto) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(0.54f)
                    .padding(bottom = 10.dp),
            ) {
                Text(
                    text = "Conectando pessoas.",
                    color = Color(0xFFD6D0EC),
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    maxLines = 1,
                )
                Text(
                    text = "Transformando comunidades.",
                    color = Color.White,
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }

        IlustracaoComunidade(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth(if (compacto) 0.52f else 0.54f)
                .height(if (compacto) 115.dp else 140.dp),
        )
    }
}

@Composable
private fun IlustracaoComunidade(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val corLinha = Color(0xFFB790FF).copy(alpha = 0.35f)
        val corPessoa = Color(0xFF9A5BF2)
        val corPessoaClara = Color(0xFFB790FF)

        drawLine(
            color = corLinha,
            start = Offset(size.width * 0.08f, size.height * 0.46f),
            end = Offset(size.width * 0.91f, size.height * 0.22f),
            strokeWidth = 3f,
            cap = StrokeCap.Round,
        )
        drawCircle(corPessoaClara, radius = 8f, center = Offset(size.width * 0.08f, size.height * 0.46f))
        drawCircle(CoralAcento, radius = 8f, center = Offset(size.width * 0.91f, size.height * 0.22f))

        val centroCoracao = Offset(size.width * 0.56f, size.height * 0.42f)
        val coracao = Path().apply {
            moveTo(centroCoracao.x, centroCoracao.y + 34f)
            cubicTo(
                centroCoracao.x - 60f,
                centroCoracao.y - 2f,
                centroCoracao.x - 38f,
                centroCoracao.y - 48f,
                centroCoracao.x,
                centroCoracao.y - 18f,
            )
            cubicTo(
                centroCoracao.x + 38f,
                centroCoracao.y - 48f,
                centroCoracao.x + 60f,
                centroCoracao.y - 2f,
                centroCoracao.x,
                centroCoracao.y + 34f,
            )
            close()
        }
        drawPath(
            path = coracao,
            brush = Brush.verticalGradient(listOf(CoralAcento, CoralAcento)),
        )

        drawCircle(
            color = corPessoaClara,
            radius = size.minDimension * 0.09f,
            center = Offset(size.width * 0.23f, size.height * 0.63f),
        )
        drawOval(
            color = corPessoa,
            topLeft = Offset(size.width * 0.04f, size.height * 0.72f),
            size = Size(size.width * 0.35f, size.height * 0.42f),
        )
        drawCircle(
            color = corPessoaClara,
            radius = size.minDimension * 0.09f,
            center = Offset(size.width * 0.82f, size.height * 0.58f),
        )
        drawOval(
            color = corPessoa,
            topLeft = Offset(size.width * 0.65f, size.height * 0.68f),
            size = Size(size.width * 0.34f, size.height * 0.45f),
        )
        drawLine(
            color = corPessoaClara,
            start = Offset(size.width * 0.31f, size.height * 0.73f),
            end = Offset(size.width * 0.48f, size.height * 0.57f),
            strokeWidth = 10f,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = corPessoaClara,
            start = Offset(size.width * 0.73f, size.height * 0.70f),
            end = Offset(size.width * 0.64f, size.height * 0.56f),
            strokeWidth = 10f,
            cap = StrokeCap.Round,
        )
        drawCircle(
            color = corLinha,
            radius = size.minDimension * 0.46f,
            center = centroCoracao,
            style = Stroke(width = 2f),
        )
    }
}

@Composable
private fun TextoTermos(
    aoClicarTermos: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = aoClicarTermos,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = "Ao continuar, você concorda com nossos Termos e Privacidade",
            color = TextoTerciario,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun DialogoTermosEPrivacidade(aoFechar: () -> Unit) {
    AlertDialog(
        onDismissRequest = aoFechar,
        shape = RoundedCornerShape(20.dp),
        containerColor = FundoSuperficieElevada,
        title = {
            Text(
                text = "Termos e Privacidade",
                style = MaterialTheme.typography.titleLarge,
                color = TextoPrincipal,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Termos de Uso do SOS+",
                    style = MaterialTheme.typography.titleSmall,
                    color = RoxoClaro,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "O SOS+ é uma plataforma solidária sem fins lucrativos que conecta cidadãos dispostos a ajudar organizações sociais locais.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Privacidade e Proteção de Dados",
                    style = MaterialTheme.typography.titleSmall,
                    color = RoxoClaro,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Seus dados de cadastro e localização são utilizados exclusivamente para identificar ONGs próximas e validar intenções de apoio. As senhas são criptografadas e nenhuma informação financeira real é armazenada.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario,
                )
            }
        },
        confirmButton = {
            SosBotaoPrimario(
                texto = "Entendi",
                onClick = aoFechar,
                modifier = Modifier.width(120.dp),
            )
        },
    )
}

private fun erroDaApi(error: IOException): RetornoAutenticacao = RetornoAutenticacao(
    if (error is AuthApiException) error.message.orEmpty()
    else "Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente."
)
