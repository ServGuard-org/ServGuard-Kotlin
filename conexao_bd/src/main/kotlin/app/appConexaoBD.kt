package app

import dominio_captura.Captura
import java.time.LocalDateTime
import dominio_maquina.Maquina
import dominio_maquina_recurso.MaquinaRecurso
import dominio_slack.Slack
import com.github.britooo.looca.api.core.Looca
import io.github.cdimascio.dotenv.dotenv
import repositorio_captura.CapturaRepositorio
import repositorio_maquina.MaquinaRepositorio
import repositorio_maquina_recurso.MaquinaRecursoRepositorio
import repositorio_recurso.RecursoRepositorio
import org.json.JSONObject

open class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {

            println("""
  █████████                                   █████████                                     █████
 ███░░░░░███                                 ███░░░░░███                                   ░░███ 
░███    ░░░   ██████  ████████  █████ █████ ███     ░░░  █████ ████  ██████   ████████   ███████ 
░░█████████  ███░░███░░███░░███░░███ ░░███ ░███         ░░███ ░███  ░░░░░███ ░░███░░███ ███░░███ 
 ░░░░░░░░███░███████  ░███ ░░░  ░███  ░███ ░███    █████ ░███ ░███   ███████  ░███ ░░░ ░███ ░███ 
 ███    ░███░███░░░   ░███      ░░███ ███  ░░███  ░░███  ░███ ░███  ███░░███  ░███     ░███ ░███ 
░░█████████ ░░██████  █████      ░░█████    ░░█████████  ░░████████░░████████ █████    ░░████████
 ░░░░░░░░░   ░░░░░░  ░░░░░        ░░░░░      ░░░░░░░░░    ░░░░░░░░  ░░░░░░░░ ░░░░░      ░░░░░░░░ 
            """.trimIndent())


            val maquinaRepositorio = MaquinaRepositorio()
            maquinaRepositorio.configurar()

            val looca = Looca()

            val interfacesRede = looca.rede.grupoDeInterfaces.interfaces

            val listaMacsNaoCadastrados = mutableListOf<String>()
            var maquinaCadastrada: Boolean = false

            println("""
                Iniciando verificação de máquinas!
                ==================================
            """.trimIndent())

            for (intRede in interfacesRede) {
                println("Verificando máquina com MAC Address: ${intRede.enderecoMac}")
                if(maquinaRepositorio.existePorMac(intRede.enderecoMac)) {
                    maquinaCadastrada = true
                    println("\nMáquina localizada! Capturando dados para o MAC Address: ${intRede.enderecoMac} ")
                    capturarDados(intRede.enderecoMac)
                    break
                } else {
                    listaMacsNaoCadastrados.add(intRede.enderecoMac)
                }
            }
            println("""
                ==================================
                Verificação Finalizada! Essas foram as máquinas encontradas:
            """.trimIndent())

            if (!maquinaCadastrada) {
                for (mac in listaMacsNaoCadastrados) {
                    println("Máquina de MAC Address: ${mac} não cadastrada! Verifique se isto é um erro, ou se ainda não foi cadastrada via Python!")
                }
            }

        }
    }
}

fun capturarDados(mac: String) {

    val dotenv = dotenv {
        directory = "conexao_bd/src/main/kotlin/"
        ignoreIfMalformed = true
        ignoreIfMissing = true
    }
    val looca = Looca()
    val maquinaRepositorio = MaquinaRepositorio()
    val capturaRepositorio = CapturaRepositorio()
    val maquinaRecursoRepositorio = MaquinaRecursoRepositorio()
    val recursoRepositorio = RecursoRepositorio()
    val slack = Slack(dotenv["URL_SLACK"])


    maquinaRepositorio.configurar()
    capturaRepositorio.configurar()
    maquinaRecursoRepositorio.configurar()
    recursoRepositorio.configurar()

    // Buscando o id da máquina pelo MAC
    val idMaquina: Int = maquinaRepositorio.buscaIdPorMac(mac)
    println("ID da máquina encontrada para o MAC Address: $mac, ID da máquina: $idMaquina\n\n\n")

    // Verificar se o IdMaquinaRecurso existe

    val listaNomesRecursos: List<String> = listOf<String>("megabytesEnviados", "megabytesRecebidos", "pacotesEnviados", "pacotesRecebidos")
    val listaIdRecursos: MutableList<Int> = mutableListOf<Int>()

    for (recurso in listaNomesRecursos) {
        var idDaVez = recursoRepositorio.buscarIdRecursoPorNome(recurso)
        if (idDaVez == null) { // recurso repositorio
            println("Recurso não encontrado! Encerrando operação.")
            return
        }
        listaIdRecursos.add(idDaVez)
    }

    val listaIdMaquinaRecursos: MutableList<Int> = mutableListOf()
    var contador: Int = 0

    for (idRecursoAtual in listaIdRecursos) {
        val idMaquinaRecurso = maquinaRecursoRepositorio.buscarIdMaquinaRecurso(idRecursoAtual, idMaquina)
        if (idMaquinaRecurso == null) { // recurso repositorio
            println("IdMaquinaRecurso não encontrado! Encerrado operação.")
            return
        }
        listaIdMaquinaRecursos.add(idMaquinaRecurso)
        contador ++
    }


    while (true) {
        val hostname: String = looca.rede.parametros.hostName
        val indicePacotesEnviados: Int = listaNomesRecursos.indexOf("pacotesEnviados")
        val indicePacotesRecebidos: Int = listaNomesRecursos.indexOf("pacotesRecebidos")
        val indicebytesEnviados: Int = listaNomesRecursos.indexOf("megabytesEnviados")
        val indicebytesRecebidos: Int = listaNomesRecursos.indexOf("megabytesRecebidos")

        val idMaquinaRecursoPCTEnviados = listaIdMaquinaRecursos[indicePacotesEnviados]
        val idMaquinaRecursoPCTRecebidos = listaIdMaquinaRecursos[indicePacotesRecebidos]
        val idMaquinaRecursoBytesEnviados = listaIdMaquinaRecursos[indicebytesEnviados]
        val idMaquinaRecursoBytesRecebidos = listaIdMaquinaRecursos[indicebytesRecebidos]

//        println("idMaquinaRecursoPCTEnviados: $idMaquinaRecursoPCTEnviados")
//        println("idMaquinaRecursoPCTRecebidos: $idMaquinaRecursoPCTRecebidos")
//        println("idMaquinaRecursoBytesEnviados: $idMaquinaRecursoBytesEnviados")
//        println("idMaquinaRecursoBytesRecebidos: $idMaquinaRecursoBytesRecebidos")


        // Capturando os bytes recebidos
        val interfacesRede = looca.rede.grupoDeInterfaces.interfaces
        var bytesRecebidosTotais: Long = 0

        for (intRede in interfacesRede) {

            val bytesRecebidos: Long = intRede.bytesRecebidos
            bytesRecebidosTotais += bytesRecebidos

        }
        val recebidosMB = bytesRecebidosTotais / (1024 * 1024)
        println("Bytes recebidos: $recebidosMB MB")

        val mensagem1 = JSONObject().apply {
            put("text", "Alerta! :rotating_light: \n\n BytesRecebidos da máquina:\n- id: $idMaquina \n- Hostname: $hostname \nChegou a: ${recebidosMB} MBs!!")
        }

        val maxBytesRecebidos = recursoRepositorio.buscarAlertaPorMaquina(idMaquinaRecursoBytesRecebidos)
        val isAlertaBytesRecebidos = maxBytesRecebidos?.let {
            if (recebidosMB >= it) {
                println("ALERTA!!!!!!!! BYTES RECEBIDOS CHEGOU A: $recebidosMB")
                slack.enviarMensagem(mensagem1)
                1
            } else {
                0
            }
        } ?: 0

        // Inserindo os dados dos BytesRecebidos no Banco
        capturaRepositorio.inserirBytesRecebidos(idMaquinaRecursoBytesRecebidos,recebidosMB, isAlertaBytesRecebidos)



        // Capturando os bytes enviados
        var bytesEnviadosTotais: Long = 0

        for (intRede in interfacesRede){

            val bytesEnviados: Long = intRede.bytesEnviados
            bytesEnviadosTotais += bytesEnviados
        }
        val enviadosMB = bytesEnviadosTotais / (1024 * 1024)
        println("Bytes enviados: $enviadosMB MB")

        val mensagem2 = JSONObject().apply {
            put("text", "Alerta! :rotating_light: \n\n BytesEnviados da máquina:\n- id: $idMaquina \n- Hostname: $hostname \nChegou a: ${enviadosMB} MBs!!")
        }

        val maxBytesEnviados = recursoRepositorio.buscarAlertaPorMaquina(idMaquinaRecursoBytesEnviados)
        val isAlertaBytesEnviados = maxBytesEnviados?.let {
            if (enviadosMB >= it) {
                println("ALERTA!!!!!!!! BYTES ENVIADOS CHEGOU A: $enviadosMB")
                slack.enviarMensagem(mensagem2)
                1
            } else {
                0
            }
        } ?: 0

        // Inserindo os dados dos BytesEnviados no Banco
        capturaRepositorio.inserirBytesEnviados(idMaquinaRecursoBytesEnviados, enviadosMB, isAlertaBytesEnviados)



        // Capturando os pacotes recebidos
        var pacotesRecebidosTotais: Long = 0

        for (intRede in interfacesRede){

            val pacotesRecebidos: Long = intRede.pacotesRecebidos
            pacotesRecebidosTotais += pacotesRecebidos
        }
        val pacotesRecebidos = pacotesRecebidosTotais
        println("Pacotes recebidos: $pacotesRecebidos")


        val mensagem3 = JSONObject().apply {
            put("text", "Alerta! :rotating_light: \n\n PacotesRecebidos da máquina:\n- id: $idMaquina \n- Hostname: $hostname \nChegou a: ${pacotesRecebidos} Pacotes!!")
        }

        val maxPacotesRecebidos = recursoRepositorio.buscarAlertaPorMaquina(idMaquinaRecursoPCTRecebidos)
        val isAlertaPacotesRecebidos = maxPacotesRecebidos?.let {
            if (pacotesRecebidos >= it) {
                println("ALERTA!!!!!!!! PACOTES RECEBIDOS CHEGOU A: $pacotesRecebidos")
                slack.enviarMensagem(mensagem3)
                1
            } else {
                0
            }
        } ?: 0

        // Inserindo os dados dos PacotesRecebidos no Banco
        capturaRepositorio.inserirPacotesRecebidos(idMaquinaRecursoPCTRecebidos, pacotesRecebidos, isAlertaPacotesRecebidos)



        // Capturando os pacotes enviados
        var pacotesEnviadosTotais: Long = 0

        for (intRede in interfacesRede){

            val pacotesEnviados: Long = intRede.pacotesEnviados
            pacotesEnviadosTotais += pacotesEnviados
        }
        val pacotesEnviados = pacotesEnviadosTotais
        println("Pacotes enviados: $pacotesEnviados")


        val mensagem4 = JSONObject().apply {
            put("text", "Alerta! :rotating_light: \n\n PacotesEnviados da máquina:\n- id: $idMaquina \n- Hostname: $hostname \nChegou a: ${pacotesEnviados} Pacotes!!")
        }

        val maxPacotesEnviados = recursoRepositorio.buscarAlertaPorMaquina(idMaquinaRecursoPCTEnviados)
        val isAlertaPacotesEnviados = maxPacotesEnviados?.let {
            if (pacotesEnviados >= it) {
                println("ALERTA!!!!!!!! PACOTES ENVIADOS CHEGOU A: $pacotesEnviados")
                slack.enviarMensagem(mensagem4)
                1
            } else {
                0
            }
        } ?: 0

        // Inserindo os dados dos PacotesEnviados no Banco
        capturaRepositorio.inserirPacotesEnviados(idMaquinaRecursoPCTEnviados, pacotesEnviados, isAlertaPacotesEnviados)

        // Os dados serão capturados a cada 30 segundos
        Thread.sleep(30000)

    }

}