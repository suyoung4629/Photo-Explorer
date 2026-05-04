package com.unsplash.photoexplorer.domain.usecase

import androidx.paging.PagingData
import com.unsplash.photoexplorer.domain.model.Photo
import com.unsplash.photoexplorer.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPhotoListUseCase @Inject constructor(
    private val repository: PhotoRepository,
) {
    operator fun invoke(): Flow<PagingData<Photo>> = repository.getPhotoList()
}
