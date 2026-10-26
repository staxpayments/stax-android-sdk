plugins {
    id("com.android.application") version "8.13.2" apply false
    id("com.android.library") version "8.13.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
    id("com.vanniktech.maven.publish") version "0.35.0" apply false
}
val manualReleaseOnly = "This SDK never releases automatically. Upload with publishToMavenCentral, then click Publish on the Central Portal."
check(providers.gradleProperty("mavenCentralAutomaticPublishing").orNull?.toBoolean() != true) { manualReleaseOnly }
gradle.taskGraph.whenReady {
    check(allTasks.none { it.name == "enableAutomaticMavenCentralPublishing" }) { manualReleaseOnly }
}

subprojects {
    plugins.withId("com.vanniktech.maven.publish") {
        afterEvaluate {
            val publishedVersion = findProperty("VERSION_NAME").toString()
            check(publishedVersion.endsWith("-SNAPSHOT") || providers.gradleProperty("signAllPublications").orNull == "true") {
                "Release version $publishedVersion of $path must be signed. For a local build, use -PVERSION_NAME=$publishedVersion-SNAPSHOT."
            }
        }
    }
}
