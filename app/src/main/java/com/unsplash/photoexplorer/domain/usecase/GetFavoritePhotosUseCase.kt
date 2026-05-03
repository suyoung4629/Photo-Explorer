package com.unsplash.photoexplorer.domain.usecase

import com.unsplash.photoexplorer.domain.model.Photo
import com.unsplash.photoexplorer.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavoritePhotosUseCase @Inject constructor(
    private val repository: PhotoRepository,
) {
    operator fun invoke(): Flow<List<Photo>> = repository.observeFavoritePhotos()
}
