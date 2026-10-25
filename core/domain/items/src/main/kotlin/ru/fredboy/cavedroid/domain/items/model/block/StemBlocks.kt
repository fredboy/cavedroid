package ru.fredboy.cavedroid.domain.items.model.block

/** Shared stem states for planting, natural growth, and bone meal. */
object StemBlocks {

    const val GROW_ACTION_KEY = "grow_stem"
    const val UPDATE_ACTION_KEY = "update_stem"
    const val MAX_AGE = 7
    const val MIN_GROWTH_LIGHT = 9f / 15f
    val FARMLAND_KEYS = setOf("farmland", "farmland_moist")
    val FRUIT_SOIL_KEYS = FARMLAND_KEYS + setOf("dirt", "grass", "grass_snowed")

    data class State(val fruit: String, val age: Int) {
        val key: String get() = "${fruit}_stem_stage$age"
        fun advance(stages: Int): State = copy(age = (age + stages).coerceAtMost(MAX_AGE))
    }

    fun fromKey(key: String): State? {
        val fruit = key.substringBefore("_stem_stage")
        if (fruit != "pumpkin" && fruit != "melon") return null
        val age = key.removePrefix("${fruit}_stem_stage").toIntOrNull() ?: return null
        return State(fruit, age).takeIf { age in 0..MAX_AGE }
    }

    fun fromSeeds(key: String): State? = when (key) {
        "pumpkin_seeds" -> State("pumpkin", 0)
        "melon_seeds" -> State("melon", 0)
        else -> null
    }

    /** The Java crop-growth formula projected onto a horizontal row of soil. */
    fun growthBound(leftSoil: String, soil: String, rightSoil: String): Int {
        fun weight(key: String): Float = when (key) {
            "farmland_moist" -> 3f
            "farmland" -> 1f
            else -> 0f
        }
        val speed = 1f + weight(soil) + (weight(leftSoil) + weight(rightSoil)) / 4f
        return (25f / speed).toInt() + 1
    }
}
