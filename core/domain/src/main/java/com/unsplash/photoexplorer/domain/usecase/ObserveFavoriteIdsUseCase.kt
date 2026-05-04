package com.unsplash.photoexplorer.domain.usecase

import com.unsplash.photoexplorer.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveFavoriteIdsUseCase @Inject constructor(
    private val repository: PhotoRepository,
) {
    operator fun invoke(): Flow<Set<String>> = repository.observeFavoriteIds()
}
