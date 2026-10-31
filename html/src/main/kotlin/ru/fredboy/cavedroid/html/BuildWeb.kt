package ru.fredboy.cavedroid.html

import com.github.xpenatan.gdx.teavm.backends.shared.config.AssetFileHandle
import com.github.xpenatan.gdx.teavm.backends.shared.config.builder.TeaBuilder
import com.github.xpenatan.gdx.teavm.backends.web.config.backend.WebBackend
import org.teavm.tooling.sources.DirectorySourceFileProvider
import org.teavm.vm.TeaVMOptimizationLevel
import java.io.File

object BuildWeb {

    @JvmStatic
    fun main(args: Array<String>) {
        val assetsPath = System.getProperty("cavedroid.assetsPath")
            ?: error("cavedroid.assetsPath system property must be set by the gradle task.")
        val extraAssetsPath = System.getProperty("cavedroid.extraAssetsPath")
        val launcherClass = System.getProperty("cavedroid.launcherClass")
            ?: error("cavedroid.launcherClass system property must be set by the gradle task.")
        val serve = System.getProperty("cavedroid.serve")?.toBooleanStrictOrNull() == true
        val wasm = System.getProperty("cavedroid.wasm")?.toBooleanStrictOrNull() == true
        val outputPath = System.getProperty("cavedroid.outputPath")
            ?: error("cavedroid.outputPath system property must be set by the gradle task.")
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

        val backend = WebBackend()
            .setWebAssembly(wasm)
            .setStartJettyAfterBuild(serve)

        val builder = TeaBuilder(backend)
            .addAssets(AssetFileHandle(assetsPath))

        if (!extraAssetsPath.isNullOrEmpty()) {
            builder.addAssets(AssetFileHandle(extraAssetsPath))
        }

        builder
            .setOptimizationLevel(optimization)
            .setMainClass(launcherClass)
            .setObfuscated(obfuscate)
            .setSourceMapsFileGenerated(sourceMaps)
            .setDebugInformationGenerated(sourceMaps)

        if (sourceMaps) {
            sourceRoots.forEach { builder.addSourceFileProvider(DirectorySourceFileProvider(it)) }
        }

        builder.build(File(outputPath))
    }
}
