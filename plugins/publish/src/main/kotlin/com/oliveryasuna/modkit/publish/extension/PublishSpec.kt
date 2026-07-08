package com.oliveryasuna.modkit.publish.extension

import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested

public abstract class PublishSpec {

    public abstract val type: Property<String>

    public abstract val dryRun: Property<Boolean>

    @get:Nested
    public abstract val changelog: ChangelogSpec

    @get:Nested
    public abstract val modrinth: ModrinthSpec

    @get:Nested
    public abstract val curseforge: CurseForgeSpec

    @get:Nested
    public abstract val github: GitHubSpec

    @get:Nested
    public abstract val discord: DiscordSpec

    public fun changelog(action: Action<in ChangelogSpec>) {
        action.execute(changelog)
    }

    public fun modrinth(action: Action<in ModrinthSpec>) {
        action.execute(modrinth)
    }

    public fun curseforge(action: Action<in CurseForgeSpec>) {
        action.execute(curseforge)
    }

    public fun github(action: Action<in GitHubSpec>) {
        action.execute(github)
    }

    public fun discord(action: Action<in DiscordSpec>) {
        action.execute(discord)
    }

}
