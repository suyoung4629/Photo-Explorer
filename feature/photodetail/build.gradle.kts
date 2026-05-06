plugins {
    id("photoexplorer.android.feature")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.unsplash.photoexplorer.feature.photodetail"
}

dependencies {
    // Navigation (for SavedStateHandle)
    implementation(libs.androidx.navigation.compose)

    // Serialization (for Route)
    implementation(libs.kotlinx.serialization.json)
}
