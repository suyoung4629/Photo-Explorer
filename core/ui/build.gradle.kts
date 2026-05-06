plugins {
    id("photoexplorer.android.library")
    id("photoexplorer.android.compose")
    id("photoexplorer.android.hilt")
}

android {
    namespace = "com.unsplash.photoexplorer.core.ui"
}

dependencies {
    implementation(project(":core:domain"))

    // Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Paging
    implementation(libs.androidx.paging.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
}
