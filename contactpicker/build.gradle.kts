@file:OptIn(ExperimentalAbiValidation::class)

import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.Sign
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.dokka.gradle.engine.parameters.KotlinPlatform
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.vanniktechMavenPublish)
    alias(libs.plugins.dokka)
    id("signing")
}

allprojects { version = libs.versions.contact.picker.version.get() }

dokka {
    moduleName.set("ContactPickerKMP")
    moduleVersion.set("${project.version}")

    dokkaPublications.configureEach {
        outputDirectory.set(rootDir.resolve("docs"))
    }

    dokkaSourceSets {
        configureEach {
            reportUndocumented.set(true)

            documentedVisibilities(
                VisibilityModifier.Public,
                VisibilityModifier.Internal,
                VisibilityModifier.Private,
            )

            sourceLink {
                localDirectory.set(projectDir.resolve("src"))
                remoteUrl.set(
                    uri("https://github.com/dalafiarisamuel/ContactPickerKMP/tree/master/contactpicker/src")
                )
                remoteLineSuffix.set("#L")
            }

            skipEmptyPackages.set(true)

            if (name == "commonMain") {
                displayName.set("Common")
                analysisPlatform.set(KotlinPlatform.Common)
            }

            if (name == "androidMain") {
                displayName.set("Android")
                analysisPlatform.set(KotlinPlatform.AndroidJVM)
                suppress.set(false)
            }

            if (name == "iosMain") {
                displayName.set("iOS")
                // analysisPlatform.set(KotlinPlatform.Native)
                suppress.set(false)
            }
        }
    }

    dokkaPublications.configureEach {
        pluginsConfiguration.html {
            footerMessage.set("Built with ❤️ for the KMP community by Samuel Dalafiari")
        }
    }
}

signing {
    useInMemoryPgpKeys(
        System.getenv("SIGNING_KEY"),
        System.getenv("SIGNING_KEY_PASSWORD")
    )
    sign(publishing.publications)

    // Temporary workaround, see https://github.com/gradle/gradle/issues/26091#issuecomment-1722947958
    tasks.withType<AbstractPublishToMaven>().configureEach {
        val signingTasks = tasks.withType<Sign>()
        mustRunAfter(signingTasks)
    }
}

mavenPublishing {
    // Define coordinates for the published artifact
    coordinates(
        groupId = "io.github.dalafiarisamuel",
        artifactId = "contactpicker",
        version = "${libs.versions.contact.picker.version.get()}"
    )

    // Configure POM metadata for the published artifact
    pom {
        name.set("KMP Library to natively select a contact")
        description.set("This library can be used by Android and iOS targets for the shared functionality of selecting a contact")
        inceptionYear.set("2024")
        url.set("https://github.com/dalafiarisamuel/ContactPickerKMP")

        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
            }
        }

        // Specify developers information
        developers {
            developer {
                id.set("devTamuno")
                name.set("Samuel Dalafiari")
                email.set("dalafiarisamuel@gmail.com")
            }
        }

        // Specify SCM information
        scm {
            url.set("https://github.com/dalafiarisamuel/ContactPickerKMP")
        }
    }

    // Configure publishing to Maven Central
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    // Enable GPG signing for all publications
    signAllPublications()
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
        publishLibraryVariants("release")
    }

    targets.configureEach {
        compilations.configureEach {
            compileTaskProvider.get().compilerOptions {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }


    abiValidation {
        enabled.set(true)
        filters {
            excluded {
                byNames.add("com.devtamuno.kmp.contactpicker.contract.ContactPickerStateImpl")
                byNames.add("com.devtamuno.kmp.contactpicker.contract.ContactPicker")
            }
        }
        klib {
            enabled = true
            keepUnsupportedTargets = true
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "contactpicker"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}

android {
    namespace = "com.devtamuno.kmp.contactpicker"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
