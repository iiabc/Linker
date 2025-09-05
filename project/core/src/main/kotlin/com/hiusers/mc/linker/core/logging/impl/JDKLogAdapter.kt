package com.hiusers.mc.linker.core.logging.impl

import com.hiusers.mc.linker.core.logging.LogAdapter
import com.hiusers.mc.linker.core.logging.LogLevel
import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author iiabc
 * @since 2025/9/4 22:13
 */
class JDKLogAdapter(private val logger: Logger) : LogAdapter {

    override fun log(level: LogLevel, message: String) {
        when (level) {
            LogLevel.DEBUG -> logger.log(Level.FINE, message)
            LogLevel.INFO -> logger.log(Level.INFO, message)
            LogLevel.WARN -> logger.log(Level.WARNING, message)
            LogLevel.ERROR -> logger.log(Level.SEVERE, message)
        }
    }

    override fun log(level: LogLevel, message: String, throwable: Throwable) {
        when (level) {
            LogLevel.DEBUG -> logger.log(Level.FINE, message, throwable)
            LogLevel.INFO -> logger.log(Level.INFO, message, throwable)
            LogLevel.WARN -> logger.log(Level.WARNING, message, throwable)
            LogLevel.ERROR -> logger.log(Level.SEVERE, message, throwable)
        }
    }

}