package com.unsplash.photoexplorer.domain.model

data class Photo(
    val id: String,
    val imageUrls: PhotoUrls,
    val username: String,
    val profileImageUrl: String?,
    val width: Int,
    val height: Int,
    val description: String?,
    val isFavorite: Boolean,
    val localPath: String?,
)

data class PhotoUrls(
    val raw: String,
    val full: String,
    val regular: String,
    val small: String,
    val thumb: String,
)
