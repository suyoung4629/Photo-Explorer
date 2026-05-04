package com.unsplash.photoexplorer.domain.usecase

import com.unsplash.photoexplorer.domain.model.PhotoDetail
import com.unsplash.photoexplorer.domain.repository.PhotoRepository
import javax.inject.Inject

class GetPhotoDetailUseCase @Inject constructor(
    private val repository: PhotoRepository,
) {
    suspend operator fun invoke(id: String): PhotoDetail = repository.getPhotoDetail(id)
}
