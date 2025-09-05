package com.hiusers.mc.linker.core

import com.hiusers.mc.linker.core.classloader.IsolatedClassLoader
import com.hiusers.mc.linker.core.logging.LogAdapter
import com.hiusers.mc.linker.core.logging.Logger
import com.hiusers.mc.linker.core.relocation.Relocation
import com.hiusers.mc.linker.core.relocation.RelocationHelper
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

/**
 * 插件的运行时依赖管理器
 *
 * @author iiabc
 * @since 2025/9/4 21:32
 *
 * @param logAdapter 插件日志适配器
 * @param saveDirectory 下载的库 jar 保存目录
 */
abstract class LibraryManager(
    protected val logAdapter: LogAdapter,
    protected val saveDirectory: Path
) {
    private val logger = Logger(logAdapter)
    private val repositories = mutableSetOf<String>()
    private var relocator: RelocationHelper? = null
    private val isolatedLibraries = mutableMapOf<String, IsolatedClassLoader>()

    init {
        // 确保保存目录存在
        Files.createDirectories(saveDirectory)
    }

    protected abstract fun addToClasspath(file: Path)

    protected fun addToIsolatedClasspath(library: Library, file: Path) {
        val classLoader = if (library.id != null) {
            isolatedLibraries.computeIfAbsent(library.id) { IsolatedClassLoader() }
        } else {
            IsolatedClassLoader()
        }
        classLoader.addPath(file)
    }

    fun getIsolatedClassLoaderOf(libraryId: String): IsolatedClassLoader? {
        return isolatedLibraries[libraryId]
    }

    fun addRepository(url: String) {
        val normalizedUrl = if (url.endsWith("/")) url else "$url/"
        synchronized(repositories) {
            repositories.add(normalizedUrl)
        }
    }

    fun addMavenCentral() = addRepository("https://repo1.maven.org/maven2/")
    fun addSonatype() = addRepository("https://oss.sonatype.org/content/repositories/snapshots/")

    /**
     * 将库 jar 加载到插件的类路径中
     */
    fun loadLibrary(library: Library) {
        var file = downloadLibrary(library)

        if (library.hasRelocations()) {
            file = relocate(file, library.relocatedPath!!, library.relocations)
        }

        if (library.isolatedLoad) {
            addToIsolatedClasspath(library, file)
        } else {
            addToClasspath(file)
        }
    }

    /**
     * 下载库文件到本地缓存
     */
    fun downloadLibrary(library: Library): Path {
        val file = saveDirectory.resolve(library.path)

        if (Files.exists(file) && !library.isSnapshot()) {
            return file
        }

        // 删除现有文件（用于快照版本）
        if (Files.exists(file)) {
            Files.delete(file)
        }

        val urls = resolveLibrary(library)
        if (urls.isEmpty()) {
            throw RuntimeException("Library '$library' couldn't be resolved, add a repository")
        }

        val messageDigest = if (library.hasChecksum()) {
            MessageDigest.getInstance("SHA-256")
        } else null

        val tempFile = file.resolveSibling("${file.fileName}.tmp")
        tempFile.toFile().deleteOnExit()

        try {
            Files.createDirectories(file.parent)

            for (url in urls) {
                val bytes = downloadFromUrl(url) ?: continue

                // 校验和检查
                if (messageDigest != null) {
                    val checksum = messageDigest.digest(bytes)
                    if (!checksum.contentEquals(library.checksum!!)) {
                        logger.warn("*** INVALID CHECKSUM ***")
                        logger.warn(" Library: $library")
                        logger.warn(" URL: $url")
                        continue
                    }
                }

                Files.write(tempFile, bytes)
                Files.move(tempFile, file)
                return file
            }
        } finally {
            Files.deleteIfExists(tempFile)
        }

        throw RuntimeException("Failed to download library '$library'")
    }

    /**
     * 从 URL 下载字节数组
     */
    private fun downloadFromUrl(url: String): ByteArray? {
        return try {
            val connection = URL(url).openConnection()
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.setRequestProperty("User-Agent", "Linker/1.0.0")

            connection.getInputStream().use { input ->
                logger.info("Downloaded library $url")
                input.readBytes()
            }
        } catch (e: Exception) {
            logger.debug("Failed to download from $url: ${e.message}")
            null
        }
    }

    /**
     * 解析库的下载 URL
     */
    private fun resolveLibrary(library: Library): Collection<String> {
        val urls = mutableListOf<String>()

        // 添加直接 URL
        urls.addAll(library.urls)

        // 添加库特定仓库的 URL
        for (repository in library.repositories) {
            urls.add(repository + library.path)
        }

        // 添加全局仓库的 URL
        synchronized(repositories) {
            for (repository in repositories) {
                urls.add(repository + library.path)
            }
        }

        return urls
    }

    /**
     * 应用重定位规则
     */
    private fun relocate(input: Path, outputPath: String, relocations: List<Relocation>): Path {
        val file = saveDirectory.resolve(outputPath)
        if (Files.exists(file)) {
            return file
        }

        val tempFile = file.resolveSibling("${file.fileName}.tmp")
        tempFile.toFile().deleteOnExit()

        synchronized(this) {
            if (relocator == null) {
                relocator = RelocationHelper(this)
            }
        }

        try {
            relocator!!.relocate(input, tempFile, relocations)
            Files.move(tempFile, file)
            logger.info("Relocations applied to ${saveDirectory.parent.relativize(input)}")
            return file
        } finally {
            Files.deleteIfExists(tempFile)
        }
    }
}