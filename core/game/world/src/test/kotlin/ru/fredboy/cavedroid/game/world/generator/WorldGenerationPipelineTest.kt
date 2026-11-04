package ru.fredboy.cavedroid.game.world.generator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.game.world.generator.verification.SnapshotCases
import ru.fredboy.cavedroid.game.world.generator.verification.SnapshotItems
import ru.fredboy.cavedroid.game.world.generator.verification.TerrainMaps
import kotlin.random.Random

class WorldGenerationPipelineTest {
    @Test
    fun `middleware runs in order with independent salted random streams`() {
        val config = WorldGeneratorConfig.getDefault(seed = 42)
        val context = WorldGenerationContext(config, GeneratorTestItems(), 0, 0)
        val order = mutableListOf<String>()
        val values = mutableListOf<Int>()
        fun stage(name: String, draws: Int) = object : WorldGenerationMiddleware {
            override val id = name
            override val salt = 123L
            override fun generate(context: WorldGenerationContext, random: GenerationRandom) {
                order += id
                val stream = random.stream()
                values += stream.nextInt()
                repeat(draws) { stream.nextInt() }
            }
        }
        val pipeline = WorldGenerationPipeline(listOf(stage("first", 1000), stage("second", 0)))
        repeat(2) {
            pipeline.generate(context) { random ->
                order += "basic"
                assertEquals(Random(config.seed).nextInt(), random.stream().nextInt())
            }
        }
        assertEquals(listOf("basic", "first", "second", "basic", "first", "second"), order)
        assertEquals(List(4) { Random(config.seed xor 123L).nextInt() }, values)
        assertEquals(Random(42L xor (-7L * 341873128712L) xor 123L).nextInt(), GenerationRandom(42, 123).region(-7).nextInt())
    }

    @Test
    fun `spatial requirements compose and duplicate middleware is rejected`() {
        val stage = object : WorldGenerationMiddleware {
            override val id = "large_feature"
            override val regionAlignment = 192
            override val horizontalReach = 20
            override fun generate(context: WorldGenerationContext, random: GenerationRandom) = Unit
        }
        val pipeline = WorldGenerationPipeline(listOf(stage))
        assertEquals(192, pipeline.regionAlignment)
        assertEquals(20, pipeline.horizontalReach)
        assertThrows(IllegalArgumentException::class.java) { WorldGenerationPipeline(listOf(stage, stage)) }
        assertTrue(pipeline.regionAlignment % MonsterRoomGenerator.REGION_WIDTH == 0)
    }

    @Test
    fun `looping generator can be reused and village metadata calls do not advance terrain RNG`() {
        val items = SnapshotItems(requireNotNull(javaClass.getResource("/json/game_items.json")).readText())
        val case = SnapshotCases.cases.first()
        val generator = GameWorldGenerator(case.config, items)
        val before = generator.villageBeds()
        fun capture() = SnapshotCases.capture(case, items, looping = {
            generator.generate().let { TerrainMaps(it.foreMap, it.backMap, it.biomes) }
        })
        val first = capture()
        assertEquals(first, capture())
        assertEquals(before, generator.villageBeds())
    }
}
