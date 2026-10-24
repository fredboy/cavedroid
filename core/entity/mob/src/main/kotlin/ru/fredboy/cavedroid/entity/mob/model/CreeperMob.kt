package ru.fredboy.cavedroid.entity.mob.model

import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import ru.fredboy.cavedroid.domain.items.model.mob.MobParams
import ru.fredboy.cavedroid.entity.mob.impl.CreeperFuse

class CreeperMob(params: MobParams) : WalkingMob(params) {
    val fuse = CreeperFuse()
    var daylightSec = 0f

    override fun draw(spriteBatch: SpriteBatch, x: Float, y: Float, delta: Float) {
        if (!fuse.isPrimed) {
            super.draw(spriteBatch, x, y, delta)
            return
        }
        val transform = spriteBatch.transformMatrix.cpy()
        val progress = fuse.elapsedSec / CreeperFuse.DURATION_SEC
        spriteBatch.transformMatrix = transform.cpy()
            .translate(x + width / 2f, y + height, 0f)
            .scale(1f + 0.15f * progress, 1f + 0.1f * progress, 1f)
            .translate(-x - width / 2f, -y - height, 0f)
        super.draw(spriteBatch, x, y, delta)
        if ((fuse.elapsedSec * 10f).toInt() % 2 == 0) {
            val src = spriteBatch.blendSrcFunc
            val dst = spriteBatch.blendDstFunc
            val srcAlpha = spriteBatch.blendSrcFuncAlpha
            val dstAlpha = spriteBatch.blendDstFuncAlpha
            spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE)
            super.draw(spriteBatch, x, y, 0f)
            spriteBatch.setBlendFunctionSeparate(src, dst, srcAlpha, dstAlpha)
        }
        spriteBatch.transformMatrix = transform
    }
}
