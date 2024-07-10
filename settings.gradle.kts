enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
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
include(":core:designsystem")
include(":core:ui")
include(":core:common")
include(":core:model")
include(":feature:messageshistory")
include(":feature:conversations")
include(":feature:auth")
include(":feature:chatmaterials")
include(":feature:languagepicker")
include(":feature:photoviewer")
include(":feature:settings")
include(":feature:auth:login")
include(":feature:auth:twofa")
include(":feature:auth:captcha")
include(":feature:auth:userbanned")
include(":feature:friends")
include(":feature:profile")
