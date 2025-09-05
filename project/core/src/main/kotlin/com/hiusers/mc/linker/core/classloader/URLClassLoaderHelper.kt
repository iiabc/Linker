package com.hiusers.mc.linker.core.classloader

import com.hiusers.mc.linker.core.Library
import com.hiusers.mc.linker.core.LibraryManager
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Path
import java.util.*

/**
 * @author iiabc
 * @since 2025/9/4 21:59
 */
class URLClassLoaderHelper(
    classLoader: URLClassLoader,
    libraryManager: LibraryManager
) {
    companion object {
        // Unsafe 实例，用于特权访问
        private val theUnsafe: sun.misc.Unsafe? = run {
            var unsafe: sun.misc.Unsafe? = null

            // 动态查找 theUnsafe 字段
            for (field in sun.misc.Unsafe::class.java.declaredFields) {
                try {
                    if (field.type == sun.misc.Unsafe::class.java && Modifier.isStatic(field.modifiers)) {
                        field.isAccessible = true
                        unsafe = field.get(null) as sun.misc.Unsafe
                        break
                    }
                } catch (_: Exception) {
                }
            }
            unsafe
        }
    }

    private var addURLMethodHandle: MethodHandle

    init {
        try {
            val addURLMethod = URLClassLoader::class.java.getDeclaredMethod("addURL", URL::class.java)

            // 尝试开放模块
            try {
                openUrlClassLoaderModule()
            } catch (_: Exception) {
            }

            var methodHandle: MethodHandle? = null

            try {
                addURLMethod.isAccessible = true
                methodHandle = MethodHandles.lookup().unreflect(addURLMethod).bindTo(classLoader)
            } catch (exception: Exception) {
                // Java 9+ InaccessibleObjectException
                if (exception.javaClass.name == "java.lang.reflect.InaccessibleObjectException") {

                    // 尝试 Unsafe 特权访问
                    if (theUnsafe != null) {
                        methodHandle = try {
                            getPrivilegedMethodHandle(addURLMethod).bindTo(classLoader)
                        } catch (_: Exception) {
                            null
                        }
                    }

                    // 如果 Unsafe 失败，尝试 ByteBuddy Agent 回退
                    if (methodHandle == null) {
                        try {
                            addOpensWithAgent(libraryManager)
                            addURLMethod.isAccessible = true
                            methodHandle = MethodHandles.lookup().unreflect(addURLMethod).bindTo(classLoader)
                        } catch (e: Exception) {
                            throw RuntimeException(
                                "Cannot access URLClassLoader#addURL(URL). All fallback methods failed.",
                                e
                            )
                        }
                    }
                } else {
                    throw RuntimeException("Cannot set accessible URLClassLoader#addURL(URL)", exception)
                }
            }

            addURLMethodHandle = methodHandle ?: throw RuntimeException("Failed to obtain method handle")
        } catch (e: Exception) {
            throw RuntimeException("Failed to initialize URLClassLoaderHelper", e)
        }
    }

    fun addToClasspath(url: URL) {
        try {
            addURLMethodHandle.invokeWithArguments(url)
        } catch (e: Throwable) {
            throw RuntimeException(e)
        }
    }

    fun addToClasspath(path: Path) {
        addToClasspath(path.toUri().toURL())
    }

    // 开放实现
    private fun openUrlClassLoaderModule() {
        val moduleClass = Class.forName("java.lang.Module")
        val getModuleMethod = Class::class.java.getMethod("getModule")
        val addOpensMethod = moduleClass.getMethod("addOpens", String::class.java, moduleClass)

        val urlClassLoaderModule = getModuleMethod.invoke(URLClassLoader::class.java)
        val thisModule = getModuleMethod.invoke(URLClassLoaderHelper::class.java)

        addOpensMethod.invoke(urlClassLoaderModule, URLClassLoader::class.java.`package`.name, thisModule)
    }

    // Unsafe 特权访问实现
    private fun getPrivilegedMethodHandle(method: Method): MethodHandle {
        // 查找 MethodHandles.Lookup.IMPL_LOOKUP 字段
        for (field in MethodHandles.Lookup::class.java.declaredFields) {
            if (field.type == MethodHandles.Lookup::class.java &&
                Modifier.isStatic(field.modifiers) &&
                !field.isSynthetic
            ) {
                try {
                    val lookup = theUnsafe!!.getObject(
                        theUnsafe.staticFieldBase(field),
                        theUnsafe.staticFieldOffset(field)
                    ) as MethodHandles.Lookup
                    return lookup.unreflect(method)
                } catch (_: Exception) {
                }
            }
        }
        throw RuntimeException("Cannot get privileged method handle.")
    }

    private fun addOpensWithAgent(libraryManager: LibraryManager) {
        val isolatedClassLoader = IsolatedClassLoader()
        try {
            // 下载 ByteBuddy Agent
            isolatedClassLoader.addPath(
                libraryManager.downloadLibrary(
                    Library.builder()
                        .groupId("net.bytebuddy")
                        .artifactId("byte-buddy-agent")
                        .version("1.12.1")
                        .checksum("mcCtBT9cljUEniB5ESpPDYZMfVxEs1JRPllOiWTP+bM=")
                        .build()
                )
            )

            val byteBuddyAgent = isolatedClassLoader.loadClass("net.bytebuddy.agent.ByteBuddyAgent")

            // 安装 Agent 并重新定义模块
            val instrumentation = byteBuddyAgent.getDeclaredMethod("install").invoke(null)
            val instrumentationClass = Class.forName("java.lang.instrument.Instrumentation")
            val redefineModule = instrumentationClass.getDeclaredMethod(
                "redefineModule",
                Class.forName("java.lang.Module"),
                Set::class.java,
                Map::class.java,
                Map::class.java,
                Set::class.java,
                Map::class.java
            )
            val getModule = Class::class.java.getDeclaredMethod("getModule")
            val toOpen = Collections.singletonMap(
                "java.net",
                Collections.singleton(getModule.invoke(javaClass))
            )

            redefineModule.invoke(
                instrumentation,
                getModule.invoke(URLClassLoader::class.java),
                Collections.emptySet<Any>(),
                Collections.emptyMap<Any, Any>(),
                toOpen,
                Collections.emptySet<Any>(),
                Collections.emptyMap<Any, Any>()
            )
        } finally {
            try {
                isolatedClassLoader.close()
            } catch (_: Exception) {
            }
        }
    }
}