package ru.fredboy.cavedroid.html

import com.github.xpenatan.gdx.teavm.backends.shared.config.AssetFileHandle
import com.github.xpenatan.gdx.teavm.backends.shared.config.compiler.TeaCompiler
import com.github.xpenatan.gdx.teavm.backends.web.config.backend.WebBackend
import org.teavm.vm.TeaVMOptimizationLevel
import java.io.File

object BuildWebJs {

    @JvmStatic
    fun main(args: Array<String>) {
        val assetsPath = System.getProperty("cavedroid.assetsPath")
            ?: error("cavedroid.assetsPath system property must be set by the gradle task.")
        val launcherClass = System.getProperty("cavedroid.launcherClass")
            ?: error("cavedroid.launcherClass system property must be set by the gradle task.")
        val serve = System.getProperty("cavedroid.serve")?.toBooleanStrictOrNull() == true

        val backend = WebBackend().setStartJettyAfterBuild(serve)

        TeaCompiler(backend)
            .addAssets(AssetFileHandle(assetsPath))
            .setOptimizationLevel(TeaVMOptimizationLevel.SIMPLE)
            .setMainClass(launcherClass)
            .setObfuscated(false)
            .build(File("build/dist"))
    }
}
