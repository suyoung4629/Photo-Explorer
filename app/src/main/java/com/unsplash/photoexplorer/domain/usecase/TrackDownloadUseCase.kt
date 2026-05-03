package com.unsplash.photoexplorer.domain.usecase

import com.unsplash.photoexplorer.domain.repository.PhotoRepository
import javax.inject.Inject

class TrackDownloadUseCase @Inject constructor(
    private val repository: PhotoRepository,
) {
    suspend operator fun invoke(photoId: String): String = repository.trackDownload(photoId)
}
