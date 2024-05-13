package ru.deadsoftware.cavedroid.menu.objects

import ru.deadsoftware.cavedroid.MainConfig

class BooleanOptionButton(
    private val mainConfig: MainConfig,
    private val optionKey: String,
    private val defaultValue: Boolean,
    label: String,
    x: Int,
    y: Int,
    type: Int,
) : Button(
    label,
    x,
    y,
    type,
    {
        val current = (mainConfig.getPreference(optionKey)?.toBooleanStrictOrNull()) ?: defaultValue
        mainConfig.setPreference(optionKey, (!current).toString())
    }
) {

    override fun getLabel(): String {
        val value = (mainConfig.getPreference(optionKey)?.toBooleanStrictOrNull()) ?: defaultValue
        return super.getLabel().replace("%%value%%", value.toString())
    }

}