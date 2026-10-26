package br.com.sosplus.ui.campanhas

/**
 * Status possíveis de uma campanha, espelhando o enum `status` da tabela
 * `campanhas` no banco (ver sosplus-server/src/database/migrations/009-create-campanhas.ts).
 */
enum class StatusCampanha(val rotulo: String) {
    RASCUNHO("Rascunho"),
    ATIVA("Ativa"),
    ENCERRADA("Encerrada"),
    CANCELADA("Cancelada"),
}

/**
 * Modelo de campanha usado nas telas de doador. Os campos seguem o MER do
 * SOSPlus (tabela `campanhas` + dados básicos da `perfis_ong` associada).
 *
 * As datas ficam como texto já formatado (dd/MM/yyyy) para evitar depender de
 * java.time, que exige desugaring para o minSdk atual do projeto.
 */
data class Campanha(
    val id: Long,
    val ongId: Long,
    val ongNome: String,
    val ongDescricao: String,
    val titulo: String,
    val descricao: String,
    val simbolo: String,
    val metaValor: Double,
    val arrecadado: Double,
    val dataInicioTexto: String,
    val dataFimTexto: String,
    val status: StatusCampanha,
)
