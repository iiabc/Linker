package com.hiusers.mc.linker.paper

import com.hiusers.mc.linker.core.LibraryManager
import com.hiusers.mc.linker.core.classloader.URLClassLoaderHelper
import com.hiusers.mc.linker.core.logging.impl.JDKLogAdapter
import org.bukkit.plugin.Plugin
import java.net.URLClassLoader
import java.nio.file.Path

/**
 * Paper 插件的运行时依赖管理器
 * 专门为 Paper 1.19.3+ 的 PaperPluginClassLoader 设计
 *
 * @author iiabc
 * @since 2025/9/4 22:10
 */
class PaperLibraryManager private constructor(
    plugin: Plugin,
    logAdapter: JDKLogAdapter,
    saveDirectory: Path
) : LibraryManager(logAdapter, saveDirectory) {

    private val classLoaderHelper = createClassLoaderHelper(plugin, this)

    override fun addToClasspath(file: Path) {
        classLoaderHelper.addToClasspath(file)
    }

    companion object {
        @JvmStatic
        @JvmOverloads
        fun create(plugin: Plugin, directoryName: String = "lib"): PaperLibraryManager {
            return PaperLibraryManager(
                plugin,
                JDKLogAdapter(plugin.logger),
                plugin.dataFolder.toPath().resolve(directoryName)
            )
        }

        private fun createClassLoaderHelper(plugin: Plugin, libraryManager: LibraryManager): URLClassLoaderHelper {
            val classLoader = plugin.javaClass.classLoader
            val paperClClazz = try {
                Class.forName("io.papermc.paper.plugin.entrypoint.classloader.PaperPluginClassLoader")
            } catch (e: ClassNotFoundException) {
                throw RuntimeException("PaperPluginClassLoader not found, are you using Paper 1.19.3+?", e)
            }

            require(paperClClazz.isAssignableFrom(classLoader.javaClass)) {
                "Plugin classloader is not a PaperPluginClassLoader, are you using paper-plugin.yml?"
            }

            val libraryLoaderField = try {
                paperClClazz.getDeclaredField("libraryLoader")
            } catch (e: NoSuchFieldException) {
                throw RuntimeException("Cannot find libraryLoader field in PaperPluginClassLoader", e)
            }

            libraryLoaderField.isAccessible = true
            val libraryLoader = try {
                libraryLoaderField.get(classLoader) as URLClassLoader
            } catch (e: IllegalAccessException) {
                throw RuntimeException(e)
            }

            return URLClassLoaderHelper(libraryLoader, libraryManager)
        }
    }

}