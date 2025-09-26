object Dependencies {

    object LibGDX {
        const val gdx = "com.badlogicgames.gdx:gdx:${Versions.gdx}"

        const val box2dLights = "com.badlogicgames.box2dlights:box2dlights:${Versions.box2dLights}"

        object Box2d {
            const val box2d = "com.badlogicgames.gdx:gdx-box2d:${Versions.gdx}"

            object Natives {
                object Android {
                    const val armeabi = "com.badlogicgames.gdx:gdx-box2d-platform:${Versions.gdx}:natives-armeabi-v7a"
                    const val arm64 = "com.badlogicgames.gdx:gdx-box2d-platform:${Versions.gdx}:natives-arm64-v8a"
                    const val x86 = "com.badlogicgames.gdx:gdx-box2d-platform:${Versions.gdx}:natives-x86"
                    const val x86_64 = "com.badlogicgames.gdx:gdx-box2d-platform:${Versions.gdx}:natives-x86_64"
                }

                const val desktop = "com.badlogicgames.gdx:gdx-box2d-platform:${Versions.gdx}:natives-desktop"
                const val ios = "com.badlogicgames.gdx:gdx-box2d-platform:${Versions.gdx}:natives-ios"
            }
        }

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

        object Ios {
            const val backend = "com.badlogicgames.gdx:gdx-backend-robovm:${Versions.gdx}"
            const val natives = "com.badlogicgames.gdx:gdx-platform:${Versions.gdx}:natives-ios"
        }
    }

    object LibKTX {
        const val scene2d = "io.github.libktx:ktx-scene2d:${Versions.libKtx}"
        const val actors = "io.github.libktx:ktx-actors:${Versions.libKtx}"
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
        const val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinxCoroutines}"

        object Serialization {
            const val json = "org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.kotlinxSerialization}"
            const val protobuf = "org.jetbrains.kotlinx:kotlinx-serialization-protobuf:${Versions.kotlinxSerialization}"
        }
    }

    object Automultibind {
        const val annotations = "ru.fredboy:automultibind-annotations:${Versions.automultibind}"
        const val ksp = "ru.fredboy:automultibind-ksp:${Versions.automultibind}"
    }

    object RoboVM {
        const val rt = "com.mobidevelop.robovm:robovm-rt:${Versions.roboVM}"
        const val cocoatouch = "com.mobidevelop.robovm:robovm-cocoatouch:${Versions.roboVM}"
        const val gradlePlugin = "com.mobidevelop.robovm:robovm-gradle-plugin:${Versions.roboVM}"
    }

    const val androidGradlePlugin = "com.android.tools.build:gradle:${Versions.agp}"

    const val proGuardPlugin = "com.guardsquare:proguard-gradle:${Versions.proGuard}"

    const val kermit = "co.touchlab:kermit:${Versions.kermit}"
}
