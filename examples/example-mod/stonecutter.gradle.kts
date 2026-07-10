plugins {
    id("dev.kikugie.stonecutter")
}

// Expose a `fabric` / `neoforge` boolean to the source preprocessor, derived
// from the active node's name (e.g. "1.21.1-fabric" -> fabric).
stonecutter parameters {
    constants.match(current.project.substringAfterLast('-'), "fabric", "neoforge")
}

// The node checked out into src/ by default.
stonecutter active "1.20.6-neoforge"
