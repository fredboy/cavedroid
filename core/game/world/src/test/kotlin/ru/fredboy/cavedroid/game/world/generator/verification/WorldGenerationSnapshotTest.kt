package ru.fredboy.cavedroid.game.world.generator.verification

import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

class WorldGenerationSnapshotTest {
    @TestFactory
    fun `basic terrain matches the release baseline`(): List<DynamicTest> {
        val definitions = requireNotNull(javaClass.getResource("/json/game_items.json")).readText()
        val items = SnapshotItems(definitions)
        return SnapshotCases.cases.map { case ->
            DynamicTest.dynamicTest("${case.id}, seed=${case.config.seed}") {
                val actual = SnapshotCases.capture(case, items)
                val resource = requireNotNull(javaClass.getResourceAsStream("/world-generation/${case.id}.bin.gz")) {
                    "Missing release terrain snapshot: ${case.id}"
                }
                val expected = TerrainSnapshot.read(resource)
                val differences = expected.differences(actual)
                assertNull(differences, "${case.id}, seed=${case.config.seed}: $differences")
            }
        }
    }
}
