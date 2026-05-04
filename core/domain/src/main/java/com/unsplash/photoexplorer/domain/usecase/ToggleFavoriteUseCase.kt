package com.unsplash.photoexplorer.domain.usecase

import com.unsplash.photoexplorer.domain.model.Photo
import com.unsplash.photoexplorer.domain.repository.PhotoRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: PhotoRepository,
) {
    suspend operator fun invoke(photo: Photo) = repository.toggleFavorite(photo)
}
