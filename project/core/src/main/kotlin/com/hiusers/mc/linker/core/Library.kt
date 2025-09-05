package com.hiusers.mc.linker.core

import com.hiusers.mc.linker.core.relocation.Relocation

/**
 * @author iiabc
 * @since 2025/9/4 21:28
 *
 * @param groupId Maven 组 ID（必需）
 * @param artifactId Maven 工件 ID（必需）
 * @param version 工件版本，支持 SNAPSHOT 检测
 * @param classifier 可选的工件分类器
 * @param urls 直接下载 URL 列表（绕过仓库解析）
 * @param repositories 库特定的仓库 URL 列表
 * @param checksum SHA-256 校验和，用于完整性验证
 * @param relocations 要应用的包重定位规则
 * @param isolatedLoad 是否在隔离的类加载器中加载
 * @param id 用于共享隔离类加载器的标识符
 */
data class Library(
    val groupId: String,
    val artifactId: String,
    val version: String,
    val classifier: String? = null,
    val urls: List<String> = emptyList(),
    val repositories: List<String> = emptyList(),
    val checksum: ByteArray? = null,
    val relocations: List<Relocation> = emptyList(),
    val isolatedLoad: Boolean = false,
    val id: String? = null
) {
    /**
     * 获取此库工件的相对 Maven 路径
     * 格式：groupId/artifactId/version/artifactId-version[-classifier].jar
     */
    val path: String by lazy {
        val basePath = "$partialPath$artifactId-$version"
        val withClassifier = if (classifier != null) "$basePath-$classifier" else basePath
        "$withClassifier.jar"
    }

    /**
     * 获取此库的相对部分 Maven 路径
     * 格式：groupId/artifactId/version/
     */
    val partialPath: String by lazy {
        "${groupId.replace('.', '/')}/$artifactId/$version/"
    }

    /**
     * 获取重定位后 jar 的相对路径
     */
    val relocatedPath: String? by lazy {
        if (hasRelocations()) {
            val basePath = "$partialPath$artifactId-$version"
            val withClassifier = if (classifier != null) "$basePath-$classifier" else basePath
            "$withClassifier-relocated.jar"
        } else null
    }

    fun hasClassifier(): Boolean = classifier != null
    fun hasChecksum(): Boolean = checksum != null
    fun hasRelocations(): Boolean = relocations.isNotEmpty()
    fun isSnapshot(): Boolean = version.endsWith("-SNAPSHOT")

    override fun toString(): String {
        return if (hasClassifier()) {
            "$groupId:$artifactId:$version:$classifier"
        } else {
            "$groupId:$artifactId:$version"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Library

        if (groupId != other.groupId) return false
        if (artifactId != other.artifactId) return false
        if (version != other.version) return false
        if (classifier != other.classifier) return false
        if (urls != other.urls) return false
        if (repositories != other.repositories) return false
        if (checksum != null) {
            if (other.checksum == null) return false
            if (!checksum.contentEquals(other.checksum)) return false
        } else if (other.checksum != null) return false
        if (relocations != other.relocations) return false
        if (isolatedLoad != other.isolatedLoad) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = groupId.hashCode()
        result = 31 * result + artifactId.hashCode()
        result = 31 * result + version.hashCode()
        result = 31 * result + (classifier?.hashCode() ?: 0)
        result = 31 * result + urls.hashCode()
        result = 31 * result + repositories.hashCode()
        result = 31 * result + (checksum?.contentHashCode() ?: 0)
        result = 31 * result + relocations.hashCode()
        result = 31 * result + isolatedLoad.hashCode()
        result = 31 * result + (id?.hashCode() ?: 0)
        return result
    }

    companion object {
        @JvmStatic
        fun builder(): LibraryBuilder = LibraryBuilder()
    }
}
