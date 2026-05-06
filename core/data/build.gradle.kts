plugins {
    id("photoexplorer.android.library")
    id("photoexplorer.android.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.unsplash.photoexplorer.core.data"
}

dependencies {
    implementation(project(":core:domain"))

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    ksp(libs.androidx.room.compiler)

    // Retrofit + OkHttp
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.kotlinx.serialization.converter)

    // Paging
    implementation(libs.androidx.paging.runtime)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
}
