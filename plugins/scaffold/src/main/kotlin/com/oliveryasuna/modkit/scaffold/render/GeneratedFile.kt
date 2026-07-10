package com.oliveryasuna.modkit.scaffold.render

/**
 * One file the scaffold will write, as a relative [path] and its full
 * [content]. Renderers return an ordered list of these; the task writes them.
 */
internal data class GeneratedFile(val path: String, val content: String)
