plugins {
    id("com.github.johnrengelman.shadow")
}

dependencies {
    implementation(project(":project:core"))
    implementation(project(":project:bukkit"))
    implementation(project(":project:paper"))
}

tasks.shadowJar {
    // 最小化
    minimize()

    archiveBaseName.set("Linker")
    archiveClassifier.set("")
    destinationDirectory.set(rootProject.layout.buildDirectory.dir("target"))

    // 显式包含所有子项目的输出
    from(project(":project:core").sourceSets.main.get().output)
    from(project(":project:bukkit").sourceSets.main.get().output)
    from(project(":project:paper").sourceSets.main.get().output)

    // 重定位配置
    relocate("me.lucko.jarrelocator", "com.hiusers.mc.linker.libs.jarrelocator")
    relocate("org.objectweb.asm", "com.hiusers.mc.linker.libs.asm")

    // 排除不需要的类
    exclude("org/bukkit/**")
    exclude("org/spigotmc/**")
    exclude("io/papermc/**")
    exclude("org/jetbrains/")
    exclude("org/intellij/")
}

// 确保 build 任务依赖 shadowJar
tasks.build {
    dependsOn(tasks.shadowJar)
}

// 禁用普通 jar 任务
tasks.jar {
    enabled = false
}