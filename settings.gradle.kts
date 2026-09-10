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

rootProject.name = "Jyoti"

include(":app")
include(":core:common")
include(":core:designsystem")
include(":core:network")
include(":core:voice")
include(":feature:assistant")
include(":feature:home")
include(":feature:settings")
