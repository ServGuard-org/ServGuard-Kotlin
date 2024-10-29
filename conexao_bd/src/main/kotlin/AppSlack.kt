import dominio_slack.Slack
import io.github.cdimascio.dotenv.dotenv
import org.json.JSONObject

fun main() {

    val slack = Slack("https://hooks.slack.com/services/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")
    val dotenv = dotenv {
        directory = "conexao_bd/src/main/kotlin/"
        ignoreIfMalformed = true
        ignoreIfMissing = true
    }
    print("jdbc:mysql://${dotenv["DB_HOST"]}:${dotenv["DB_PORT"]}/${dotenv["DATABASE"]}")
    val mensagem = JSONObject().apply {
        put("text", "Teste")
    }

    val maquina_recurso = repositorio_maquina_recurso.MaquinaRecursoRepositorio()
    maquina_recurso.configurar()
    println(maquina_recurso.obterAlerta(2,1))

    slack.enviar(mensagem)


}