import java.util.Properties

        plugins {
            alias(libs.plugins.android.application)
            alias(libs.plugins.kotlin.android)
            alias(libs.plugins.kotlin.parcelize)
            id("com.google.gms.google-services")
            alias(libs.plugins.navigation.safeargs)
        }

android {
    namespace = "com.example.mueblar"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mueblar"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Configuración para leer claves de API desde secrets.properties
        buildConfigField("String", "MAPS_API_KEY", "\"${getProperty("MAPS_API_KEY")}\"")
        buildConfigField("String", "ARCORE_API_KEY", "\"${getProperty("ARCORE_API_KEY")}\"")
        manifestPlaceholders["MAPS_API_KEY"] = getProperty("MAPS_API_KEY")
        manifestPlaceholders["ARCORE_API_KEY"] = getProperty("ARCORE_API_KEY")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true // Habilita la generación de BuildConfig
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)


    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // Firebase BoM
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.fragment.ktx)


    // Glide
    implementation(libs.glide)

    // Play Services
    implementation(libs.play.services.analytics.impl)
    implementation(libs.play.services.maps)

    // ARCore
    implementation(libs.core)
    implementation(libs.play.services.location)
    implementation(libs.androidx.navigation.fragment)

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // SceneView
    implementation(libs.sceneview)
    implementation(libs.arsceneview)

    // OkHttp para descargar modelos
    implementation(libs.okhttp)

    // Firebase Storage
    implementation(libs.firebase.storage.ktx)

// Image/File picker
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx.v162)

    // Firebase Cloud Messaging
    implementation(libs.firebase.messaging.ktx)

    // Firebase Functions
    implementation (libs.firebase.functions.ktx)

    implementation (libs.json)


}

// Función para leer propiedades de secrets.properties
fun getProperty(propertyName: String): String {
    val propertiesFile = rootProject.file("secrets.properties")
    val properties = Properties()
    return if (propertiesFile.exists()) {
        properties.load(propertiesFile.inputStream())
        properties.getProperty(propertyName, "")
    } else {
        System.getenv(propertyName) ?: ""
    }
}