package com.unsplash.photoexplorer.di

import android.content.Context
import androidx.room.Room
import com.unsplash.photoexplorer.data.local.dao.FavoritePhotoDao
import com.unsplash.photoexplorer.data.local.database.PhotoExplorerDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PhotoExplorerDatabase =
        Room.databaseBuilder(
            context,
            PhotoExplorerDatabase::class.java,
            "photo_explorer.db",
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideFavoritePhotoDao(db: PhotoExplorerDatabase): FavoritePhotoDao =
        db.favoritePhotoDao()
}
