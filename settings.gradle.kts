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
        // TomTom Maven repository with credentials
        maven {
            url = uri("https://repositories.tomtom.com/artifactory/maven")
            credentials {
                username = providers.gradleProperty("repositoriesTomtomComUsername").get()
                password = providers.gradleProperty("repositoriesTomtomComPassword").get()
            }
        }
    }
}

rootProject.name = "Delivery"
include(":app")
