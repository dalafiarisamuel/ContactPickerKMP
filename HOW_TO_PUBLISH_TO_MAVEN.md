### Publishing to Maven Central with `com.vanniktech.maven.publish` Plugin

This guide explains how to set up and publish your Kotlin Multiplatform library to Maven Central using
`com.vanniktech.maven.publish` Gradle plugin.

### Prerequisites

Before you start, make sure you have the following:

- A Maven Central account.
- Your project is set up to use Gradle.
- A GPG key for signing artifacts.

### Step 1: Configure `gradle.properties`

Add the following properties to your `gradle.properties` file to set up signing and Maven credentials:

```properties
signing.keyId=<GPG Key ID>
signing.password=<GPG Key Password>
signing.secretKeyRingFile=<path to secret key ring file>
mavenCentralUsername=<Sonatype Username>
mavenCentralPassword=<Sonatype Password>
```

Note: Ensure your gradle.properties file is not included in version control or consider using environment variables to
keep your credentials secure.

### Step 2: Add the Maven Publish Plugin to build.gradle.kts

Add `com.vanniktech.maven.publish` plugin to your `build.gradle.kts:`

```kotlin
plugins {
    id("com.vanniktech.maven.publish") version "0.30.0"
}
```

### Step 3: Configure Your Library's build.gradle.kts

Ensure that your build.gradle.kts has the necessary metadata for your project:

```kotlin
mavenPublishing {
    // Define coordinates for the published artifact
    coordinates(
        groupId = "com.group.package.id", // Your maven central namespace e.g io.github.dalafiarisamuel
        artifactId = "library", // library
        version = "0.0.1" // version
    )

    // Configure POM metadata for the published artifact
    pom {
        name.set("Your library name")
        description.set("A brief description of your library.")
        inceptionYear.set("2024") //or year of publication
        url.set("https://github.com/your/repository")

        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
            }
        }

        // Specify developers information
        developers {
            developer {
                id.set("yourId e.g devTamuno")
                name.set("Your Name")
                email.set("Your Email")
            }
        }

        // Specify SCM information
        scm {
            url.set("https://github.com/your/repository")
        }
    }

    // Configure publishing to Maven Central
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    // Enable GPG signing for all publications
    signAllPublications()
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
    }
}

```

### Step 4: Generate and Upload GPG Key

To sign your artifacts, you need a GPG key:

1. Run the command: ```gpg --full-generate-key```
    - When the terminal asks "Please select what kind of key you want:" Enter 1 for RSA and RSA
    - When the terminal asks "RSA keys may be between 1024 and 4096 bits long." Enter 4096
    - When the terminal asks "Please specify how long the key should be valid." enter 0 for `key does not expire` and
      respond `y` for "Key does not expire at all?".
    - Continue the process by inputting your real name, email address and comment (this can be blank)
    - Enter a password you'll remember to sign your key

    <br>

2. Export your GPG key and Setup Credentials:
    - List generate keys by running this command: ```gpg --list-secret-keys```
    - Copy the long form of the newly generated gpg key e.g `574DEB97803CD28D5F07A4054DCE5983654E7199`
    - Export your key to base64 PGP private key block
      `--armor --export-secret-keys 574DEB97803CD28D5F07A4054DCE5983654E7199 | pbcopy` N/B: this will be copied to your
      clipboard. you can paste it into another file before you contiue to the next step.
    - Save exported key to a `.gpg` file by running this command:
      ```echo "paste the exported base64 key block" | gpg --dearmor > ~/secring.gpg```. This should generate a file
      secring.gpg in your root directory.
    - Upload your key to a public repository. This step is compulsory, as it's the only way Maven Central can validate
      the files you're uploading: ```--keyserver keys.openpgp.org --send-key 574DEB97803CD28D5F07A4054DCE5983654E7199```
    - Check the email address attached to that key, you should get an email asking you to verify the email address.
      follow the prompt and verify the email address accordingly.

    <br>

3. Setup Token-Based Authentication
   * Login to your Maven Central account
   * Click on your Profile --> `View Account`
   * Click on `Generate User Token`
   * If you can't remember your last generated token, generate another.
   * Copy username and password, as they'll be used in the next step.

    <br>

4. Set up your key in `gradle.properties` as shown earlier.
    ```properties
    signing.keyId=//last 8 characters of your key e.g  654E7199
    signing.password=// password from Step 2
    signing.secretKeyRingFile=// path to your secring.gpg file
    mavenCentralUsername=//sonar username copied from Step 3
    mavenCentralPassword=//sonar password copied from Step 3
   ```


### Step 5: Push to Maven Central
- Make sure your app builds and runs 👀
- Run this gradle command to push your package to maven central:
      ```./gradlew publishToMavenCentral --no-configuration-cache```.
- After the gradle task is completed, goto your `Maven Central Profile` --> `View Deployments`.
- If you carried out these steps correctly, you should see your package being processed and verified by Maven. You have
   two options, to `Drop` or `Publish`. If it fails to validate your build, your gpg public key is probably not
      available in the public repository. Kindly retry `Step 4`.
- After publishing is successful, verify your library is now visible on Maven Central by searching for the namespace.


<br>

###### Happy Coding ❤️🚀
