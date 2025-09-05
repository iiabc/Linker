# Linker

> English | [中文](README_zh_CN.md)

## Features

- 🚀 **Runtime Dependency Download**: Dynamically download dependencies from Maven repositories, eliminating the need to
  package them into the plugin JAR.
- 🔄 **Package Relocation**: Automatically apply package relocation rules to avoid dependency conflicts.
- 🔒 **Checksum Verification**: Support SHA-256 checksums to ensure the integrity of downloaded files.
- 🎯 **Multi-Platform Support**: Compatible with platforms like Bukkit, Spigot, and Paper.

## Why Use Runtime Dependency Management?

- **Reduce Plugin Size**: Avoid packaging large dependencies into the plugin JAR.
- **Bypass Upload Limits**: Circumvent file size restrictions on plugin hosting services.
- **Minimize Network Load**: Dependencies are cached locally on the server, reducing repeated downloads.

## Quick Start

### Add Dependency

```kotlin
repositories {
    maven("https://repo.hiusers.com/releases")
}

dependencies {
    implementation("com.hiusers.mc:Linker:1.0.0")
}
```

### Dependency Relocation

**Important**: Always relocate Linker to prevent conflicts:

```kotlin
relocate("com.hiusers.mc.linker", "com.yourplugin.libs.linker")
```

### Basic Usage

```kotlin
// Create LibraryManager
val libraryManager = BukkitLibraryManager.create(plugin)

// Add Maven Central repository
libraryManager.addMavenCentral()

// Define the library to load
val library = Library.builder()
    .groupId("com.google.code.gson")
    .artifactId("gson")
    .version("2.10.1")
    // Avoid conflicts
    .relocate("com.google.gson", "com.yourplugin.libs.gson")
    // Optional integrity verification
    .checksum("your-sha256-checksum")
    .build()

// Load the library into the plugin's classpath
libraryManager.loadLibrary(library)
```

### Advanced Configuration

#### Isolated Class Loading

```kotlin
val library = Library.builder()
    .groupId("com.example")
    .artifactId("library")
    .version("1.0.0")
    // Load into an isolated class loader
    .isolatedLoad(true)
    // Libraries with the same ID share the class loader
    .id("shared-lib")
    .build()
```

## Supported Platforms

- Bukkit/Spigot
- Paper

## Build

```bash
./gradlew clean build
```

The generated JAR file is located at `build/target/Linker-{version}.jar`

## License

This project is licensed under MIT - see the [LICENSE](LICENSE) file for details.