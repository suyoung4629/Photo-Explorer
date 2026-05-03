package com.unsplash.photoexplorer.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.unsplash.photoexplorer.data.local.dao.FavoritePhotoDao
import com.unsplash.photoexplorer.data.local.store.PhotoFileStore
import com.unsplash.photoexplorer.data.mapper.toFavoriteEntity
import com.unsplash.photoexplorer.data.mapper.toPhoto
import com.unsplash.photoexplorer.data.mapper.toPhotoDetail
import com.unsplash.photoexplorer.data.paging.PhotoPagingSource
import com.unsplash.photoexplorer.data.remote.api.UnsplashApi
import com.unsplash.photoexplorer.domain.model.Photo
import com.unsplash.photoexplorer.domain.model.PhotoDetail
import com.unsplash.photoexplorer.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoRepositoryImpl @Inject constructor(
    private val api: UnsplashApi,
    private val favoriteDao: FavoritePhotoDao,
    private val photoFileStore: PhotoFileStore,
) : PhotoRepository {

    override fun getPhotoList(): Flow<PagingData<Photo>> {
        val pagerFlow = Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = PAGE_SIZE,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = { PhotoPagingSource(api) },
        ).flow

        return combine(pagerFlow, favoriteDao.observeFavoriteIds()) { pagingData, favoriteIds ->
            val favoriteSet = favoriteIds.toSet()
            pagingData.map { dto -> dto.toPhoto(isFavorite = dto.id in favoriteSet) }
        }
    }

    override suspend fun getPhotoDetail(id: String): PhotoDetail {
        val dto = api.getPhotoDetail(id)
        val isFavorite = favoriteDao.isFavorite(id)
        return dto.toPhotoDetail(isFavorite = isFavorite)
    }

    override suspend fun toggleFavorite(photo: Photo) {
        val existing = favoriteDao.getById(photo.id)
        if (existing != null) {
            photoFileStore.delete(existing.localFilePath)
            favoriteDao.deleteById(photo.id)
        } else {
            val downloadUrl = api.trackDownload(photo.id).url
            val localPath = photoFileStore.download(downloadUrl, photo.id)
            favoriteDao.insert(
                photo.toFavoriteEntity(
                    localFilePath = localPath,
                    addedAt = System.currentTimeMillis(),
                )
            )
        }
    }

    override fun observeFavoritePhotos(): Flow<List<Photo>> =
        favoriteDao.observeAll().map { entities -> entities.map { it.toPhoto() } }

    override suspend fun trackDownload(photoId: String): String =
        api.trackDownload(photoId).url

    private companion object {
        const val PAGE_SIZE = 20
    }
}
