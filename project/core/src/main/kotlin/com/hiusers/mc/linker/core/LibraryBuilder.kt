package com.hiusers.mc.linker.core

import com.hiusers.mc.linker.core.relocation.Relocation
import java.util.*

/**
 * Library 的流式构建器
 *
 * @author iiabc
 * @since 2025/9/4 21:37
 */
class LibraryBuilder {
    private val urls = mutableListOf<String>()
    private val repositories = mutableListOf<String>()
    private var id: String? = null
    private var groupId: String? = null
    private var artifactId: String? = null
    private var version: String? = null
    private var classifier: String? = null
    private var checksum: ByteArray? = null
    private var isolatedLoad: Boolean = false
    private val relocations = mutableListOf<Relocation>()

    fun url(url: String): LibraryBuilder {
        urls.add(url)
        return this
    }

    fun repository(url: String): LibraryBuilder {
        val normalizedUrl = if (url.endsWith("/")) url else "$url/"
        repositories.add(normalizedUrl)
        return this
    }

    fun id(id: String): LibraryBuilder {
        this.id = id
        return this
    }

    fun groupId(groupId: String): LibraryBuilder {
        this.groupId = groupId.replace("{}", ".")
        return this
    }

    fun artifactId(artifactId: String): LibraryBuilder {
        this.artifactId = artifactId
        return this
    }

    fun version(version: String): LibraryBuilder {
        this.version = version
        return this
    }

    fun classifier(classifier: String): LibraryBuilder {
        this.classifier = classifier
        return this
    }

    fun checksum(checksum: ByteArray): LibraryBuilder {
        this.checksum = checksum
        return this
    }

    fun checksum(checksum: String): LibraryBuilder {
        this.checksum = Base64.getDecoder().decode(checksum)
        return this
    }

    fun isolatedLoad(isolatedLoad: Boolean): LibraryBuilder {
        this.isolatedLoad = isolatedLoad
        return this
    }

    fun relocate(relocation: Relocation): LibraryBuilder {
        relocations.add(relocation)
        return this
    }

    fun relocate(pattern: String, relocatedPattern: String): LibraryBuilder {
        return relocate(Relocation(pattern.replace("{}", "."), relocatedPattern.replace("{}", ".")))
    }

    fun build(): Library {
        require(groupId != null) { "groupId cannot be null" }
        require(artifactId != null) { "artifactId cannot be null" }
        require(version != null) { "version cannot be null" }

        return Library(
            groupId = groupId!!,
            artifactId = artifactId!!,
            version = version!!,
            classifier = classifier,
            urls = urls.toList(),
            repositories = repositories.toList(),
            checksum = checksum,
            relocations = relocations.toList(),
            isolatedLoad = isolatedLoad,
            id = id
        )
    }

}