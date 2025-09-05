package com.hiusers.mc.linker.bukkit

import com.hiusers.mc.linker.core.LibraryManager
import com.hiusers.mc.linker.core.classloader.URLClassLoaderHelper
import com.hiusers.mc.linker.core.logging.impl.JDKLogAdapter
import org.bukkit.plugin.Plugin
import java.net.URLClassLoader
import java.nio.file.Path

/**
 * Bukkit 插件的运行时依赖管理器
 *
 * @author iiabc
 * @since 2025/9/4 22:02
 */
class BukkitLibraryManager private constructor(
    plugin: Plugin,
    logAdapter: JDKLogAdapter,
    saveDirectory: Path
) : LibraryManager(logAdapter, saveDirectory) {

    private val classLoaderHelper = URLClassLoaderHelper(
        plugin.javaClass.classLoader as URLClassLoader,
        this
    )

    override fun addToClasspath(file: Path) {
        classLoaderHelper.addToClasspath(file)
    }

    companion object {
        /**
         * 创建使用插件目录作为依赖存储目录
         */
        @JvmStatic
        @JvmOverloads
        fun create(plugin: Plugin, directoryName: String = "linker"): BukkitLibraryManager {
            val pluginDataPath = plugin.dataFolder.toPath()
            val serverRoot = pluginDataPath.parent?.parent ?:
            // 回退到插件数据目录
            return BukkitLibraryManager(
                plugin,
                JDKLogAdapter(plugin.logger),
                pluginDataPath.resolve(directoryName)
            )

            return BukkitLibraryManager(
                plugin,
                JDKLogAdapter(plugin.logger),
                serverRoot.resolve(directoryName)
            )
        }
    }
}