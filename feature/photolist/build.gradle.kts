plugins {
    id("photoexplorer.android.feature")
}

android {
    namespace = "com.unsplash.photoexplorer.feature.photolist"
}

dependencies {
    // Paging
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)
}
