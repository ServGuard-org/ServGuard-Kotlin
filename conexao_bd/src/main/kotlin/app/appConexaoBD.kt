package app

import com.github.britooo.looca.api.core.Looca
import dominio_captura.Captura
import repositorio_captura.CapturaRepositorio
import java.time.LocalDateTime
import dominio_maquina.Maquina
import dominio_maquina_recurso.MaquinaRecurso
import repositorio_maquina.MaquinaRepositorio
import repositorio_maquina_recurso.MaquinaRecursoRepositorio
import repositorio_recurso.RecursoRepositorio

open class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {

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
                    println("\nMáquina cadastrada com sucesso! Capturando dados para o MAC: ${intRede.enderecoMac} ")
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

    val looca = Looca()
    val maquinaRepositorio = MaquinaRepositorio()
    val capturaRepositorio = CapturaRepositorio()
    val maquinaRecursoRepositorio = MaquinaRecursoRepositorio()
    val recursoRepositorio = RecursoRepositorio()

    maquinaRepositorio.configurar()
    capturaRepositorio.configurar()
    maquinaRecursoRepositorio.configurar()
    recursoRepositorio.configurar()

    // Buscando o id da máquina pelo MAC
    val idMaquina: Int = maquinaRepositorio.buscaIdPorMac(mac)
    println("ID da máquina encontrada para o MAC $mac: $idMaquina")

    // ID do Recurso (fixo ou obtido de algum lugar)
    val idRecurso = 1 // Exemplo: ID do recurso correspondente ao uso de rede
    println("ID do Recurso: $idRecurso")

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
    var contador: Int = -1

    for (idRecurso in listaIdRecursos) {
        contador += 1
        var idMaquinaRecurso = maquinaRecursoRepositorio.buscarIdMaquinaRecurso(idRecurso, idMaquina)
        if (idMaquinaRecurso == null) { // recurso repositorio
            println("IdMaquinaRecurso não encontrado! Encerrado operação.")
            return
        }
        listaIdMaquinaRecursos.add(idMaquinaRecurso)
        println("ID máquina recurso encontrada: $idMaquinaRecurso, para o recurso: ${listaNomesRecursos[contador]}, de id: $idRecurso")
    }


    while (true) {

        val idMaquinaRecurso = listaIdMaquinaRecursos

        val indicePacotesEnviados: Int = listaNomesRecursos.indexOf("pacotesEnviados")
        val indicePacotesRecebidos: Int = listaNomesRecursos.indexOf("pacotesRecebidos")
        val indicebytesEnviados: Int = listaNomesRecursos.indexOf("bytesEnviados")
        val indicebytesRecebidos: Int = listaNomesRecursos.indexOf("bytesRecebidos")


        // Capturando os bytes recebidos
        val interfacesRede = looca.rede.grupoDeInterfaces.interfaces
        var bytesRecebidosTotais: Long = 0

        for (intRede in interfacesRede) {

            val bytesRecebidos: Long = intRede.bytesRecebidos
            bytesRecebidosTotais += bytesRecebidos

        }

        val recebidosMB = bytesRecebidosTotais / (1024 * 1024)
        println("Bytes recebidos: $recebidosMB MB")

        // Inserindo os dados dos BytesRecebidos no Banco
        capturaRepositorio.inserirBytesRecebidos(indicebytesRecebidos,recebidosMB)
        println("Dados de bytes recebidos estão sendo inseridos no banco de dados ID MaquinaRecurso: $idMaquinaRecurso")

        // Capturando os bytes enviados
        var bytesEnviadosTotais: Long = 0

        for (intRede in interfacesRede){

            val bytesEnviados: Long = intRede.bytesEnviados
            bytesEnviadosTotais += bytesEnviados
        }

        val enviadosMB = bytesEnviadosTotais / (1024 * 1024)
        println("Bytes enviados: $enviadosMB MB")

        capturaRepositorio.inserirBytesEnviados(indicebytesEnviados, enviadosMB)
        println("Dados de bytes enviados inseridos no banco para ID MaquinaRecurso: $idMaquinaRecurso")

        // Capturando os pacotes recebidos
        var pacotesRecebidosTotais: Long = 0

        for (intRede in interfacesRede){

            val pacotesRecebidos: Long = intRede.pacotesRecebidos
            pacotesRecebidosTotais += pacotesRecebidos
        }

        val pacotesRecebidosMB = pacotesRecebidosTotais / (1024*1024)
        println("Pacotes recebidos: $pacotesRecebidosMB MB")
        capturaRepositorio.inserirPacotesRecebidos(indicePacotesRecebidos, pacotesRecebidosMB)

        // Capturando os pacotes enviados
        var pacotesEnviadosTotais: Long = 0

        for (intRede in interfacesRede){

            val pacotesEnviados: Long = intRede.pacotesEnviados
            pacotesEnviadosTotais += pacotesEnviados
        }

        val pacotesEnviadosMB = pacotesEnviadosTotais / (1024*1024)
        println("Pacotes enviados: $pacotesEnviadosMB MB")

        capturaRepositorio.inserirPacotesEnviados(indicePacotesEnviados, pacotesEnviadosMB)

        // Os dados serão capturados a cada 30 segundos
        Thread.sleep(30000)

    }

}
