package ru.melod1n.project.vkm.net

import androidx.collection.ArrayMap
import androidx.collection.arrayMapOf
import ru.melod1n.project.vkm.io.EasyStreams.gzip
import ru.melod1n.project.vkm.io.EasyStreams.read
import ru.melod1n.project.vkm.io.EasyStreams.readBytes
import java.io.IOException
import java.io.InputStream
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class HttpRequest(
    private val url: String,
    private val method: String,
    private val params: ArrayMap<String, String> = arrayMapOf()
) {
    companion object {
        const val GET = "GET"
        const val POST = "POST"

        operator fun get(
            url: String,
            params: ArrayMap<String, String> = arrayMapOf()
        ): HttpRequest {
            return HttpRequest(url, GET, params)
        }

        operator fun get(url: String): HttpRequest {
            return Companion[url, ArrayMap()]
        }

    }

    private var connection: HttpURLConnection? = null

    @Throws(IOException::class)
    fun asString(): String {
        val input = getStream()

        val content = read(input)

        connection?.disconnect()

        return content
    }

    @Throws(IOException::class)
    fun asBytes(): ByteArray {
        val input = getStream()
        val content = readBytes(input)

        connection?.disconnect()

        return content
    }

    @Throws(IOException::class)
    fun getStream(): InputStream {
        if (connection == null) {
            connection = createConnection()
        }

        var input = connection!!.inputStream
        val encoding = connection!!.getHeaderField("Content-Encoding")
        if ("gzip".equals(encoding, ignoreCase = true)) {
            input = gzip(input)
        }

        return input
    }

    @Throws(UnsupportedEncodingException::class)
    private fun getParams(): String {
        val buffer = StringBuilder()

        for (i in 0 until params.size) {
            val key = params.keyAt(i)
            val value = params.valueAt(i)
            buffer.append(key).append("=")
            buffer.append(URLEncoder.encode(value, "UTF-8"))
            buffer.append("&")
        }
        return buffer.toString()
    }

    @Throws(UnsupportedEncodingException::class)
    private fun getUrl(): String {
        return if (params.isNotEmpty() && "GET".equals(method, ignoreCase = true)) {
            url + "?" + getParams()
        } else url
    }

    @Throws(IOException::class)
    private fun createConnection(): HttpURLConnection? {
        connection = URL(getUrl()).openConnection() as HttpURLConnection
        connection!!.readTimeout = 60000
        connection!!.connectTimeout = 60000
        connection!!.useCaches = true
        connection!!.doInput = true
        connection!!.doOutput =
            !GET.equals(method, ignoreCase = true)
        connection!!.requestMethod = method
        connection!!.setRequestProperty("Accept-Encoding", "gzip")
        return connection
    }

    override fun toString(): String {
        try {
            return asString()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }
}