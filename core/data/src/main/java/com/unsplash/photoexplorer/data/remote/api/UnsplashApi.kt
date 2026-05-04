package com.unsplash.photoexplorer.data.remote.api

import com.unsplash.photoexplorer.data.remote.dto.DownloadResponseDto
import com.unsplash.photoexplorer.data.remote.dto.PhotoDetailDto
import com.unsplash.photoexplorer.data.remote.dto.PhotoDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface UnsplashApi {

    @GET("photos")
    suspend fun getPhotos(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("order_by") orderBy: String,
    ): List<PhotoDto>

    @GET("photos/{id}")
    suspend fun getPhotoDetail(
        @Path("id") id: String,
    ): PhotoDetailDto

    @GET("photos/{id}/download")
    suspend fun trackDownload(
        @Path("id") id: String,
    ): DownloadResponseDto
}
