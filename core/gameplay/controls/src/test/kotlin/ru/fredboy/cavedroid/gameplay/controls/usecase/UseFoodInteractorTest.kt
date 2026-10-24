package ru.fredboy.cavedroid.gameplay.controls.usecase

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import ru.fredboy.cavedroid.common.api.SoundPlayer
import ru.fredboy.cavedroid.domain.assets.repository.FoodSoundAssetsRepository
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.block.CommonBlockParams
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.world.GameWorld

class UseFoodInteractorTest {

    private val world = mockk<GameWorld>(relaxed = true)
    private val repository = mockk<ItemsRepository>()
    private val player = mockk<Player>(relaxed = true)
    private val sounds = mockk<FoodSoundAssetsRepository> {
        every { getFoodSound() } returns null
    }
    private val soundPlayer = mockk<SoundPlayer>(relaxed = true)
    private val interactor = UseFoodInteractor(soundPlayer, sounds, PlantCropInteractor(world, repository))
    private val air = Block.None(mockk(relaxed = true))

    init {
        every { world.getForeMap(any(), any()) } returns air
        every { player.selectedX } returns 5
        every { player.selectedY } returns 10
    }

    @ParameterizedTest
    @CsvSource(
        "carrots_stage0, farmland, 10",
        "carrots_stage0, farmland_moist, 20",
        "potatoes_stage0, farmland, 20",
        "potatoes_stage0, farmland_moist, 10",
    )
    fun `using food on farmland plants above it without eating`(cropKey: String, soilKey: String, hunger: Int) {
        holdFood(cropKey, hunger)
        val crop = block(cropKey)
        every { repository.getBlockByKey(cropKey) } returns crop
        every { world.getForeMap(5, 10) } returns block(soilKey)

        assertTrue(interactor.execute(player))

        verify(exactly = 1) { world.setForeMap(5, 9, crop) }
        verify(exactly = 1) { player.decreaseCurrentItemCount(1) }
        verify(exactly = 0) { player.eat(any(), any()) }
        verify(exactly = 0) { sounds.getFoodSound() }
    }

    @ParameterizedTest
    @CsvSource("carrots_stage0, farmland", "potatoes_stage0, farmland_moist")
    fun `using food on air above farmland plants in the selected cell`(cropKey: String, soilKey: String) {
        holdFood(cropKey, Player.MAX_FOOD_LEVEL)
        val crop = block(cropKey)
        every { repository.getBlockByKey(cropKey) } returns crop
        every { world.getForeMap(5, 11) } returns block(soilKey)

        assertTrue(interactor.execute(player))

        verify(exactly = 1) { world.setForeMap(5, 10, crop) }
        verify(exactly = 1) { player.decreaseCurrentItemCount(1) }
        verify(exactly = 0) { player.eat(any(), any()) }
    }

    @Test
    fun `raw food is eaten when the selected soil cannot grow crops`() {
        holdFood("carrots_stage0", 10)
        every { world.getForeMap(5, 11) } returns block("dirt")

        assertTrue(interactor.execute(player))

        verify(exactly = 1) { player.eat(3, 3.6f) }
        verify(exactly = 1) { player.decreaseCurrentItemCount(1) }
        verify(exactly = 0) { world.setForeMap(any(), any(), any()) }
    }

    @Test
    fun `occupied farmland cannot replace a crop or consume food at full hunger`() {
        holdFood("potatoes_stage0", Player.MAX_FOOD_LEVEL)
        every { world.getForeMap(5, 10) } returns block("farmland")
        every { world.getForeMap(5, 9) } returns block("wheat_stage3")

        assertFalse(interactor.execute(player))

        verify(exactly = 0) { world.setForeMap(any(), any(), any()) }
        verify(exactly = 0) { player.decreaseCurrentItemCount(any()) }
        verify(exactly = 0) { player.eat(any(), any()) }
    }

    @Test
    fun `ordinary food is eaten even when pointing at farmland`() {
        holdFood(null, 10)
        every { world.getForeMap(5, 10) } returns block("farmland")

        assertTrue(interactor.execute(player))

        verify(exactly = 1) { player.eat(3, 3.6f) }
        verify(exactly = 0) { world.setForeMap(any(), any(), any()) }
    }

    @Test
    fun `ordinary food is not consumed at full hunger`() {
        holdFood(null, Player.MAX_FOOD_LEVEL)

        assertFalse(interactor.execute(player))

        verify(exactly = 0) { player.decreaseCurrentItemCount(any()) }
        verify(exactly = 0) { player.eat(any(), any()) }
    }

    private fun holdFood(cropKey: String?, hunger: Int) {
        val food = Item.Food(mockk(), mockk(), 3, 3.6f, cropKey)
        every { player.activeItem } returns InventoryItem(food)
        every { player.foodLevel } returns hunger
    }

    private fun block(key: String): Block.Normal {
        val params = mockk<CommonBlockParams>(relaxed = true)
        every { params.key } returns key
        return Block.Normal(params)
    }
}
