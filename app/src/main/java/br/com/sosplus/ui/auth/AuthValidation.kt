package br.com.sosplus.ui.auth

internal fun cnpjValido(valor: String): Boolean {
    val cnpj = valor.trim().uppercase(java.util.Locale.ROOT).replace(Regex("[./-]"), "")
    if (!Regex("[A-Z0-9]{12}[0-9]{2}").matches(cnpj) || cnpj.toSet().size == 1) return false
    fun digito(base: String): Char {
        val soma = base.reversed().mapIndexed { indice, char -> (char.code - 48) * (indice % 8 + 2) }.sum()
        val resto = soma % 11
        return ('0'.code + if (resto < 2) 0 else 11 - resto).toChar()
    }
    val primeiro = digito(cnpj.take(12))
    return cnpj.takeLast(2) == "$primeiro${digito(cnpj.take(12) + primeiro)}"
}

internal fun validarCadastro(
    perfil: PerfilUsuario,
    nome: String,
    email: String,
    senha: String,
    confirmacao: String,
    cnpj: String = "",
): RetornoAutenticacao = when {
    nome.isBlank() || email.isBlank() || senha.isBlank() || confirmacao.isBlank() ||
        (perfil == PerfilUsuario.Ong && cnpj.isBlank()) -> RetornoAutenticacao("Preencha todos os campos obrigatórios.")
    nome.trim().length > (if (perfil == PerfilUsuario.Ong) 200 else 150) -> RetornoAutenticacao("O nome informado é muito longo.")
    email.trim().length > 255 || !Regex("[^\\s@]+@[^\\s@]+\\.[^\\s@]+").matches(email.trim()) -> RetornoAutenticacao("Digite um e-mail válido.")
    senha.length < 8 -> RetornoAutenticacao("A senha deve ter pelo menos 8 caracteres.")
    senha.length > 128 || confirmacao.length > 128 -> RetornoAutenticacao("A senha deve ter no máximo 128 caracteres.")
    senha != confirmacao -> RetornoAutenticacao("As senhas não são iguais.")
    perfil == PerfilUsuario.Ong && !cnpjValido(cnpj) -> RetornoAutenticacao("Digite um CNPJ válido.")
    else -> RetornoAutenticacao("", sucesso = true)
}
