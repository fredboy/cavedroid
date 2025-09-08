package ru.fredboy.cavedroid.domain.world.model

enum class ContactSensorType {
    MOB_ON_GROUND,
    MOB_SHOULD_JUMP_RIGHT,
    MOB_SHOULD_JUMP_LEFT,
    MOB_CLIFF_EDGE_LEFT,
    MOB_CLIFF_EDGE_RIGHT,
    DROP_ON_GROUND,
    DROP_ATTRACTOR,
    DROP_PICK_UP,
}
