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
    versionCatalogs {
        create("configs") {
            from(files("gradle/configs.versions.toml"))
        }
    }
}

rootProject.name = "appname"
include(":app")
include(":core")
