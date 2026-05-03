package com.unsplash.photoexplorer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_photos")
data class FavoritePhotoEntity(
    @PrimaryKey val id: String,
    val raw: String,
    val full: String,
    val regular: String,
    val small: String,
    val thumb: String,
    val username: String,
    val width: Int,
    val height: Int,
    val description: String?,
    val localFilePath: String,
    val addedAt: Long,
)
