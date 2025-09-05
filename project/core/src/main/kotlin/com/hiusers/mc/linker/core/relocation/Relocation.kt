package com.hiusers.mc.linker.core.relocation

/**
 * @author iiabc
 * @since 2025/9/4 21:52
 */
data class Relocation(
    val pattern: String,
    val relocatedPattern: String,
    val includes: Collection<String> = emptyList(),
    val excludes: Collection<String> = emptyList()
)