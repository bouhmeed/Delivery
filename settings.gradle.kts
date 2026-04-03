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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // TomTom repository for SDK - TEMPORARILY DISABLED due to missing Maven credentials
        // Uncomment when TomTom SDK credentials are available
        /*
        maven {
            url = uri("https://repositories.tomtom.com/artifactory/maven")
        }
        */
    }
}

rootProject.name = "Delivery"
include(":app")
