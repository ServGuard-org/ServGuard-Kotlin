package app

import com.github.britooo.looca.api.core.Looca
import dominio_captura.Captura
import repositorio_captura.CapturaRepositorio
import java.time.LocalDateTime
import dominio_maquina.Maquina
import repositorio_maquina.MaquinaRepositorio

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

    maquinaRepositorio.configurar()

    val idMaquina: Int = maquinaRepositorio.buscaIdPorMac(mac)

    while (true) {

        val interfacesRede = looca.rede.grupoDeInterfaces.interfaces
        var bytesRecebidosTotais: Long = 0

        for (intRede in interfacesRede) {

            val bytesRecebidos: Long = intRede.bytesRecebidos
            bytesRecebidosTotais += bytesRecebidos


        }

        val recebidosMB = bytesRecebidosTotais / (1024 * 1024)

        // falta a logica para inserir no BD

        // 30 segundos
        Thread.sleep(30000)
    }

}
