package ru.fredboy.cavedroid.html

import com.github.xpenatan.gdx.teavm.backends.shared.config.AssetFileHandle
import com.github.xpenatan.gdx.teavm.backends.shared.config.compiler.TeaCompiler
import com.github.xpenatan.gdx.teavm.backends.web.config.backend.WebBackend
import org.teavm.tooling.sources.DirectorySourceFileProvider
import org.teavm.vm.TeaVMOptimizationLevel
import java.io.File

object BuildWebJs {

    @JvmStatic
    fun main(args: Array<String>) {
        val assetsPath = System.getProperty("cavedroid.assetsPath")
            ?: error("cavedroid.assetsPath system property must be set by the gradle task.")
        val extraAssetsPath = System.getProperty("cavedroid.extraAssetsPath")
        val launcherClass = System.getProperty("cavedroid.launcherClass")
            ?: error("cavedroid.launcherClass system property must be set by the gradle task.")
        val serve = System.getProperty("cavedroid.serve")?.toBooleanStrictOrNull() == true
        val sourceMaps = System.getProperty("cavedroid.sourceMaps")?.toBooleanStrictOrNull() == true
        val obfuscate = System.getProperty("cavedroid.obfuscate")?.toBooleanStrictOrNull() == true
        val optimization = System.getProperty("cavedroid.optimization")
            ?.let(TeaVMOptimizationLevel::valueOf)
            ?: TeaVMOptimizationLevel.SIMPLE
        val sourceRoots = System.getProperty("cavedroid.sourceRoots")
            ?.split(File.pathSeparator)
            .orEmpty()
            .filter { it.isNotEmpty() }
            .map(::File)
            .filter(File::isDirectory)

        val backend = WebBackend().setStartJettyAfterBuild(serve)

        val compiler = TeaCompiler(backend)
            .addAssets(AssetFileHandle(assetsPath))

        if (!extraAssetsPath.isNullOrEmpty()) {
            compiler.addAssets(AssetFileHandle(extraAssetsPath))
        }

        compiler
            .setOptimizationLevel(optimization)
            .setMainClass(launcherClass)
            .setObfuscated(obfuscate)
            .setSourceMapsFileGenerated(sourceMaps)
            .setDebugInformationGenerated(sourceMaps)

        if (sourceMaps) {
            sourceRoots.forEach { compiler.addSourceFileProvider(DirectorySourceFileProvider(it)) }
        }

        compiler.build(File("build/dist"))
    }
}
