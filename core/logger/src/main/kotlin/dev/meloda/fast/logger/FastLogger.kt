package dev.meloda.fast.logger

import android.util.Log
import kotlin.reflect.KClass

class FastLogger {
    companion object {

        @Volatile
        private lateinit var instance: FastLogger

        fun setInstance(logger: FastLogger) {
            if (::instance.isInitialized) {
                throw IllegalStateException("FastLogger has already been initialized.")
            }

            instance = logger
        }

        fun getInstance(): FastLogger {
            if (!::instance.isInitialized) {
                throw UninitializedPropertyAccessException("FastLogger is not initialized.")
            }
            return instance
        }
    }

    private var logLevel: FastLogLevel = FastLogLevel.ERROR

    fun setLogLevel(logLevel: FastLogLevel) {
        Log.v(this::class.java.simpleName, "Set LogLevel from ${this.logLevel} to $logLevel")
        this.logLevel = logLevel
    }

    fun verbose(clazz: Class<*>, message: String, throwable: Throwable? = null) {
        verbose(clazz.simpleName, message, throwable)
    }

    fun verbose(tag: String, message: String, throwable: Throwable? = null) {
        if (shouldLog(FastLogLevel.VERBOSE)) {
            Log.v(tag, message, throwable)
        }
    }

    fun debug(clazz: KClass<*>, message: String, throwable: Throwable? = null) {
        debug(clazz.java, message, throwable)
    }

    fun debug(clazz: Class<*>, message: String, throwable: Throwable? = null) {
        debug(clazz.simpleName, message, throwable)
    }

    fun debug(tag: String, message: String, throwable: Throwable? = null) {
        if (shouldLog(FastLogLevel.DEBUG)) {
            Log.d(tag, message, throwable)
        }
    }

    fun info(clazz: KClass<*>, message: String, throwable: Throwable? = null) {
        info(clazz.java, message, throwable)
    }

    fun info(clazz: Class<*>, message: String, throwable: Throwable? = null) {
        info(clazz.simpleName, message, throwable)
    }

    fun info(tag: String, message: String, throwable: Throwable? = null) {
        if (shouldLog(FastLogLevel.INFO)) {
            Log.i(tag, message, throwable)
        }
    }

    fun warning(clazz: Class<*>, message: String, throwable: Throwable? = null) {
        warning(clazz.simpleName, message, throwable)
    }

    fun warning(tag: String, message: String, throwable: Throwable? = null) {
        if (shouldLog(FastLogLevel.WARNING)) {
            Log.w(tag, message, throwable)
        }
    }

    fun error(clazz: KClass<*>, message: String, throwable: Throwable? = null) {
        error(clazz.java, message, throwable)
    }

    fun error(clazz: Class<*>, message: String, throwable: Throwable? = null) {
        error(clazz.simpleName, message, throwable)
    }

    fun error(tag: String, message: String, throwable: Throwable? = null) {
        if (shouldLog(FastLogLevel.ERROR)) {
            Log.e(tag, message, throwable)
        }
    }

    fun assert(clazz: Class<*>, message: String, throwable: Throwable? = null) {
        assert(clazz.simpleName, message, throwable)
    }

    fun assert(tag: String, message: String, throwable: Throwable? = null) {
        if (shouldLog(FastLogLevel.ASSERT)) {
            Log.wtf(tag, message, throwable)
        }
    }

    private fun shouldLog(level: FastLogLevel): Boolean = level.ordinal >= logLevel.ordinal
}
