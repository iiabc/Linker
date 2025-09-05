package com.hiusers.mc.linker.core.relocation

import com.hiusers.mc.linker.core.Library
import com.hiusers.mc.linker.core.LibraryManager
import com.hiusers.mc.linker.core.classloader.IsolatedClassLoader
import java.io.File
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.nio.file.Path

/**
 * @author iiabc
 * @since 2025/9/4 21:51
 */
class RelocationHelper(libraryManager: LibraryManager) {
    private val jarRelocatorConstructor: Constructor<*>
    private val jarRelocatorRunMethod: Method
    private val relocationConstructor: Constructor<*>

    init {
        val classLoader = IsolatedClassLoader()

        // 下载 jar-relocator 依赖
        classLoader.addPath(
            libraryManager.downloadLibrary(
                Library.builder()
                    .groupId("me.lucko")
                    .artifactId("jar-relocator")
                    .version("1.7")
                    .checksum("b30RhOF6kHiHl+O5suNLh/+eAr1iOFEFLXhwkHHDu4I=")
                    .build()
            )
        )

        // 下载 ASM 依赖
        classLoader.addPath(
            libraryManager.downloadLibrary(
                Library.builder()
                    .groupId("org.ow2.asm")
                    .artifactId("asm")
                    .version("9.7")
                    .checksum("rfRtXjSUC98Ujs3Sap7o7qlElqcgNP9xQQZrPupcTp0=")
                    .build()
            )
        )

        classLoader.addPath(
            libraryManager.downloadLibrary(
                Library.builder()
                    .groupId("org.ow2.asm")
                    .artifactId("asm-commons")
                    .version("9.7")
                    .checksum("OJvCR5WOBJ/JoECNOYySxtNwwYA1EgOV1Muh2dkwS3o=")
                    .build()
            )
        )

        try {
            val jarRelocatorClass = classLoader.loadClass("me.lucko.jarrelocator.JarRelocator")
            val relocationClass = classLoader.loadClass("me.lucko.jarrelocator.Relocation")

            jarRelocatorConstructor = jarRelocatorClass.getConstructor(
                File::class.java, File::class.java, Collection::class.java
            )
            jarRelocatorRunMethod = jarRelocatorClass.getMethod("run")
            relocationConstructor = relocationClass.getConstructor(
                String::class.java, String::class.java, Collection::class.java, Collection::class.java
            )
        } catch (e: ReflectiveOperationException) {
            throw RuntimeException(e)
        }
    }

    fun relocate(input: Path, output: Path, relocations: Collection<Relocation>) {
        try {
            val rules = relocations.map { relocation ->
                relocationConstructor.newInstance(
                    relocation.pattern,
                    relocation.relocatedPattern,
                    relocation.includes,
                    relocation.excludes
                )
            }

            val relocator = jarRelocatorConstructor.newInstance(
                input.toFile(), output.toFile(), rules
            )
            jarRelocatorRunMethod.invoke(relocator)
        } catch (e: ReflectiveOperationException) {
            throw RuntimeException(e)
        }
    }
}