package dominio_slack

import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class Slack(private val url: String) {

    @Throws(Exception::class)
    fun enviar(message: JSONObject) {
        val obj = URL(url)
        val con = obj.openConnection() as HttpURLConnection

        con.requestMethod = "POST"
        con.doOutput = true

        DataOutputStream(con.outputStream).use { wr ->
            wr.writeBytes(message.toString())
            wr.flush()
        }

        val responseCode = con.responseCode
        println("Sending 'POST' request to URL: $url")
        println("POST parameters: ${message.toString()}")
        println("Response Code: $responseCode")

        BufferedReader(InputStreamReader(con.inputStream)).use { reader ->
            val response = StringBuilder()
            reader.lines().forEach { line -> response.append(line) }
            println("Success.")
        }
    }
}
