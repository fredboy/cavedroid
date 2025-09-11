include("android")
include("desktop")
include("ios")

/**
 * Global modules
 */
include("core:common")

/**
 * Data modules
 */
include("core:data:assets")
include("core:data:configuration")
include("core:data:items")
include("core:data:save")

/**
 * First level domain models
 */
include("core:domain:assets")
include("core:domain:configuration")
include("core:domain:items")
include("core:domain:world")
include("core:domain:save")

/**
 * Second level domain models
 */
include("core:entity:container")
include("core:entity:drop")
include("core:entity:mob")
include("core:entity:projectile")

/**
 * Game scope modules
 */
include("core:game")

// controller modules
include("core:game:controller:drop")
include("core:game:controller:container")
include("core:game:controller:mob")
include("core:game:controller:projectile")

// ui windows module
include("core:game:window")

// world module
include("core:game:world")

/**
 * Gameplay modules, physics, rendering, controls, etc.
 */
include("core:gameplay:controls")
include("core:gameplay:physics")
include("core:gameplay:rendering")

/**
 * Gdx module: initialization, menu, screens...
 */
include("core:gdx")
