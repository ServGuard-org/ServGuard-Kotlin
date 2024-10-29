package repositorio_maquina

import dominio_maquina.Maquina
import io.github.cdimascio.dotenv.dotenv
import org.apache.commons.dbcp2.BasicDataSource
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.JdbcTemplate


class MaquinaRepositorio{

    lateinit var  jdbcTemplate: JdbcTemplate

    fun configurar(){
        val dotenv = dotenv()
        val datasource = BasicDataSource()
        datasource.driverClassName = "com.mysql.cj.jdbc.Driver"
        datasource.url = "jdbc:mysql://${dotenv["DB_HOST"]}:${dotenv["DB_PORT"]}/${dotenv["DATABASE"]}"
        datasource.username = dotenv["DB_USER"]
        datasource.password = dotenv["DB_PASSWORD"]

        jdbcTemplate = JdbcTemplate(datasource)
    }

    fun criarTabela(){
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS Maquina(
            idMaquina INT NOT NULL AUTO_INCREMENT,
            fkEmpresa INT NOT NULL,
            nome VARCHAR(50),
            rack VARCHAR(20),
            modeloCPU VARCHAR(50),
            qtdNucleosFisicos INT,
            qtdNucleosLogicos INT, 
            capacidadeRAM DECIMAL(8,3),
            MACAddress CHAR(17) UNIQUE NOT NULL,
            isAtiva TINYINT DEFAULT 1 NOT NULL,
            
            CONSTRAINT fkEmpresaMaquina FOREIGN KEY (fkEmpresa) REFERENCES Empresa (idEmpresa),
            PRIMARY KEY (idMaquina, fkEmpresa)
            )
        """.trimIndent())

    }

    fun inserir(novoValor: Maquina):Boolean{
        return jdbcTemplate.update("""
            INSERT INTO Maquina (fkEmpresa, nome, rack, modeloCPU, qtdNucleosFisicos, qtdNucleosLogicos, capacidadeRAM, MACAddres, isAtiva) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
            novoValor.getfkEmpresa(),
            novoValor.getNomeMaquina(),
            novoValor.getRack(),
            novoValor.getModeloCPU(),
            novoValor.getNucleosFisicos(),
            novoValor.getNucleosLogicos(),
            novoValor.getCapacidadeRAM(),
            novoValor.getMACAddress(),
            novoValor.getIsAtiva()
            ) > 0
    }

    fun listar():List<Maquina>{
        return jdbcTemplate.query("SELECT * FROM Maquina", BeanPropertyRowMapper(Maquina::class.java))
    }

    fun existePorId(id: Int): Boolean{
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Maquina WHERE id = ?",
            Int::class.java,
            id
        ) > 0
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

    fun buscarPorId(id:Int): Maquina? {
        return jdbcTemplate.queryForObject("SELECT * FROM Maquina WHERE id = ?",
            BeanPropertyRowMapper(Maquina::class.java),
            id
        )
    }

    fun deletarPorId(id:Int):Boolean{
        return jdbcTemplate.update(
            "DELETE FROM Maquina WHERE id = ?",
            id
        ) > 0

    }

    fun atualizarPorId(id:Int, maquinaParaAtualizar: Maquina):Boolean{
        return jdbcTemplate.update(
            """ UPDATE Maquina SET fkEmpresa = ?, nome = ?, rack = ?, modeloCPU = ?, qtdNucleosFisicos = ?, qtdNucleosLogicos = ?, capacidadeRAM = ?, 
                MACAddress = ?, isAtiva = ?
            """,
            maquinaParaAtualizar.getfkEmpresa(),
            maquinaParaAtualizar.getNomeMaquina(),
            maquinaParaAtualizar.getRack(),
            maquinaParaAtualizar.getModeloCPU(),
            maquinaParaAtualizar.getNucleosFisicos(),
            maquinaParaAtualizar.getNucleosLogicos(),
            maquinaParaAtualizar.getCapacidadeRAM(),
            maquinaParaAtualizar.getMACAddress(),
            maquinaParaAtualizar.getIsAtiva(),
            id
        ) > 0

    }
}