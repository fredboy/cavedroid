package ru.fredboy.cavedroid.common.performance

/** Inclusive elapsed wall durations, never CPU execution time or GPU time. */
enum class PerformanceMetric(val perFrame: Boolean = true) {
    FRAME_INTERVAL(false),
    FRAME_CALLBACK,
    UPDATE_TOTAL,
    WORLD_UPDATE,
    ENTITIES_UPDATE,
    OTHER_UPDATE,
    STREAMING,
    COLLISION_PREPARE,
    PHYSICS,
    BOX2D,
    LIGHTING_UPDATE,
    RENDER_TOTAL,
    RENDER_WORLD,
    RENDER_BLOCKS,
    RENDER_PLAYER,
    RENDER_MOBS,
    RENDER_DROPS,
    RENDER_PROJECTILES,
    RENDER_HUD,
    RENDER_LIGHTING,
    RENDER_OTHER,
    TASK_BLOCKS(false),
    TASK_FLUIDS(false),
    TASK_GROW(false),
    TASK_FIRE(false),
    TASK_SPAWN(false),
    TASK_DAMAGE(false),
    WORLD_GENERATE(false),
    CHUNK_GENERATE(false),
    CHUNK_REQUEST(false),
    CHUNK_READ(false),
    CHUNK_WRITE(false),
    CHUNK_PERSIST_TASK(false),
    COLLISION_BUILD(false),
    COLLISION_REQUEST(false),
    COLLISION_APPLY(false),
    COLLISION_INSTALL,
    SAVE_SERIALIZE(false),
    SAVE_WRITE(false),
    SAVE_SCREENSHOT(false),
}

enum class PerformanceOperation {
    ANDROID_STARTUP,
    APP_INIT,
    ASSETS_LOAD,
    MENU_READY,
    WORLD_CREATE,
    WORLD_LOAD,
    WORLD_SAVE,
    WORLD_RELEASE,
    WORLD_UNLOAD,
    STARTUP_SPAWN,
}

enum class PerformanceResult { SUCCESS, ERROR, CANCELLED }
enum class PerformanceMode {
    OFF,
    LOCAL,
    FIREBASE,
    ;

    fun next(localAvailable: Boolean, firebaseAvailable: Boolean): PerformanceMode = when (this) {
        OFF -> when {
            localAvailable -> LOCAL
            firebaseAvailable -> FIREBASE
            else -> OFF
        }
        LOCAL -> if (firebaseAvailable) FIREBASE else OFF
        FIREBASE -> OFF
    }

    companion object {
        fun restore(stored: String?, firebaseAvailable: Boolean, localAvailable: Boolean): PerformanceMode {
            if (stored == null) return if (firebaseAvailable) FIREBASE else OFF
            val mode = entries.firstOrNull { it.name == stored } ?: OFF
            return when {
                mode == FIREBASE && !firebaseAvailable -> OFF
                mode == LOCAL && !localAvailable -> OFF
                else -> mode
            }
        }
    }
}
enum class PerformanceTrigger { USER, INITIAL }
enum class PerformanceStartKind { COLD_PROCESS, ACTIVITY_RECREATION, FOREGROUND_RESUME }
enum class PerformanceGauge {
    HEAP_USED,
    HEAP_COMMITTED,
    HEAP_MAX,
    HEAP_HEADROOM,
    NATIVE_HEAP,
    PROCESS_PSS,
    TERRAIN_CHUNKS,
    RENDER_CHUNKS,
    CACHE_UPDATES,
    ACTIVE_ENTITIES,
    DRAWN_ENTITIES,
    PHYSICS_STEPS,
}

fun interface PerformanceClock {
    fun nanoTime(): Long
}
