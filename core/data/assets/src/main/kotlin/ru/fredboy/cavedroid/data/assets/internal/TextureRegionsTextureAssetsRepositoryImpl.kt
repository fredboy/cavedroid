package ru.fredboy.cavedroid.data.assets.internal

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.TextureRegion
import kotlinx.serialization.json.Json
import ru.fredboy.cavedroid.data.assets.model.region.TextureRegionsDto
import ru.fredboy.cavedroid.domain.assets.repository.TextureRegionsTextureAssetsRepository
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class TextureRegionsTextureAssetsRepositoryImpl @Inject constructor() : TextureRegionsTextureAssetsRepository() {

    private var textureRegions: HashMap<String, TextureRegion>? = null

    private fun loadTextureRegions() {
        val file = Gdx.files.internal(JSON_TEXTURE_REGIONS)
        val dto = JsonFormat.decodeFromString<TextureRegionsDto>(file.readString())

        val regions = HashMap<String, TextureRegion>()

        dto.forEach { (fileName, fileData) ->
            val texture = loadTexture("$fileName.png")

            if (fileData.isEmpty()) {
                regions[fileName.split('/').last()] = flippedRegion(
                    texture = texture,
                    x = 0,
                    y = 0,
                    width = texture.width,
                    height = texture.height,
                )
            } else {
                fileData.forEach { (regionName, regionData) ->
                    regions[regionName.split(File.separator).last()] = flippedRegion(
                        texture = texture,
                        x = regionData.x,
                        y = regionData.y,
                        width = regionData.width ?: texture.width,
                        height = regionData.height ?: texture.height,
                    )
                }
            }
        }

        textureRegions = regions
    }

    override fun initialize() {
        loadTextureRegions()
    }

    override fun getTextureRegionByName(name: String): TextureRegion? = requireNotNull(textureRegions)[name]

    override fun dispose() {
        super.dispose()
        textureRegions = null
    }

    companion object {
        private val JsonFormat = Json { ignoreUnknownKeys = true }

        private const val JSON_TEXTURE_REGIONS = "json/texture_regions.json"
    }
}
