package repositorio_captura

import dominio_captura.Captura
import org.apache.commons.dbcp2.BasicDataSource
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import java.time.LocalDateTime

class CapturaRepositorio {

    lateinit var jdbcTemplate: JdbcTemplate

    fun configurar(){
        val datasource = BasicDataSource()
        datasource.driverClassName = "com.mysql.cj.jdbc.Driver"
        datasource.url = "jdbc:mysql://localhost:3306/ServGuard"
        datasource.username = "seu_usuario"
        datasource.password = "sua_senha"

        jdbcTemplate = JdbcTemplate(datasource)
    }

    fun criarTabela(){
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS ServGuard.dominio_captura.Captura(
            IdCaptura INT NOT NULL AUTO_INCREMENT,
            fkMaquinaRecurso INT NOT NULL,
            registro DECIMAL(8,3) NOT NULL,
            dthCriacao DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
            
            CONSTRAINT fkMaquinaRecursoCaptura FOREIGN KEY (fkMaquinaRecurso) REFERENCES ServGuard.dominio_maquina_recurso.MaquinaRecurso(idMaquinaRecurso),
            PRIMARY KEY (idCaptura)
            )
        """.trimIndent())

    }

    fun existePorMac(mac: String): Boolean{
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Maquina WHERE MACAddress = ?",
            Int::class.java,
            mac
        ) > 0
    }

    fun buscaIdPorMac(mac: String): Int{
        return jdbcTemplate.queryForObject("SELECT idMaquina FROM Maquina WHERE MACAddress = ?",
            Int::class.java,
            mac)
    }

    fun inserirBytesEnviados(mac: String, bytesEnviados: Long) {

        val captura =  Captura()
        captura.setfkMaquinaRecurso(buscaIdPorMac(mac))  // Busca a ID da máquina pelo MAC
        captura.setRegistro(bytesEnviados.toDouble()) // Convertendo Long para Double
        captura.setDTHCriacao(LocalDateTime.now())  // Data e hora atual

        inserir(captura) // Chama a função existente para inserir
    }

    fun inserirBytesRecebidos(mac: String, bytesRecebidos: Long) {

        val captura =  Captura()
           captura.setfkMaquinaRecurso(buscaIdPorMac(mac))  // Busca a ID da máquina pelo MAC
           captura.setRegistro(bytesRecebidos.toDouble()) // Convertendo Long para Double
           captura.setDTHCriacao(LocalDateTime.now())  // Data e hora atual

        inserir(captura) // Chama a função existente para inserir
    }

    fun inserir(novoValor: Captura):Boolean{
        return jdbcTemplate.update("""
            INSERT INTO dominio_captura.Captura (fkMaquinaRecurso, registro, dthCriacao) VALUES (?,?,?)
        """,
           novoValor.getfkMaquinaRecurso(),
           novoValor.getRegistro(),
           novoValor.getDTHCriacao()
            ) > 0
    }

    fun listar():List<Captura>{
        return jdbcTemplate.query("SELECT * FROM dominio_captura.Captura", BeanPropertyRowMapper(Captura::class.java))
    }

    fun existePorId(id: Int):Boolean{
        return jdbcTemplate.queryForObject("SELECET COUNT(*) FROM dominio_captura.Captura WHERE id = ?",
            Int::class.java,
            id
            ) > 0

    }

    fun buscarPorid(id: Int): Captura? {
        return jdbcTemplate.queryForObject("SELECT * FROM dominio_captura.Captura WHERE id = ?",
            BeanPropertyRowMapper(Captura::class.java),
            id
            )
    }

    fun deletarPorId(id: Int):Boolean{
        return jdbcTemplate.update("DELETE FROM dominio_captura.Captura WHERE id = ?",
            id
            ) > 0

    }

    fun atualizarPorId(id:Int, capturaParaAtualizar: Captura):Boolean{
        return jdbcTemplate.update("""
            UPDATE dominio_captura.Captura SET fkMaquinaRecurso = ?, registro = ?, dthCriacao = ?
        """,
            capturaParaAtualizar.getfkMaquinaRecurso(),
            capturaParaAtualizar.getRegistro(),
            capturaParaAtualizar.getDTHCriacao(),
            id
            ) > 0
    }

}


