# Linker

> [English](README.md) | 中文

## 特性

- 🚀 **运行时依赖下载**：从 Maven 仓库动态下载依赖，避免打包到插件 JAR 中
- 🔄 **包重定位**：自动应用包重定位规则避免依赖冲突
- 🔒 **校验和验证**：支持 SHA-256 校验和确保下载文件完整性
- 🎯 **多平台支持**：支持 Bukkit、Spigot、Paper 等平台

## 为什么使用运行时依赖管理？

- **减小插件体积**：避免将大型依赖打包到插件 JAR 中
- **避免上传限制**：绕过插件托管服务的文件大小限制
- **减少网络负担**：依赖被缓存在服务器本地，减少重复下载

## 快速开始

### 添加依赖

```kotlin
repositories {
    maven("https://repo.hiusers.com/releases")
}

dependencies {
    implementation("com.hiusers.mc:Linker:1.0.0")
}
```

### 依赖重定位

**重要**：始终对 Linker 进行重定位以避免冲突：

```kotlin
relocate("com.hiusers.mc.linker", "com.yourplugin.libs.linker")
```

### 基本用法

```kotlin
// 创建 LibraryManager
val libraryManager = BukkitLibraryManager.create(plugin)

// 添加 Maven Central 仓库
libraryManager.addMavenCentral()

// 定义要加载的库
val library = Library.builder()
    .groupId("com.google.code.gson")
    .artifactId("gson")
    .version("2.10.1")
    // 避免冲突
    .relocate("com.google.gson", "com.yourplugin.libs.gson")
    // 可选的完整性验证
    .checksum("your-sha256-checksum")
    .build()

// 加载库到插件类路径
libraryManager.loadLibrary(library)
```

### 高级配置

#### 隔离类加载

```kotlin
val library = Library.builder()
    .groupId("com.example")
    .artifactId("library")
    .version("1.0.0")
    // 加载到隔离的类加载器
    .isolatedLoad(true)
    // 相同 ID 的库共享类加载器
    .id("shared-lib")
    .build()
```

## 平台支持

- Bukkit/Spigot
- Paper

## 构建

```bash
./gradlew clean build
```

生成的 JAR 文件位于 `build/target/Linker-{version}.jar`

## 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件。