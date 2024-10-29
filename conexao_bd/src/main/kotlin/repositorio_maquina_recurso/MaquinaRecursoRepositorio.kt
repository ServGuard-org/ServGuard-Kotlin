package repositorio_maquina_recurso

import dominio_maquina_recurso.MaquinaRecurso
import org.apache.commons.dbcp2.BasicDataSource
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import kotlin.math.max

class MaquinaRecursoRepositorio {

    lateinit var jdbcTemplate: JdbcTemplate

    fun configurar(){
        val datasource = BasicDataSource()
        datasource.driverClassName = "com.mysql.cj.jdbc.Driver"
        datasource.url = "jdbc:mysql://localhost:3306/ServGuard?useTimezone=true&serverTimezone=America/Sao_Paulo"
        datasource.username = "root"
        datasource.password = "2205"

        jdbcTemplate = JdbcTemplate(datasource)
    }

    fun criarTabela(){
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS MaquinaRecurso(
            IdMaquinaRecurso INT NOT NULL AUTO_INCREMENT,
            fkMaquina INT NOT NULL,
            fkRecurso INT NOT NULL,
            max DECIMAL(8,3),
            
            CONSTRAINT fkMaquinaMaquinaRecurso FOREIGN KEY (fkMaquina) REFERENCES Maquina(idMaquina),
            CONSTRAINT fkRecursoMaquinaRecurso FOREIGN KEY (fkRecurso) REFERENCES Recurso(idRecurso),
            PRIMARY KEY (idMaquinaRecurso, fkMaquina, fkRecurso)
            )
        """.trimIndent())
    }

    fun inserir(novoValor: MaquinaRecurso):Boolean{
        return jdbcTemplate.update("""
            INSERT INTO MaquinaRecurso (fkMaquina, fkRecurso, max) VALUES (?,?,?)
        """,
            novoValor.getfkMaquina(),
            novoValor.getfkRecurso(),
            novoValor.getMax()
            ) > 0
    }

    fun listar():List<MaquinaRecurso>{
        return jdbcTemplate.query("SELECT * FROM MaquinaRecurso", BeanPropertyRowMapper(MaquinaRecurso::class.java))
    }

    fun existePorId(id: Int):Boolean{
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM MaquinaRecurso WHERE id = ?",
            Int::class.java,
            id
            ) > 0
    }

    fun obterAlerta(fkMaquina: Int, fkRecurso: Int):Double{
        return jdbcTemplate.queryForObject("SELECT max FROM MaquinaRecurso WHERE fkMaquina = ?, and fkRecurso = ?",
            Double::class.java,
            fkMaquina, fkRecurso
            )
    }

    fun buscarPorId(id: Int): MaquinaRecurso? {
        return jdbcTemplate.queryForObject("SELECT * FROM MaquinaRecurso WHERE id = ?",
        BeanPropertyRowMapper(MaquinaRecurso::class.java),
        id
        )
    }


    fun buscarIdMaquinaRecurso(idRecurso: Int, idMaquina: Int): Int? {
        return jdbcTemplate.queryForObject(
            """
        SELECT idMaquinaRecurso FROM MaquinaRecurso WHERE fkRecurso = ? AND fkMaquina = ?
        """,
            Int::class.java,
            idRecurso,
            idMaquina
        )
    }

    fun deletarPorId(id:Int): Boolean{
        return jdbcTemplate.update("DELETE FROM MaquinaRecurso WHERE id = ?",
            id
            ) > 0
    }

    fun atualizarPorId(id:Int, maquinaRecursoParaAtualizar: MaquinaRecurso): Boolean{
        return jdbcTemplate.update("""
            UPDATE MaquinaRecurso SET fkMaquina = ?, fkRecurso = ?, max = ?
        """,
            maquinaRecursoParaAtualizar.getfkMaquina(),
            maquinaRecursoParaAtualizar.getfkRecurso(),
            maquinaRecursoParaAtualizar.getMax(),
            id
            ) > 0
    }


}