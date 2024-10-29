package repositorio_captura

import dominio_captura.Captura
import dominio_maquina_recurso.MaquinaRecurso
import io.github.cdimascio.dotenv.dotenv
import org.apache.commons.dbcp2.BasicDataSource
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import java.time.LocalDateTime

class CapturaRepositorio {

    lateinit var jdbcTemplate: JdbcTemplate

    fun configurar(){
        val dotenv = dotenv {
            directory = "conexao_bd/src/main/kotlin/"
            ignoreIfMalformed = true
            ignoreIfMissing = true
        }
        val datasource = BasicDataSource()
        datasource.driverClassName = "com.mysql.cj.jdbc.Driver"
        datasource.url = "jdbc:mysql://${dotenv["DB_HOST"]}:${dotenv["DB_PORT"]}/${dotenv["DATABASE"]}?serverTimezone=America/Sao_Paulo"
        datasource.username = dotenv["DB_USER"]
        datasource.password = dotenv["DB_PASSWORD"]

        jdbcTemplate = JdbcTemplate(datasource)
    }

    fun criarTabela(){
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS Captura(
            IdCaptura INT NOT NULL AUTO_INCREMENT,
            fkMaquinaRecurso INT NOT NULL,
            registro DECIMAL(8,3) NOT NULL,
            dthCriacao DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
            
            CONSTRAINT fkMaquinaRecursoCaptura FOREIGN KEY (fkMaquinaRecurso) REFERENCES MaquinaRecurso(idMaquinaRecurso),
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

    fun inserirBytesEnviados(idMaquinaRecurso: Int, megabytesEnviados: Long): Boolean {
        return jdbcTemplate.update("""
            INSERT INTO Captura (fkMaquinaRecurso, registro) VALUES (?,?)
        """,
            idMaquinaRecurso,
            megabytesEnviados,
        ) > 0
    }

    fun inserirBytesRecebidos(idMaquinaRecurso: Int, megabytesRecebidos: Long): Boolean {
        return jdbcTemplate.update("""
            INSERT INTO Captura (fkMaquinaRecurso, registro) VALUES (?,?)
        """,
            idMaquinaRecurso,
            megabytesRecebidos,
        ) > 0
    }

    fun inserirPacotesRecebidos(idMaquinaRecurso: Int, pacotesRecebidos: Long): Boolean {
        return jdbcTemplate.update("""
            INSERT INTO Captura (fkMaquinaRecurso, registro) VALUES (?,?)
        """,
            idMaquinaRecurso,
            pacotesRecebidos,
        ) > 0
    }

    fun inserirPacotesEnviados(idMaquinaRecurso: Int, pacotesEnviados: Long): Boolean {
        return jdbcTemplate.update("""
            INSERT INTO Captura (fkMaquinaRecurso, registro) VALUES (?,?)
        """,
            idMaquinaRecurso,
            pacotesEnviados,
        ) > 0
    }

    fun listar():List<Captura>{
        return jdbcTemplate.query("SELECT * FROM Captura", BeanPropertyRowMapper(Captura::class.java))
    }

    fun existePorId(id: Int):Boolean{
        return jdbcTemplate.queryForObject("SELECET COUNT(*) FROM Captura WHERE id = ?",
            Int::class.java,
            id
            ) > 0

    }

    fun buscarPorid(id: Int): Captura? {
        return jdbcTemplate.queryForObject("SELECT * FROM Captura WHERE id = ?",
            BeanPropertyRowMapper(Captura::class.java),
            id
            )
    }

    fun deletarPorId(id: Int):Boolean{
        return jdbcTemplate.update("DELETE FROM Captura WHERE id = ?",
            id
            ) > 0

    }

    fun atualizarPorId(id:Int, capturaParaAtualizar: Captura):Boolean{
        return jdbcTemplate.update("""
            UPDATE Captura SET fkMaquinaRecurso = ?, registro = ?, dthCriacao = ?
        """,
            capturaParaAtualizar.getfkMaquinaRecurso(),
            capturaParaAtualizar.getRegistro(),
            capturaParaAtualizar.getDTHCriacao(),
            id
            ) > 0
    }

}


