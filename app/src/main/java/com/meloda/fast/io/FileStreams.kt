package com.meloda.fast.io

import com.meloda.fast.io.EasyStreams.read
import com.meloda.fast.io.EasyStreams.write
import org.jetbrains.annotations.Contract
import java.io.*
import java.math.BigInteger

object FileStreams {

    val lineSeparatorChar = lineSeparator()[0]

    const val ONE_KB = 1024
    const val ONE_MB = ONE_KB * 1024
    const val ONE_GB = ONE_MB * 1024
    const val ONE_TB = ONE_GB * 1024L
    const val ONE_PB = ONE_TB * 1024L
    const val ONE_EB = ONE_PB * 1024L

    val ONE_ZB: BigInteger = BigInteger.valueOf(ONE_EB).multiply(BigInteger.valueOf(1024L))
    val ONE_YB: BigInteger = ONE_ZB.multiply(BigInteger.valueOf(1024L))

    @Throws(IOException::class)
    fun read(from: File?): String {
        return read(reader(from))
    }

    @Throws(IOException::class)
    fun write(from: String?, to: File?) {
        write(from, writer(to))
    }

    @Throws(IOException::class)
    fun write(from: ByteArray?, to: File?) {
        write(from, FileOutputStream(to))
    }

    @Throws(IOException::class)
    fun append(from: ByteArray?, to: File?) {
        write(from, FileOutputStream(to, true))
    }

    @Throws(IOException::class)
    fun append(from: CharArray?, to: File?) {
        write(from, FileWriter(to, true))
    }

    @Throws(IOException::class)
    fun append(from: CharSequence, to: File?) {
        write(if (from is String) from else from.toString(), FileWriter(to, true))
    }

    fun delete(dir: File) {
        if (dir.isDirectory) {
            val files = dir.listFiles() ?: return
            for (file in files) {
                delete(file)
            }
        } else {
            dir.delete()
        }
    }

    fun lineSeparator(): String {
        return System.lineSeparator()
    }

    fun search(dir: File, name: String?): File? {
        require(dir.isDirectory) { "dir can't be file." }

        val files = dir.listFiles() ?: return null

        if (files.isEmpty()) {
            return null
        }

        for (file in files) {
            if (file.isDirectory) {
                search(file, name)
            } else if (file.name.contains(name!!)) {
                return file
            }
        }
        return null
    }

    @Contract("_ -> new")
    @Throws(FileNotFoundException::class)
    fun reader(from: File?): Reader {
        return InputStreamReader(FileInputStream(from), Charsets.UTF_8)
    }

    @Contract("_ -> new")
    @Throws(FileNotFoundException::class)
    fun writer(to: File?): Writer {
        return OutputStreamWriter(FileOutputStream(to), Charsets.UTF_8)
    }
}