pluginManagement {
    repositories {
        maven {
            name = "groupezReleases"
            url = uri("https://repo.groupez.dev/releases")
        }
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "zEssentials"

include("API")
include("DiscordBot")
include("Hooks:Vault")
include("Hooks:SuperiorSkyBlock2")
include("Hooks:Redis")
include("Hooks:ProtocolLib")
include("Hooks:BlockTracker")
include("Hooks:AxVault")
include("Hooks:NuVotifier")
include("Hooks:NChat")
include("Hooks:WorldGuard")
include("Hooks:MythicMobs")

include("NMS:V26_1_2")
include("NMS:V26_2")

