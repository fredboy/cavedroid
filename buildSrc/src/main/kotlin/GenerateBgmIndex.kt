import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.jaudiotagger.audio.AudioFileIO

@CacheableTask
abstract class GenerateBgmIndex : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val tracks: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val index = tracks.files.sortedBy { it.name }.associate { track ->
            val duration = try {
                AudioFileIO.read(track).audioHeader.preciseTrackLength.also {
                    require(it.isFinite() && it > 0) { "Invalid track duration: $it" }
                }
            } catch (exception: Exception) {
                throw GradleException("Cannot read BGM duration from ${track.name}: ${exception.message}", exception)
            }
            track.name to duration
        }
        val output = outputDirectory.file("bgm/index.json").get().asFile
        output.parentFile.mkdirs()
        output.writeText(JsonOutput.prettyPrint(JsonOutput.toJson(index)) + "\n")
        logger.lifecycle("Generated BGM index with ${index.size} tracks: $output")
    }
}
