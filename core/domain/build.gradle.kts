plugins {
    id("photoexplorer.android.library")
    id("photoexplorer.android.hilt")
}

android {
    namespace = "com.unsplash.photoexplorer.core.domain"
}

dependencies {
    // Paging (for PagingData in repository interface)
    implementation(libs.androidx.paging.runtime)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
}
