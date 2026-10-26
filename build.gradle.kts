plugins {
    id("com.android.application") version "8.13.2" apply false
    id("com.android.library") version "8.13.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
    id("com.vanniktech.maven.publish") version "0.35.0" apply false
}
val versionName = providers.gradleProperty("VERSION_NAME").get()
check(versionName.endsWith("-SNAPSHOT") || providers.gradleProperty("signAllPublications").orNull == "true") {
    "Release version $versionName must be signed. For a local build, use -PVERSION_NAME=$versionName-SNAPSHOT."
}
