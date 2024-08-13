include("android")
include("desktop")
include("core")

/**
 * Global modules
 */
include("core:common")

// data modules
include("core:data:assets")
include("core:data:items")
include("core:data:save")

// domain modules
include("core:domain:assets")
include("core:domain:items")
include("core:domain:save")

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
