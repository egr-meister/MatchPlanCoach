import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
}

// ---------------------------------------------------------------------------
// Read local.properties for the (optional) football-data.org API token.
// The token is NEVER hardcoded in source. If it is missing/empty/placeholder
// the app falls back to demo data.
// ---------------------------------------------------------------------------
val localProperties = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) {
        FileInputStream(f).use { load(it) }
    }
}

fun readSecret(key: String, default: String): String {
    // Priority: environment variable (CI) -> local.properties -> default
    return System.getenv(key) ?: localProperties.getProperty(key) ?: default
}

val footballApiToken: String = readSecret("FOOTBALL_DATA_API_TOKEN", "your_api_token_here")
val footballApiBaseUrl: String = readSecret("FOOTBALL_API_BASE_URL", "https://api.football-data.org/v4")

// ---------------------------------------------------------------------------
// Optional release signing. Values are provided ONLY via environment variables
// (GitHub Secrets in CI) or a local keystore.properties file that is gitignored.
// If nothing is configured we fall back to debug signing so local `assemble`
// still works, but CI must supply the real PKCS12 keystore.
// ---------------------------------------------------------------------------
val keystoreProperties = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) FileInputStream(f).use { load(it) }
}

fun signingValue(envKey: String, propKey: String): String? =
    System.getenv(envKey) ?: keystoreProperties.getProperty(propKey)

val storeFilePath: String? = System.getenv("ANDROID_KEYSTORE_PATH")
    ?: keystoreProperties.getProperty("storeFile")
val storePassword: String? = signingValue("ANDROID_KEYSTORE_PASSWORD", "storePassword")
val keyAlias: String? = signingValue("ANDROID_KEY_ALIAS", "keyAlias")
val keyPassword: String? = signingValue("ANDROID_KEY_PASSWORD", "keyPassword")
val hasReleaseSigning = storeFilePath != null && storePassword != null &&
    keyAlias != null && keyPassword != null

android {
    namespace = "com.matchplan.coach"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.matchplan.coach"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        vectorDrawables { useSupportLibrary = true }

        // Exposed to the app via BuildConfig. The token is a build-time value
        // read from local.properties / env, never committed to source.
        buildConfigField("String", "FOOTBALL_DATA_API_TOKEN", "\"$footballApiToken\"")
        buildConfigField("String", "FOOTBALL_API_BASE_URL", "\"$footballApiBaseUrl\"")
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(storeFilePath!!)
                this.storePassword = storePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
                enableV1Signing = true
                enableV2Signing = true
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
        }
        release {
            // NOTE: Verify a NON-minified release build launches correctly first
            // (set both to false), then flip these to true and re-test.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = if (hasReleaseSigning) {
                signingConfigs.getByName("release")
            } else {
                // Fallback so a local `assembleRelease` still produces output.
                // CI ALWAYS provides the real keystore via GitHub Secrets.
                signingConfigs.getByName("debug")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        // Ensure 16 KB page-size alignment friendliness; we ship no native libs.
        jniLibs {
            useLegacyPackaging = false
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    debugImplementation(libs.androidx.ui.tooling)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.core.splashscreen)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    debugImplementation(libs.okhttp.logging)
}
