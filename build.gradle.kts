plugins {
    alias(libs.plugins.indra) apply false
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
        plugin("net.kyori.indra.publishing")
    }
}