package com.hiusers.mc.linker.core.logging

/**
 * @author iiabc
 * @since 2025/9/4 22:39
 */
class Logger(private val adapter: LogAdapter) {
    fun info(message: String) = adapter.log(LogLevel.INFO, message)
    fun warn(message: String) = adapter.log(LogLevel.WARN, message)
    fun debug(message: String) = adapter.log(LogLevel.DEBUG, message)
    fun error(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            adapter.log(LogLevel.ERROR, message, throwable)
        } else {
            adapter.log(LogLevel.ERROR, message)
        }
    }
}
