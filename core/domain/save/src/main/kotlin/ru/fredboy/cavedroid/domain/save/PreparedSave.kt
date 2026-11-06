package ru.fredboy.cavedroid.domain.save

interface PreparedSave {
    fun write()
    fun finish() {}
}

fun PreparedSave.persist() {
    try {
        write()
    } finally {
        finish()
    }
}
