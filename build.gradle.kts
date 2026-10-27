plugins {
    id("com.android.application") version "8.13.2" apply false
    id("com.android.library") version "8.13.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
    id("com.vanniktech.maven.publish") version "0.35.0" apply false
}
val versionName = providers.gradleProperty("VERSION_NAME").get()

val checkReadmeVersion by tasks.registering {
    val readme = layout.projectDirectory.file("README.md").asFile
    val expected = "\"com.staxpayments:cardpresent:$versionName\""
    doLast {
        check(readme.readText().contains(expected)) { "README.md must show $expected before an upload." }
    }
}

val manualReleaseOnly = "This SDK never releases automatically. Upload with publishToMavenCentral, then click Publish on the Central Portal."
gradle.taskGraph.whenReady {
    check(allTasks.none { it.name == "enableAutomaticMavenCentralPublishing" }) { manualReleaseOnly }
}

subprojects {
    plugins.withId("com.vanniktech.maven.publish") {
        afterEvaluate {
            val publishedVersion = findProperty("VERSION_NAME").toString()
            check(publishedVersion.endsWith("-SNAPSHOT") || providers.gradleProperty("signAllPublications").orElse(providers.gradleProperty("RELEASE_SIGNING_ENABLED")).orNull.toBoolean()) {
                "Release version $publishedVersion of $path must be signed. For a local build, use -PVERSION_NAME=$publishedVersion-SNAPSHOT."
            }
        }
        tasks.named { it == "prepareMavenCentralPublishing" }.configureEach { dependsOn(checkReadmeVersion) }
    }
}
