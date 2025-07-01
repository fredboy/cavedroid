package ru.fredboy.cavedroid.entity.container.model

data class ContainerCoordinates(
    val x: Int,
    val y: Int,
    val z: Int,
) {
    override fun toString(): String = "($x;$y;$z)"

    companion object {

        fun fromString(string: String): ContainerCoordinates {
            val xyz = string.trim('(', ')').split(';').map(Integer::valueOf)
            if (xyz.size != 3) {
                throw IllegalArgumentException("Invalid ContainerCoordinates format")
            }
            return ContainerCoordinates(
                x = xyz[0],
                y = xyz[1],
                z = xyz[2],
            )
        }
    }
}
