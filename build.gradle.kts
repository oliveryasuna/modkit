tasks.register("pluginModules") {
    group = "help"
    description = "Lists the plugin modules in this workspace."
    val names = subprojects.map { it.path }
    doLast {
        if(names.isEmpty()) {
            println("No plugin modules.")
        } else {
            println("Plugin modules:")
            names.forEach { println("  $it") }
        }
    }
}
