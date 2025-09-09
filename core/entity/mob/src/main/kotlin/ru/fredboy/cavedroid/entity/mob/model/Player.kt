package ru.fredboy.cavedroid.entity.mob.model

import box2dLight.Light
import box2dLight.PointLight
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Filter
import ru.fredboy.cavedroid.common.api.SoundPlayer
import ru.fredboy.cavedroid.common.model.GameMode
import ru.fredboy.cavedroid.common.utils.TooltipManager
import ru.fredboy.cavedroid.common.utils.applyOrigin
import ru.fredboy.cavedroid.common.utils.drawSprite
import ru.fredboy.cavedroid.common.utils.meters
import ru.fredboy.cavedroid.domain.assets.repository.StepsSoundAssetsRepository
import ru.fredboy.cavedroid.domain.items.model.inventory.Inventory
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.model.mob.MobParams
import ru.fredboy.cavedroid.domain.items.usecase.GetFallbackItemUseCase
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import ru.fredboy.cavedroid.domain.world.model.PhysicsConstants
import ru.fredboy.cavedroid.entity.mob.abstraction.MobPhysicsFactory
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter
import ru.fredboy.cavedroid.entity.mob.impl.PlayerMobBehavior

class Player(
    private val getFallbackItem: GetFallbackItemUseCase,
    private val tooltipManager: TooltipManager,
    params: MobParams,
    soundPlayer: SoundPlayer,
    stepsSoundAssetsRepository: StepsSoundAssetsRepository,

) : Mob(Direction.random(), params, PlayerMobBehavior(soundPlayer, stepsSoundAssetsRepository)) {

    var spawnPoint: Vector2? = null

    var inventory = Inventory(
        size = INVENTORY_SIZE,
        fallbackItem = getFallbackItem(),
    )
        set(value) {
            value.setOnItemAddedListener { item, slot ->
                if (slot == activeSlot) {
                    tooltipManager.showHotbarTooltip(item.item.params.name)
                }
            }
            field = value
        }

    var gameMode = GameMode.SURVIVAL

    var cursorX = 0f
    var cursorY = 0f

    var holdCursor = false
    var cursorToPlayer = Vector2()

    val selectedX get() = cursorX.toInt()
    val selectedY get() = cursorY.toInt()

    var controlMode = ControlMode.CURSOR
    var blockDamage = 0f

    var isHitting = false
    var isHittingWithDamage = false

    var hitAnim = 0f
    var hitAnimDelta = DEFAULT_ANIMATION_SPEED

    var headRotation = 0f

    private var _activeSlot = 0

    var isInBed = false

    var activeSlot
        get() = _activeSlot
        set(value) {
            if (value in 0..<HOTBAR_SIZE) {
                if (value != _activeSlot) {
                    tooltipManager.showHotbarTooltip(inventory.items[value].item.params.name)
                }
                _activeSlot = value
            }
        }

    val hotbarItems get() = inventory.items.subList(0, HOTBAR_SIZE)

    val activeItem get() = inventory.items[activeSlot]

    var sight: Light? = null
        set(value) {
            if (field != null) {
                field?.remove(true)
                field = null
            }

            field = value
        }

    override fun changeDir() = Unit

    override fun damage(damage: Int) {
        if (gameMode.isCreative()) {
            return
        }

        super.damage(damage)
    }

    override fun getDropItems(itemByKey: GetItemByKeyUseCase): List<InventoryItem> = inventory.items

    override fun draw(spriteBatch: SpriteBatch, x: Float, y: Float, delta: Float) {
        updateAnimation(delta)

        var backHandAnim: Float
        var frontHandAnim: Float

        val rightHandAnim = getRightHandAnim(delta)

        val backgroundTintColor = tintColor.cpy().sub(Color(0xAAAAAA shl 8))

        if (looksLeft()) {
            backHandAnim = rightHandAnim
            frontHandAnim = anim
        } else {
            backHandAnim = -anim
            frontHandAnim = -rightHandAnim
        }

        params.sprites.forEach { spriteData ->
            val sprite = spriteData.sprite

            sprite.setFlip(looksRight(), sprite.isFlipY)

            if (!isInBed) {
                sprite.applyOrigin(spriteData.origin)
            }

            if (spriteData.isBackground) {
                sprite.color = backgroundTintColor
            } else {
                sprite.color = tintColor
            }

            val animationValue = if (isInBed) {
                90f
            } else if (spriteData.isStatic) {
                0f
            } else if (spriteData.isHead) {
                headRotation
            } else if (spriteData.isHand && spriteData.isBackground) {
                backHandAnim
            } else if (spriteData.isHand) {
                frontHandAnim
            } else if (spriteData.isBackground) {
                anim
            } else {
                -anim
            }

            val spriteX = if (isInBed) {
                x - spriteData.offsetY
            } else {
                x + spriteData.offsetX
            }

            val spriteY = if (isInBed) {
                y - sprite.regionHeight.meters / 2 + height * 0.625f
            } else {
                y + spriteData.offsetY
            }

            if (spriteData.isHand && looksRight() && !spriteData.isBackground) {
                drawItem(spriteBatch, sprite.height, x, y, animationValue)
            }

            spriteBatch.drawSprite(sprite, spriteX, spriteY, animationValue)

            if (spriteData.isHand && spriteData.isBackground && looksLeft()) {
                drawItem(spriteBatch, sprite.height, x, y, -animationValue)
            }
        }
    }

    override fun dispose() {
        super.dispose()
        sight = null
    }

    fun respawn(spawnPoint: Vector2, mobPhysicsFactory: MobPhysicsFactory) {
        this.spawnPoint = spawnPoint
        isDead = false
        heal(maxHealth)
        breath = params.maxBreath
        spawn(spawnPoint.x, spawnPoint.y, mobPhysicsFactory)
        cursorX = position.x
        cursorY = position.y
    }

    fun initSight(mobWorldAdapter: MobWorldAdapter) {
        sight = PointLight(
            mobWorldAdapter.getRayHandler(),
            128,
            Color().apply { a = 0.5f },
            4f,
            position.x,
            position.y,
        ).apply {
            attachToBody(body)
            ignoreAttachedBody = true
            setContactFilter(
                Filter().apply {
                    categoryBits = PhysicsConstants.CATEGORY_OPAQUE
                },
            )
            isXray = true
        }
    }

    fun updateSight() {
        sight?.let { sight ->
            sight.setPosition(position.x, position.y)
            sight.direction = 0f
        }
    }

    fun startHitting(withDamage: Boolean = true) {
        if (isHitting) {
            return
        }

        isHitting = true
        isHittingWithDamage = withDamage
        hitAnim = HIT_ANIMATION_RANGE.endInclusive
        hitAnimDelta = DEFAULT_ANIMATION_SPEED
    }

    fun stopHitting() {
        blockDamage = 0f
        isHitting = false
    }

    fun hitMob(mob: Mob) {
        startHitting(false)
        val activeTool = activeItem.item as? Item.Tool
        if (activeTool != null) {
            decreaseCurrentItemCount()
        }
        val damage = 1 * (activeTool?.mobDamageMultiplier ?: 1f)
        mob.damage(damage.toInt())
        stopHitting()
    }

    fun decreaseCurrentItemCount(amount: Int = 1) {
        if (gameMode.isCreative()) {
            return
        }

        activeItem.subtract(amount)

        if (activeItem.amount <= 0) {
            setCurrentInventorySlotItem(getFallbackItem())
        }
    }

    fun setCurrentInventorySlotItem(item: Item) {
        inventory.items[activeSlot] = item.toInventoryItem()
    }

    private fun drawItem(spriteBatch: SpriteBatch, handLength: Float, x: Float, y: Float, handAnim: Float) {
        val item = activeItem.item.takeIf { !it.isNone() } ?: return
        val itemSprite = item.sprite
        val isSmallSprite = !item.isTool() || item.isShears()
        val originalWidth = itemSprite.width
        val originalHeight = itemSprite.height

        if (isSmallSprite) {
            itemSprite.setSize(SMALL_ITEM_SIZE, SMALL_ITEM_SIZE)
        }

        val spriteOrigin = item.params.inHandSpriteOrigin
        val handMultiplier = -direction.basis
        val xOffset = (-1 + direction.index) * itemSprite.width + SMALL_ITEM_SIZE / 2 +
            handMultiplier * itemSprite.width * spriteOrigin.x
        val yOffset = if (!isSmallSprite) -itemSprite.height / 2 else 0f

        val rotate = handAnim + HAND_ITEM_ANGLE_DEG

        if (item.isTool()) {
            itemSprite.rotate90(looksLeft())
        }

        val itemX = x + handLength * MathUtils.sin(handMultiplier * handAnim * MathUtils.degRad) + xOffset
        val itemY = y + handLength * MathUtils.cos(handMultiplier * handAnim * MathUtils.degRad) + yOffset

        if (looksLeft()) {
            itemSprite.setFlip(!item.isTool(), itemSprite.isFlipY)
            itemSprite.applyOrigin(spriteOrigin.getFlipped(flipX = true, flipY = false))
        } else {
            itemSprite.setFlip(item.isTool(), itemSprite.isFlipY)
            itemSprite.applyOrigin(spriteOrigin)
        }

        itemSprite.rotation = -handMultiplier * rotate
        itemSprite.setPosition(itemX, itemY)
        itemSprite.draw(spriteBatch)

        // dont forget to reset
        itemSprite.setFlip(false, itemSprite.isFlipY)
        itemSprite.rotation = 0f
        itemSprite.setOriginCenter()
        itemSprite.setSize(originalWidth, originalHeight)
        if (item.isTool()) {
            itemSprite.rotate90(looksRight())
        }
    }

    private fun getRightHandAnim(delta: Float): Float {
        hitAnim -= hitAnimDelta * delta

        if (hitAnim !in HIT_ANIMATION_RANGE) {
            if (isHitting) {
                hitAnim = MathUtils.clamp(hitAnim, HIT_ANIMATION_RANGE.start, HIT_ANIMATION_RANGE.endInclusive)
                hitAnimDelta = -hitAnimDelta
            } else {
                hitAnimDelta = DEFAULT_ANIMATION_SPEED
            }
        }

        if (!isHitting) {
            if (hitAnim < hitAnimDelta * delta) {
                hitAnim = 0f
                hitAnimDelta = 0f
                return -anim
            }
        }

        return hitAnim
    }

    enum class ControlMode {
        WALK,
        CURSOR,
    }

    companion object {
        const val HOTBAR_SIZE = 9
        const val INVENTORY_SIZE = 36

        private val HIT_ANIMATION_RANGE = 30f..90f

        private const val SMALL_ITEM_SIZE = .5f
        private const val HAND_ITEM_ANGLE_DEG = 30f
    }
}
