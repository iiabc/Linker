package com.hiusers.mc.linker.core.classloader

import java.net.MalformedURLException
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Path

/**
 * @author iiabc
 * @since 2025/9/4 21:49
 */
class IsolatedClassLoader(vararg urls: URL) : URLClassLoader(urls, getSystemClassLoader().parent) {

    companion object {
        init {
            registerAsParallelCapable()
        }
    }

    public override fun addURL(url: URL) {
        super.addURL(url)
    }

    fun addPath(path: Path) {
        try {
            addURL(path.toUri().toURL())
        } catch (e: MalformedURLException) {
            throw IllegalArgumentException(e)
        }
    }

}