plugins { id("com.android.application") }
android {
    namespace = "com.saerok.pleos"
    compileSdk { version = release(36) { minorApiLevel = 1 } }
    defaultConfig {
        applicationId = "com.saerok.pleos.bonsai"
        minSdk = 26
        targetSdk = 34
        versionCode = 9
        versionName = "0.6.3"
    }
    buildFeatures { buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies { implementation("androidx.webkit:webkit:1.16.0") }



