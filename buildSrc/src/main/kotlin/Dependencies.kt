import org.gradle.kotlin.dsl.version

object Dependencies {

    object LibGDX {
        const val gdx = "com.badlogicgames.gdx:gdx:${Versions.gdx}"

        object Android {
            const val backend = "com.badlogicgames.gdx:gdx-backend-android:${Versions.gdx}"

            object Natives {
                const val armeabi = "com.badlogicgames.gdx:gdx-platform:${Versions.gdx}:natives-armeabi-v7a"
                const val arm64 = "com.badlogicgames.gdx:gdx-platform:${Versions.gdx}:natives-arm64-v8a"
                const val x86 = "com.badlogicgames.gdx:gdx-platform:${Versions.gdx}:natives-x86"
                const val x86_64 = "com.badlogicgames.gdx:gdx-platform:${Versions.gdx}:natives-x86_64"
            }
        }

        object Desktop {
            const val backend = "com.badlogicgames.gdx:gdx-backend-lwjgl3:${Versions.gdx}"
            const val natives = "com.badlogicgames.gdx:gdx-platform:${Versions.gdx}:natives-desktop"
        }
    }

    object Dagger {
        const val dagger = "com.google.dagger:dagger:${Versions.dagger}"
        const val compiler = "com.google.dagger:dagger-compiler:${Versions.dagger}"
    }

    object Kotlin {
        const val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
        const val kspPlugin = "com.google.devtools.ksp:${Versions.ksp}"
        const val bom = "org.jetbrains.kotlin:kotlin-bom:${Versions.kotlin}"
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}"

        object Serialization {
            const val json = "org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.kotlinxSerialization}"
            const val protobuf = "org.jetbrains.kotlinx:kotlinx-serialization-protobuf:${Versions.kotlinxSerialization}"
        }
    }

    object Automultibind {
        const val annotations = "ru.fredboy:automultibind-annotations:${Versions.automultibind}"
        const val ksp = "ru.fredboy:automultibind-ksp:${Versions.automultibind}"
    }

    const val androidGradlePlugin = "com.android.tools.build:gradle:${Versions.agp}"

    // TODO: Remove after complete kotlin migration
    const val jetbrainsAnnotations = "org.jetbrains:annotations:${Versions.jetbrainsAnnotations}"

}