dependencies {
    implementation(libs.snakeyaml)
    implementation(libs.geantyref)
    compileOnly(libs.checker.qual)

    testImplementation(libs.junit.jupiter)
}

tasks {
    jar {
        dependsOn(configurations.runtimeClasspath)

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })

        archiveBaseName.set("configutils-${project.name}")
        archiveClassifier.set("")
    }

    test {
        useJUnitPlatform()
    }
}