package com.meloda.netservices.io

import org.jetbrains.annotations.Contract
import java.io.*
import java.nio.charset.Charset
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.math.max

object EasyStreams {

    const val BUFFER_SIZE = 8192
    const val CHAR_BUFFER_SIZE = 4096

    @JvmOverloads
    @Throws(IOException::class)
    fun read(from: InputStream, encoding: Charset? = Charsets.UTF_8): String {
        return read(InputStreamReader(from, encoding))
    }

    @JvmStatic
    @Throws(IOException::class)
    fun read(from: Reader): String {
        val builder = StringWriter(CHAR_BUFFER_SIZE)
        return try {
            copy(from, builder)
            builder.toString()
        } finally {
            close(from)
        }
    }

    @JvmStatic
    @Throws(IOException::class)
    fun readBytes(from: InputStream): ByteArray {
        val output = ByteArrayOutputStream(max(from.available(), BUFFER_SIZE))
        try {
            copy(from, output)
        } finally {
            close(from)
        }
        return output.toByteArray()
    }

    @Throws(IOException::class)
    fun write(from: ByteArray?, to: OutputStream) {
        try {
            to.write(from)
            to.flush()
        } finally {
            close(to)
        }
    }

    @Throws(IOException::class)
    fun write(from: String?, to: OutputStream?) {
        write(from, OutputStreamWriter(to, Charsets.UTF_8))
    }

    @Throws(IOException::class)
    fun write(from: CharArray?, to: Writer) {
        try {
            to.write(from)
            to.flush()
        } finally {
            close(to)
        }
    }

    @JvmStatic
    @Throws(IOException::class)
    fun write(from: String?, to: Writer) {
        try {
            to.write(from)
            to.flush()
        } finally {
            close(to)
        }
    }

    @Throws(IOException::class)
    fun copy(from: Reader, to: Writer): Long {
        val buffer = CharArray(CHAR_BUFFER_SIZE)
        var read: Int
        var total: Long = 0
        while (from.read(buffer).also { read = it } != -1) {
            to.write(buffer, 0, read)
            total += read.toLong()
        }
        return total
    }

    @Throws(IOException::class)
    fun copy(from: InputStream, to: OutputStream): Long {
        val buffer = ByteArray(BUFFER_SIZE)
        var read: Int
        var total: Long = 0
        while (from.read(buffer).also { read = it } != -1) {
            to.write(buffer, 0, read)
            total += read.toLong()
        }
        return total
    }

    fun buffer(input: InputStream?): BufferedInputStream {
        return buffer(input, BUFFER_SIZE)
    }

    @Contract("null, _ -> new")
    fun buffer(input: InputStream?, size: Int): BufferedInputStream {
        return if (input is BufferedInputStream) input else BufferedInputStream(input, size)
    }

    fun buffer(output: OutputStream?): BufferedOutputStream {
        return buffer(output, BUFFER_SIZE)
    }

    @Contract("null, _ -> new")
    fun buffer(output: OutputStream?, size: Int): BufferedOutputStream {
        return if (output is BufferedOutputStream) output else BufferedOutputStream(output, size)
    }

    fun buffer(input: Reader?): BufferedReader {
        return buffer(input, CHAR_BUFFER_SIZE)
    }

    @Contract("null, _ -> new")
    fun buffer(input: Reader?, size: Int): BufferedReader {
        return if (input is BufferedReader) input else BufferedReader(input, size)
    }

    fun buffer(output: Writer?): BufferedWriter {
        return buffer(output, CHAR_BUFFER_SIZE)
    }

    @Contract("null, _ -> new")
    fun buffer(output: Writer?, size: Int): BufferedWriter {
        return if (output is BufferedWriter) output else BufferedWriter(output, size)
    }

    @Throws(IOException::class)
    fun gzip(input: InputStream?): GZIPInputStream {
        return gzip(input, BUFFER_SIZE)
    }

    @Contract("null, _ -> new")
    @Throws(IOException::class)
    fun gzip(input: InputStream?, size: Int): GZIPInputStream {
        return if (input is GZIPInputStream) input else GZIPInputStream(input, size)
    }

    @Throws(IOException::class)
    fun gzip(input: OutputStream?): GZIPOutputStream {
        return gzip(input, BUFFER_SIZE)
    }

    @Contract("null, _ -> new")
    @Throws(IOException::class)
    fun gzip(input: OutputStream?, size: Int): GZIPOutputStream {
        return if (input is GZIPOutputStream) input else GZIPOutputStream(input, size)
    }

    fun close(c: Closeable?): Boolean {
        if (c != null) {
            try {
                c.close()
                return true
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return false
    }
}