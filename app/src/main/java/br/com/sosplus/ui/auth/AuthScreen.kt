package br.com.sosplus.ui.auth

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.sosplus.ui.theme.SosTheme

private val FundoAplicativo = Color(0xFF111020)
private val FundoPainel = Color(0xFF090E1B)
private val FundoCampo = Color(0xFF11182A)
private val BordaCampo = Color(0xFF303950)
private val RoxoPrincipal = Color(0xFF803DF1)
private val RoxoClaro = Color(0xFF9C64FF)
private val TextoPrincipal = Color(0xFFF8F7FF)
private val TextoSecundario = Color(0xFFAAA8BD)
private val CorErro = Color(0xFFFF8E9B)
private val CorSucesso = Color(0xFF62DFA4)

private enum class DestinoAutenticacao {
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
    // Controla qual tela local está visível enquanto não há navegação por rotas.
    var destino by rememberSaveable { mutableStateOf(DestinoAutenticacao.Login) }
    var perfilSelecionado by rememberSaveable { mutableStateOf(PerfilUsuario.Doador) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = FundoAplicativo,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { scaffoldPadding ->
        when (destino) {
            DestinoAutenticacao.Login -> RotaLogin(
                modifier = Modifier.padding(scaffoldPadding),
                perfilSelecionado = perfilSelecionado,
                aoAlterarPerfil = { perfilSelecionado = it },
                aoCriarConta = { destino = DestinoAutenticacao.Cadastro },
                aoConcluirLogin = { perfil ->
                    destino = if (perfil == PerfilUsuario.Doador) {
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
            )

            DestinoAutenticacao.InicioDoador -> RotaInicioDoador(
                modifier = Modifier.padding(scaffoldPadding),
                aoSair = { destino = DestinoAutenticacao.Login },
            )

            DestinoAutenticacao.InicioOng -> RotaInicioOng(
                modifier = Modifier.padding(scaffoldPadding),
                aoSair = { destino = DestinoAutenticacao.Login },
            )
        }
    }
}

@Composable
private fun RotaLogin(
    perfilSelecionado: PerfilUsuario,
    aoAlterarPerfil: (PerfilUsuario) -> Unit,
    aoCriarConta: () -> Unit,
    aoConcluirLogin: (PerfilUsuario) -> Unit,
    modifier: Modifier = Modifier,
) {
    var usuario by rememberSaveable { mutableStateOf("") }
    var senha by rememberSaveable { mutableStateOf("") }
    var senhaVisivel by rememberSaveable { mutableStateOf(false) }
    var lembrarUsuario by rememberSaveable { mutableStateOf(false) }
    var retorno by remember { mutableStateOf<RetornoAutenticacao?>(null) }

    TelaLogin(
        perfilSelecionado = perfilSelecionado,
        usuario = usuario,
        senha = senha,
        senhaVisivel = senhaVisivel,
        lembrarUsuario = lembrarUsuario,
        retorno = retorno,
        aoAlterarPerfil = {
            aoAlterarPerfil(it)
            usuario = ""
            senha = ""
            retorno = null
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
        aoRecuperarSenha = {
            retorno = if (perfilSelecionado == PerfilUsuario.Doador) {
                RetornoAutenticacao("Acesso de demonstração: usuário admin e senha 1234.")
            } else {
                RetornoAutenticacao("Acesso de demonstração: usuário ong e senha 1234.")
            }
        },
        aoEntrar = {
            retorno = when {
                usuario.isBlank() || senha.isBlank() -> {
                    RetornoAutenticacao("Preencha o usuário e a senha.")
                }

                credenciaisSaoValidas(perfilSelecionado, usuario, senha) -> {
                    aoConcluirLogin(perfilSelecionado)
                    null
                }

                else -> RetornoAutenticacao("Acesso incorreto para o perfil selecionado.")
            }
        },
        aoCriarConta = aoCriarConta,
        modifier = modifier,
    )
}

@Composable
private fun RotaCadastro(
    perfilSelecionado: PerfilUsuario,
    aoAlterarPerfil: (PerfilUsuario) -> Unit,
    aoVoltarLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var nome by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var senha by rememberSaveable { mutableStateOf("") }
    var confirmacao by rememberSaveable { mutableStateOf("") }
    var senhaVisivel by rememberSaveable { mutableStateOf(false) }
    var retorno by remember { mutableStateOf<RetornoAutenticacao?>(null) }

    TelaCadastro(
        perfilSelecionado = perfilSelecionado,
        nome = nome,
        email = email,
        senha = senha,
        confirmacao = confirmacao,
        senhaVisivel = senhaVisivel,
        retorno = retorno,
        aoAlterarPerfil = {
            aoAlterarPerfil(it)
            nome = ""
            email = ""
            senha = ""
            confirmacao = ""
            retorno = null
        },
        aoAlterarNome = {
            nome = it
            retorno = null
        },
        aoAlterarEmail = {
            email = it
            retorno = null
        },
        aoAlterarSenha = {
            senha = it
            retorno = null
        },
        aoAlterarConfirmacao = {
            confirmacao = it
            retorno = null
        },
        aoAlternarVisibilidadeSenha = { senhaVisivel = !senhaVisivel },
        aoCriarConta = {
            retorno = validarCadastro(
                perfil = perfilSelecionado,
                nome = nome,
                email = email,
                senha = senha,
                confirmacao = confirmacao,
            )
        },
        aoVoltarLogin = aoVoltarLogin,
        modifier = modifier,
    )
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
    aoEntrar: () -> Unit,
    aoCriarConta: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EstruturaTelaAutenticacao(
        cabecalhoCompacto = false,
        modifier = modifier,
    ) { tecladoVisivel ->
        Text(
            text = "Bem-vindo de volta",
            color = TextoPrincipal,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        if (!tecladoVisivel) {
            Text(
                text = "Escolha seu perfil para continuar no SOS+.",
                color = TextoSecundario,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 5.dp),
            )

            Spacer(modifier = Modifier.height(12.dp))

            SeletorPerfil(
                perfilSelecionado = perfilSelecionado,
                aoAlterarPerfil = aoAlterarPerfil,
            )
        }

        Spacer(modifier = Modifier.height(if (tecladoVisivel) 7.dp else 12.dp))

        CampoTextoSos(
            valor = usuario,
            aoAlterarValor = aoAlterarUsuario,
            rotulo = if (perfilSelecionado == PerfilUsuario.Doador) {
                "Usuário"
            } else {
                "CNPJ ou e-mail institucional"
            },
            textoOrientativo = if (perfilSelecionado == PerfilUsuario.Doador) {
                "Digite seu usuário"
            } else {
                "Digite o acesso da ONG"
            },
            simboloPrincipal = if (perfilSelecionado == PerfilUsuario.Doador) "@" else "O",
        )
        Spacer(modifier = Modifier.height(9.dp))
        CampoSenhaSos(
            valor = senha,
            aoAlterarValor = aoAlterarSenha,
            rotulo = "Senha",
            textoOrientativo = "Digite sua senha",
            senhaVisivel = senhaVisivel,
            aoAlternarVisibilidadeSenha = aoAlternarVisibilidadeSenha,
        )

        if (!tecladoVisivel) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 42.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                        text = "Lembrar de mim",
                        color = TextoSecundario,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                TextButton(onClick = aoRecuperarSenha) {
                    Text(
                        text = "Esqueci minha senha",
                        color = RoxoClaro,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }

        MensagemRetorno(retorno = retorno)

        if (!tecladoVisivel) {
            Text(
                text = if (perfilSelecionado == PerfilUsuario.Doador) {
                    "Teste: admin / 1234"
                } else {
                    "Teste: ong / 1234"
                },
                color = TextoSecundario.copy(alpha = 0.82f),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(bottom = 7.dp),
            )
        }

        Button(
            onClick = aoEntrar,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RoxoPrincipal),
        ) {
            Text(
                text = if (perfilSelecionado == PerfilUsuario.Doador) {
                    "Entrar como doador"
                } else {
                    "Entrar como ONG"
                },
                fontWeight = FontWeight.SemiBold,
            )
        }

        if (!tecladoVisivel) {
            OutlinedButton(
                onClick = aoCriarConta,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, RoxoClaro),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = RoxoClaro),
            ) {
                Text(
                    text = if (perfilSelecionado == PerfilUsuario.Doador) {
                        "Criar conta de doador"
                    } else {
                        "Criar conta de ONG"
                    },
                    fontWeight = FontWeight.SemiBold,
                )
            }

            TextoTermos(modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun TelaCadastro(
    perfilSelecionado: PerfilUsuario,
    nome: String,
    email: String,
    senha: String,
    confirmacao: String,
    senhaVisivel: Boolean,
    retorno: RetornoAutenticacao?,
    aoAlterarPerfil: (PerfilUsuario) -> Unit,
    aoAlterarNome: (String) -> Unit,
    aoAlterarEmail: (String) -> Unit,
    aoAlterarSenha: (String) -> Unit,
    aoAlterarConfirmacao: (String) -> Unit,
    aoAlternarVisibilidadeSenha: () -> Unit,
    aoCriarConta: () -> Unit,
    aoVoltarLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EstruturaTelaAutenticacao(
        cabecalhoCompacto = true,
        modifier = modifier,
    ) { tecladoVisivel ->
        Text(
            text = if (perfilSelecionado == PerfilUsuario.Doador) {
                "Crie sua conta"
            } else {
                "Cadastre sua ONG"
            },
            color = TextoPrincipal,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        if (!tecladoVisivel) {
            Text(
                text = if (perfilSelecionado == PerfilUsuario.Doador) {
                    "Encontre ONGs e campanhas perto de você."
                } else {
                    "Divulgue campanhas e necessidades para sua comunidade."
                },
                color = TextoSecundario,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 5.dp),
            )

            Spacer(modifier = Modifier.height(12.dp))

            SeletorPerfil(
                perfilSelecionado = perfilSelecionado,
                aoAlterarPerfil = aoAlterarPerfil,
            )
        }

        Spacer(modifier = Modifier.height(if (tecladoVisivel) 7.dp else 12.dp))

        CampoTextoSos(
            valor = nome,
            aoAlterarValor = aoAlterarNome,
            rotulo = if (perfilSelecionado == PerfilUsuario.Doador) "Nome completo" else "Nome da ONG",
            textoOrientativo = if (perfilSelecionado == PerfilUsuario.Doador) {
                "Como podemos chamar você?"
            } else {
                "Digite o nome da organização"
            },
            simboloPrincipal = if (perfilSelecionado == PerfilUsuario.Doador) "N" else "O",
        )
        Spacer(modifier = Modifier.height(8.dp))
        CampoTextoSos(
            valor = email,
            aoAlterarValor = aoAlterarEmail,
            rotulo = if (perfilSelecionado == PerfilUsuario.Doador) "E-mail" else "E-mail institucional",
            textoOrientativo = if (perfilSelecionado == PerfilUsuario.Doador) {
                "voce@exemplo.com"
            } else {
                "contato@ong.org.br"
            },
            simboloPrincipal = "@",
            keyboardType = KeyboardType.Email,
        )
        Spacer(modifier = Modifier.height(8.dp))
        CampoSenhaSos(
            valor = senha,
            aoAlterarValor = aoAlterarSenha,
            rotulo = "Senha",
            textoOrientativo = "Mínimo de 6 caracteres",
            senhaVisivel = senhaVisivel,
            aoAlternarVisibilidadeSenha = aoAlternarVisibilidadeSenha,
        )
        Spacer(modifier = Modifier.height(8.dp))
        CampoSenhaSos(
            valor = confirmacao,
            aoAlterarValor = aoAlterarConfirmacao,
            rotulo = "Confirmar senha",
            textoOrientativo = "Digite a senha novamente",
            senhaVisivel = senhaVisivel,
            aoAlternarVisibilidadeSenha = aoAlternarVisibilidadeSenha,
        )

        MensagemRetorno(retorno = retorno)

        Button(
            onClick = aoCriarConta,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RoxoPrincipal),
        ) {
            Text(
                text = if (perfilSelecionado == PerfilUsuario.Doador) {
                    "Criar minha conta"
                } else {
                    "Cadastrar ONG"
                },
                fontWeight = FontWeight.SemiBold,
            )
        }

        if (!tecladoVisivel) {
            TextButton(
                onClick = aoVoltarLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 42.dp),
            ) {
                Text(text = "Já tenho uma conta — entrar", color = RoxoClaro)
            }

            TextoTermos(modifier = Modifier.padding(top = 5.dp))
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
        OpcaoPerfil(
            titulo = "Doador",
            subtitulo = "Quero ajudar",
            simbolo = "♥",
            perfil = PerfilUsuario.Doador,
            selecionado = perfilSelecionado == PerfilUsuario.Doador,
            onClick = { aoAlterarPerfil(PerfilUsuario.Doador) },
            modifier = Modifier.weight(1f),
        )
        OpcaoPerfil(
            titulo = "ONG",
            subtitulo = "Represento uma ONG",
            simbolo = "⌂",
            perfil = PerfilUsuario.Ong,
            selecionado = perfilSelecionado == PerfilUsuario.Ong,
            onClick = { aoAlterarPerfil(PerfilUsuario.Ong) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun OpcaoPerfil(
    titulo: String,
    subtitulo: String,
    simbolo: String,
    perfil: PerfilUsuario,
    selecionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val transicaoInfinita = rememberInfiniteTransition(label = "icone-perfil-$titulo")
    val escalaCartaoSelecionado by animateFloatAsState(
        targetValue = if (selecionado) 1.015f else 1f,
        animationSpec = tween(durationMillis = 220),
        label = "escala-cartao-perfil-$titulo",
    )
    val escalaCoracao by transicaoInfinita.animateFloat(
        initialValue = 1f,
        targetValue = if (selecionado && perfil == PerfilUsuario.Doador) 1.18f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 720),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulso-coracao",
    )
    val rotacaoOng by transicaoInfinita.animateFloat(
        initialValue = 0f,
        targetValue = if (selecionado && perfil == PerfilUsuario.Ong) 8f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 950),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "balanco-ong",
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 62.dp)
            .graphicsLayer {
                scaleX = escalaCartaoSelecionado
                scaleY = escalaCartaoSelecionado
            },
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (selecionado) RoxoClaro else BordaCampo,
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selecionado) {
                RoxoPrincipal.copy(alpha = 0.22f)
            } else {
                FundoCampo
            },
            contentColor = TextoPrincipal,
        ),
        contentPadding = PaddingValues(horizontal = 9.dp, vertical = 7.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = simbolo,
                color = RoxoClaro,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.graphicsLayer {
                    scaleX = if (perfil == PerfilUsuario.Doador) escalaCoracao else 1f
                    scaleY = if (perfil == PerfilUsuario.Doador) escalaCoracao else 1f
                    rotationZ = if (perfil == PerfilUsuario.Ong) rotacaoOng else 0f
                },
            )
            Column {
                Text(
                    text = titulo,
                    color = TextoPrincipal,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = subtitulo,
                    color = TextoSecundario,
                    style = MaterialTheme.typography.labelSmall,
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
            .background(FundoAplicativo)
            .imePadding(),
    ) {
        // Com o teclado aberto, o cabeçalho some para priorizar os campos.
        if (!tecladoVisivel) {
            CabecalhoPrincipal(compacto = cabecalhoCompacto)
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .offset(y = if (tecladoVisivel) 0.dp else (-22).dp),
            color = FundoPainel,
            shape = RoundedCornerShape(
                topStart = if (tecladoVisivel) 0.dp else 28.dp,
                topEnd = if (tecladoVisivel) 0.dp else 28.dp,
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(
                        horizontal = 20.dp,
                        vertical = if (tecladoVisivel) 12.dp else 18.dp,
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
    val alturaCabecalho = if (compacto) 180.dp else 245.dp
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(alturaCabecalho)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF12113C),
                        Color(0xFF291172),
                        Color(0xFF461694),
                    ),
                    start = Offset.Zero,
                    end = Offset(900f, 900f),
                ),
            )
            .statusBarsPadding()
            .padding(horizontal = 22.dp, vertical = 14.dp),
    ) {
        LogoSos(modifier = Modifier.align(Alignment.TopStart))

        if (!compacto) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(0.52f)
                    .padding(bottom = 9.dp),
            ) {
                Text(
                    text = "Conectando pessoas.",
                    color = Color(0xFFD6D0EC),
                    fontSize = 16.sp,
                    lineHeight = 19.sp,
                    maxLines = 1,
                )
                Text(
                    text = "Transformando\ncomunidades.",
                    color = Color.White,
                    fontSize = 18.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }

        IlustracaoComunidade(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth(if (compacto) 0.54f else 0.56f)
                .height(if (compacto) 118.dp else 148.dp),
        )
    }
}

@Composable
private fun LogoSos(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(modifier = Modifier.size(38.dp)) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(width = 38.dp, height = 14.dp)
                    .background(RoxoClaro, RoundedCornerShape(7.dp)),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(width = 14.dp, height = 38.dp)
                    .background(RoxoClaro, RoundedCornerShape(7.dp)),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(9.dp)
                    .background(Color(0xFFFF6478), RoundedCornerShape(50)),
            )
        }
        Text(
            text = "SOS+",
            color = Color.White,
            fontSize = 31.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun IlustracaoComunidade(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val corLinha = Color(0xFFB790FF).copy(alpha = 0.42f)
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
        drawCircle(Color(0xFFFF6478), radius = 8f, center = Offset(size.width * 0.91f, size.height * 0.22f))

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
            brush = Brush.verticalGradient(listOf(Color(0xFFFF8A8F), Color(0xFFFF5068))),
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
            strokeWidth = 12f,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = corPessoaClara,
            start = Offset(size.width * 0.73f, size.height * 0.70f),
            end = Offset(size.width * 0.64f, size.height * 0.56f),
            strokeWidth = 12f,
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
private fun CampoTextoSos(
    valor: String,
    aoAlterarValor: (String) -> Unit,
    rotulo: String,
    textoOrientativo: String,
    simboloPrincipal: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    val fonteInteracao = remember { MutableInteractionSource() }
    val emFoco by fonteInteracao.collectIsFocusedAsState()
    val escalaCampo by animateFloatAsState(
        targetValue = if (emFoco) 1.01f else 1f,
        animationSpec = tween(durationMillis = 180),
        label = "escala-campo-$rotulo",
    )
    val deslocamentoCampo by animateFloatAsState(
        targetValue = if (emFoco) -2f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "deslocamento-campo-$rotulo",
    )
    val escalaSimbolo by animateFloatAsState(
        targetValue = if (emFoco) 1.16f else 1f,
        animationSpec = tween(durationMillis = 180),
        label = "escala-simbolo-$rotulo",
    )

    OutlinedTextField(
        value = valor,
        onValueChange = aoAlterarValor,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = escalaCampo
                scaleY = escalaCampo
                translationY = deslocamentoCampo
                shadowElevation = if (emFoco) 8f else 0f
                shape = RoundedCornerShape(14.dp)
            },
        label = { Text(rotulo) },
        placeholder = { Text(textoOrientativo) },
        leadingIcon = {
            Text(
                text = simboloPrincipal,
                color = RoxoClaro,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.graphicsLayer {
                    scaleX = escalaSimbolo
                    scaleY = escalaSimbolo
                },
            )
        },
        interactionSource = fonteInteracao,
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = coresCampoTextoSos(),
    )
}

@Composable
private fun CampoSenhaSos(
    valor: String,
    aoAlterarValor: (String) -> Unit,
    rotulo: String,
    textoOrientativo: String,
    senhaVisivel: Boolean,
    aoAlternarVisibilidadeSenha: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fonteInteracao = remember { MutableInteractionSource() }
    val emFoco by fonteInteracao.collectIsFocusedAsState()
    val escalaCampo by animateFloatAsState(
        targetValue = if (emFoco) 1.01f else 1f,
        animationSpec = tween(durationMillis = 180),
        label = "escala-campo-senha-$rotulo",
    )
    val deslocamentoCampo by animateFloatAsState(
        targetValue = if (emFoco) -2f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "deslocamento-campo-senha-$rotulo",
    )
    val escalaSimbolo by animateFloatAsState(
        targetValue = if (emFoco) 1.16f else 1f,
        animationSpec = tween(durationMillis = 180),
        label = "escala-simbolo-senha-$rotulo",
    )

    OutlinedTextField(
        value = valor,
        onValueChange = aoAlterarValor,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = escalaCampo
                scaleY = escalaCampo
                translationY = deslocamentoCampo
                shadowElevation = if (emFoco) 8f else 0f
                shape = RoundedCornerShape(14.dp)
            },
        label = { Text(rotulo) },
        placeholder = { Text(textoOrientativo) },
        leadingIcon = {
            Text(
                text = "●",
                color = RoxoClaro,
                fontSize = 12.sp,
                modifier = Modifier.graphicsLayer {
                    scaleX = escalaSimbolo
                    scaleY = escalaSimbolo
                },
            )
        },
        trailingIcon = {
            TextButton(
                onClick = aoAlternarVisibilidadeSenha,
                contentPadding = PaddingValues(horizontal = 8.dp),
            ) {
                Text(
                    text = if (senhaVisivel) "Ocultar" else "Mostrar",
                    color = RoxoClaro,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        },
        interactionSource = fonteInteracao,
        visualTransformation = if (senhaVisivel) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = coresCampoTextoSos(),
    )
}

@Composable
private fun coresCampoTextoSos() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextoPrincipal,
    unfocusedTextColor = TextoPrincipal,
    focusedContainerColor = FundoCampo,
    unfocusedContainerColor = FundoCampo,
    focusedBorderColor = RoxoClaro,
    unfocusedBorderColor = BordaCampo,
    focusedLabelColor = RoxoClaro,
    unfocusedLabelColor = TextoSecundario,
    cursorColor = RoxoClaro,
    focusedPlaceholderColor = TextoSecundario.copy(alpha = 0.7f),
    unfocusedPlaceholderColor = TextoSecundario.copy(alpha = 0.7f),
)

@Composable
private fun MensagemRetorno(
    retorno: RetornoAutenticacao?,
    modifier: Modifier = Modifier,
) {
    Text(
        text = retorno?.mensagem.orEmpty(),
        color = if (retorno?.sucesso == true) CorSucesso else CorErro,
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 28.dp)
            .padding(vertical = 5.dp),
    )
}

@Composable
private fun TextoTermos(modifier: Modifier = Modifier) {
    Text(
        text = "Ao continuar, você concorda com nossos Termos e Política de Privacidade.",
        color = TextoSecundario.copy(alpha = 0.75f),
        style = MaterialTheme.typography.labelSmall,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
    )
}

// Credenciais locais usadas somente enquanto o backend não está conectado.
internal fun credenciaisSaoValidas(
    perfil: PerfilUsuario,
    usuario: String,
    senha: String,
): Boolean {
    val usuarioEsperado = if (perfil == PerfilUsuario.Doador) "admin" else "ong"
    return usuario.trim().lowercase() == usuarioEsperado && senha == "1234"
}

private fun validarCadastro(
    perfil: PerfilUsuario,
    nome: String,
    email: String,
    senha: String,
    confirmacao: String,
): RetornoAutenticacao {
    return when {
        nome.isBlank() || email.isBlank() || senha.isBlank() || confirmacao.isBlank() -> {
            RetornoAutenticacao("Preencha todos os campos.")
        }

        !email.contains("@") || !email.substringAfter("@").contains(".") -> {
            RetornoAutenticacao("Digite um e-mail válido.")
        }

        senha.length < 6 -> RetornoAutenticacao("A senha deve ter pelo menos 6 caracteres.")
        senha != confirmacao -> RetornoAutenticacao("As senhas não são iguais.")
        else -> RetornoAutenticacao(
            mensagem = if (perfil == PerfilUsuario.Doador) {
                "Conta criada com sucesso! Você já pode apoiar ONGs."
            } else {
                "ONG cadastrada com sucesso! Você já pode divulgar campanhas."
            },
            sucesso = true,
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviaTelaLogin() {
    SosTheme(darkTheme = true, dynamicColor = false) {
        TelaLogin(
            perfilSelecionado = PerfilUsuario.Doador,
            usuario = "",
            senha = "",
            senhaVisivel = false,
            lembrarUsuario = false,
            retorno = null,
            aoAlterarPerfil = {},
            aoAlterarUsuario = {},
            aoAlterarSenha = {},
            aoAlternarVisibilidadeSenha = {},
            aoAlterarLembrarUsuario = {},
            aoRecuperarSenha = {},
            aoEntrar = {},
            aoCriarConta = {},
        )
    }
}
