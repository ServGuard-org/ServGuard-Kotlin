import dominio_slack.Slack
import org.json.JSONObject

fun main() {

    val slack = Slack("https://hooks.slack.com/services/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")

    val mensagem = JSONObject().apply {
        put("text", "Teste")
    }

    val maquina_recurso = repositorio_maquina_recurso.MaquinaRecursoRepositorio()
    maquina_recurso.configurar()
    println(maquina_recurso.obterAlerta(2,1))

    slack.enviar(mensagem)


}