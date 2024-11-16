include("android")
include("desktop")
include("core")

/**
 * Global modules
 */
include("core:common")

/**
 * Data modules
 */
include("core:data:assets")
include("core:data:items")
include("core:data:save")

/**
 * First level domain models
 */
include("core:domain:assets")
include("core:domain:items")
include("core:domain:world")
include("core:domain:save")

/**
 * Second level domain models
 */
include("core:entity:container")
include("core:entity:drop")
include("core:entity:mob")

/**
 * Game scope modules
 */
include("core:game")

// controller modules
include("core:game:controller:drop")
include("core:game:controller:container")
include("core:game:controller:mob")

// world module
include("core:game:world")
