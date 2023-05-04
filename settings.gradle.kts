@file:Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()

        maven("https://repo.opencollab.dev/main")
        // SnakeYaml
        maven("https://oss.sonatype.org/content/groups/public")
    }
}

include(":core")
include(":ap")
