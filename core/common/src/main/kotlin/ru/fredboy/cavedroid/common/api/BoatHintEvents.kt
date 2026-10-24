package ru.fredboy.cavedroid.common.api

interface BoatHintEvents {
    fun notifyPlacedAfloat(id: String)
    fun notifyMounted(id: String)
    fun notifyDetached(dismounted: Boolean)
    fun notifyRemoved(id: String)
}
