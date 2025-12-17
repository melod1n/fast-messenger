enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "fast-messenger"

include(":app")

include(":core:network")
include(":core:data")
include(":core:database")
include(":core:datastore")
include(":core:ui")
include(":core:common")
include(":core:domain")
include(":core:model")

include(":feature:messageshistory")
include(":feature:convos")
include(":feature:auth")
include(":feature:chatmaterials")
include(":feature:languagepicker")
include(":feature:photoviewer")
include(":feature:settings")
include(":feature:friends")
include(":feature:profile")
include(":feature:createchat")
include(":core:presentation")
