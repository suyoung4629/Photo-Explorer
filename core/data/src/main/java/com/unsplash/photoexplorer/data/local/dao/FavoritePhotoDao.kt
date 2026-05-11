package com.unsplash.photoexplorer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.unsplash.photoexplorer.data.local.entity.FavoritePhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritePhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: FavoritePhotoEntity)

    @Query("SELECT * FROM favorite_photos WHERE id = :id")
    suspend fun getById(id: String): FavoritePhotoEntity?

    @Query("DELETE FROM favorite_photos WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT id FROM favorite_photos")
    fun observeFavoriteIds(): Flow<List<String>>

    @Query("SELECT * FROM favorite_photos ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<FavoritePhotoEntity>>
}
