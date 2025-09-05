plugins {
    kotlin("jvm") version "1.9.20" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
    `java-library`
}

allprojects {
    group = "com.hiusers.mc.linker"
    version = "1.0.0"

    repositories {
        mavenCentral()
        maven("https://hub.spigotmc.org/nexus/content/groups/public/")
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "java-library")

    dependencies {
        compileOnly(kotlin("stdlib"))
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            freeCompilerArgs.addAll("-Xjsr305=strict")
        }
    }

    // 禁用子模块的 jar 任务，只在聚合模块生成
    tasks.jar {
        enabled = false
    }
}

// 禁用根项目的默认 jar 任务
tasks.jar {
    enabled = false
}