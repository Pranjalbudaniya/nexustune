pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

rootProject.name = "NexusTune"

include(":app")
include(":core:model")
include(":core:common")
include(":core:domain")
include(":core:data")
include(":core:database")
include(":core:datastore")
include(":core:extractor")
include(":core:player")
include(":core:designsystem")
include(":core:ui")
include(":core:testing")
include(":feature:home")
include(":feature:search")
include(":feature:library")
include(":feature:player")
include(":feature:settings")
include(":benchmark")
