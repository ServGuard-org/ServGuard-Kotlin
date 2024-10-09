package app

import com.github.britooo.looca.api.core.Looca
import dominio_captura.Captura
import repositorio_captura.CapturaRepositorio
import java.time.LocalDateTime
import dominio_maquina.Maquina
import repositorio_maquina.MaquinaRepositorio
import repositorio_maquina_recurso.MaquinaRecursoRepositorio

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

            for (intRede in interfacesRede) {
                if(maquinaRepositorio.existePorMac(intRede.enderecoMac)) {
                    maquinaCadastrada = true
                    capturarDados(intRede.enderecoMac)
                    break
                } else {
                    listaMacsNaoCadastrados.add(intRede.enderecoMac)
                }
            }

            if (!maquinaCadastrada) {
                for (mac in listaMacsNaoCadastrados) {
                    println("Máquina de MAC Address: ${mac} não cadastrada! Verifique se isto é um erro, ou se ainda não foi cadastrada via Python")
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

    maquinaRepositorio.configurar()
    capturaRepositorio.configurar()

    // Buscando o id da máquina pelo MAC
    val idMaquina: Int = maquinaRepositorio.buscaIdPorMac(mac)

    // ID do Recurso (fixo ou obtido de algum lugar)
    val idRecurso = 1 // Exemplo: ID do recurso correspondente ao uso de rede

    // Verificar se o IdMaquinaRecurso existe
    if (!maquinaRepositorio.existePorId(idRecurso)) {
        println("Recurso não encontrado!")
        return
    }

    // Verificar se o idMaquinaRecurso com base no IdRecurso e IdMaquina
    val idMaquinaRecurso = maquinaRecursoRepositorio.buscarIdMaquinaRecurso(idMaquina, idRecurso)

    // Validando se foi encontrado
    if (idMaquinaRecurso == null) {
        println("ID MaquinaRecurso não encontrado!")
        return
    }

    while (true) {

        // Capturando os bytes recebidos
        val interfacesRede = looca.rede.grupoDeInterfaces.interfaces
        var bytesRecebidosTotais: Long = 0

        for (intRede in interfacesRede) {

            val bytesRecebidos: Long = intRede.bytesRecebidos
            bytesRecebidosTotais += bytesRecebidos


        }

        val recebidosMB = bytesRecebidosTotais / (1024 * 1024)

        // Inserindo os dados dos BytesRecebidos no Banco
        capturaRepositorio.inserirBytesRecebidos(idMaquinaRecurso.toString(), recebidosMB.toLong())

        // Capturando os bytes enviados
        var bytesEnviadosTotais: Long = 0

        for (intRede in interfacesRede){

            val bytesEnviados: Long = intRede.bytesEnviados
            bytesEnviadosTotais += bytesEnviados
        }

        val enviadosMB = bytesEnviadosTotais / (1024 * 1024)

        capturaRepositorio.inserirBytesEnviados(idMaquinaRecurso.toString(), enviadosMB)

        // Os dados serão capturados a cada 30 segundos
        Thread.sleep(30000)



        // falta a logica para inserir no BD
    }

}
