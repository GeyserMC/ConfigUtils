import net.kyori.indra.git.IndraGitExtension

plugins {
    alias(libs.plugins.indra)
    alias(libs.plugins.indra.git)
}

allprojects {
    group = "org.geysermc.configutils"
    description = "An utility to make config loading, updating etc. easier"
}

subprojects {
    apply {
        plugin("java-library")
        plugin("maven-publish")
        plugin("net.kyori.indra")
        plugin("net.kyori.indra.git")
        plugin("net.kyori.indra.publishing")
    }

    indra {
        github("GeyserMC", "ConfigUtils") {
            ci(true)
        }
        mitLicense()

        javaVersions {
            target(8)
        }

        configurePublications {
            if (shouldAddBranchName()) {
                version = versionWithBranchName()
            }
        }

        publishSnapshotsTo("geysermc", "https://repo.opencollab.dev/maven-snapshots")
        publishReleasesTo("geysermc", "https://repo.opencollab.dev/maven-releases")
    }
}

fun Project.branchName(): String =
        the<IndraGitExtension>().branchName() ?: System.getenv("BRANCH_NAME") ?: "local/dev"

fun Project.shouldAddBranchName(): Boolean =
        System.getenv("IGNORE_BRANCH")?.toBoolean() ?: (branchName() !in arrayOf("master", "local/dev"))

fun Project.versionWithBranchName(): String =
        branchName().replace(Regex("[^0-9A-Za-z-_]"), "-") + '-' + version