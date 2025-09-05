package com.hiusers.mc.linker.core.logging

/**
 * @author iiabc
 * @since 2025/9/4 21:47
 */
interface LogAdapter {

    fun log(level: LogLevel, message: String)

    fun log(level: LogLevel, message: String, throwable: Throwable)

}