plugins {
    id("com.gtnewhorizons.gtnhconvention")
}

// The file name carries the last release's tag alone (modern-poses-1.3.1.jar), not the commits since it
val lastTag = providers.exec { commandLine("git", "describe", "--tags", "--abbrev=0") }.standardOutput.asText.get().trim()

tasks.withType<org.gradle.jvm.tasks.Jar>().configureEach {
    archiveVersion.set(lastTag)
}
