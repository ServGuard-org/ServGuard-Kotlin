package repositorio_recurso

import dominio_recurso.Recurso
import io.github.cdimascio.dotenv.dotenv
import org.apache.commons.dbcp2.BasicDataSource
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.JdbcTemplate


class RecursoRepositorio {

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
            CREATE TABLE IF NOT EXISTS Recurso(
            idRecurso INT NOT NULL AUTO_INCREMENT,
            nome VARCHAR(45) NOT NULL,
            unidadeMedida VARCHAR(45) NOT NULL,
            
            PRIMARY KEY (idRecurso)
            ) 
        """.trimIndent())
    }

    fun inserir(novoValor: Recurso):Boolean{
        return jdbcTemplate.update("""
            INSERT INTO Recurso (nome, unidadaMedida) VALUES (?,?)
        """,
            novoValor.getNomeRecurso(),
            novoValor.getUnidadeMedida()
            ) > 0
    }

    fun listar():List<Recurso>{
        return jdbcTemplate.query("SELECT * FROM Recurso", BeanPropertyRowMapper(Recurso::class.java))
    }

    fun existePorId(id: Int): Boolean{
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Recurso WHERE id = ?",
            Int::class.java,
            id
        ) > 0
    }

    fun buscarPorId(id:Int): Recurso? {
        return jdbcTemplate.queryForObject("SELECT * FROM Recurso WHERE id = ?",
            BeanPropertyRowMapper(Recurso::class.java),
            id
        )
    }

    fun existeRecursoPorNome(nomeRecurso: String): Boolean {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM MaquinaRecurso WHERE nome = ?",
            Int::class.java,
            nomeRecurso
        ) > 0
    }

    fun buscarIdRecursoPorNome(nomeRecurso: String): Int? {
        return jdbcTemplate.queryForObject(
            "SELECT idRecurso FROM Recurso WHERE nome = ?",
            Int::class.java,
            nomeRecurso
        )
    }

    fun deletarPorId(id:Int):Boolean{
        return jdbcTemplate.update(
            "DELETE FROM Recurso WHERE id = ?",
            id
        ) > 0
    }

    fun atualizarPorId(id:Int, recursoParaAtualizar: Recurso):Boolean{
        return jdbcTemplate.update(
            """ UPDATE Recurso SET nome = ?, unidadeMedida = ?
            """,
            recursoParaAtualizar.getNomeRecurso(),
            recursoParaAtualizar.getUnidadeMedida(),
            id
        ) > 0

    }


}