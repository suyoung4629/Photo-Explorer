package com.unsplash.photoexplorer.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.unsplash.photoexplorer.data.local.dao.FavoritePhotoDao
import com.unsplash.photoexplorer.data.local.entity.FavoritePhotoEntity

@Database(
    entities = [FavoritePhotoEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class PhotoExplorerDatabase : RoomDatabase() {
    abstract fun favoritePhotoDao(): FavoritePhotoDao
}
