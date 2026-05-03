package com.unsplash.photoexplorer.data.local.store

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoFileStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient,
) {
    private val favoritesDir: File by lazy {
        File(context.filesDir, FAVORITES_DIR).apply { mkdirs() }
    }

    suspend fun download(url: String, photoId: String): String = withContext(Dispatchers.IO) {
        val target = File(favoritesDir, "$photoId.jpg")
        val request = Request.Builder().url(url).build()
        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Download failed: HTTP ${response.code}")
            }
            val body = response.body ?: throw IOException("Download failed: empty body")
            target.outputStream().use { out -> body.byteStream().copyTo(out) }
        }
        target.absolutePath
    }

    suspend fun delete(path: String): Unit = withContext(Dispatchers.IO) {
        val file = File(path)
        if (file.exists() && !file.delete()) {
            throw IOException("Failed to delete file: $path")
        }
    }

    private companion object {
        const val FAVORITES_DIR = "favorites"
    }
}
