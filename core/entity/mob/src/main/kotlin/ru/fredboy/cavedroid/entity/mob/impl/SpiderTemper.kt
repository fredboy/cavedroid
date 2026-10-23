package ru.fredboy.cavedroid.entity.mob.impl

class SpiderTemper {

    var angerRemainingSec = 0f

    fun provoke() {
        angerRemainingSec = ANGER_DURATION_SEC
    }

    fun update(isDayTime: Boolean, mobY: Float, seaLevel: Int, isCreative: Boolean, delta: Float): Boolean {
        angerRemainingSec = (angerRemainingSec - delta).coerceAtLeast(0f)
        // World Y increases downward; use the mob's center to determine its depth.
        return !isCreative && (!isDayTime || mobY > seaLevel || angerRemainingSec > 0f)
    }

    companion object {
        private const val ANGER_DURATION_SEC = 30f
    }
}
