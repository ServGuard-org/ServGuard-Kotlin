import io.github.cdimascio.dotenv.dotenv
import org.apache.commons.dbcp2.BasicDataSource
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject

class MaquinaVolumeRepositorio {

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
            CREATE TABLE IF NOT EXISTS ServGuard.MaquinaVolume (
            idMaquinaVolume INT NOT NULL AUTO_INCREMENT,
            fkMaquina INT NOT NULL,
            fkVolume INT NOT NULL,
            
            CONSTRAINT fkMaquinaMaquinaVolume FOREIGN KEY (fkMaquina) REFERENCES ServGuard.dominio_maquina.Maquina(idMaquina),
            CONSTRAINT fkVolumeMaquinaVolume FOREIGN KEY (fkVolume) REFERENCES ServGuard.dominio_volume.Volume(idVolume),
            PRIMARY KEY (idMaquinaVolume, fkMaquina, fkVolume)
            )
        """.trimIndent())
    }

    fun inserir(novoValor:MaquinaVolume):Boolean{
        return jdbcTemplate.update("""
            INSERT INTO MaquinaVolume (fkMaquina, fkVolume) VALUES (?,?)
        """,
            novoValor.getfkMaquina(),
            novoValor.getfkVolume()
            ) > 0
    }

    fun listar():List<MaquinaVolume>{
        return jdbcTemplate.query("SELECT * FROM MaquinaVolume", BeanPropertyRowMapper(MaquinaVolume::class.java))
    }

    fun existirPorId(id:Int):Boolean{
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM MaquinaVolume WHERE id = ?",
            Int::class.java,
            id
            ) > 0
    }

    fun buscarPorId(id:Int):MaquinaVolume?{
        return jdbcTemplate.queryForObject("SELECT * FROM MaquinaVolume WHERE id = ?",
            BeanPropertyRowMapper(MaquinaVolume::class.java),
            id
        )
    }

    fun deletarPorId(id:Int): Boolean{
        return jdbcTemplate.update("DELETE FROM MaquinaVolume WHERE id = ?",
            id
            ) > 0

    }

    fun atualizarPorId(id:Int, maquinaVolumeParaAtualizar: MaquinaVolume): Boolean{
        return jdbcTemplate.update("""
            UPDATE MaquinaVolume SET fkMaquina = ?, fkVolume = ?
        """,
            maquinaVolumeParaAtualizar.getfkMaquina(),
            maquinaVolumeParaAtualizar.getfkVolume(),
            id
        ) > 0
    }

}