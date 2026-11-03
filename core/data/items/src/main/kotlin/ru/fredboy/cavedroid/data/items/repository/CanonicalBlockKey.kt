package ru.fredboy.cavedroid.data.items.repository

/** Stable block identity shared by save lookup and terrain compatibility verification. */
fun canonicalBlockKey(key: String): String = LegacyItemKeys.blocks[key] ?: key
