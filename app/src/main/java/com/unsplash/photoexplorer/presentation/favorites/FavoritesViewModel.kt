package com.unsplash.photoexplorer.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unsplash.photoexplorer.domain.model.Photo
import com.unsplash.photoexplorer.domain.usecase.GetFavoritePhotosUseCase
import com.unsplash.photoexplorer.presentation.common.FavoriteToggleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class FavoritesUiState(
    val photos: List<Photo> = emptyList(),
    val togglingPhotoIds: Set<String> = emptySet(),
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    getFavoritePhotosUseCase: GetFavoritePhotosUseCase,
    private val favoriteToggleManager: FavoriteToggleManager,
) : ViewModel() {

    val uiState: StateFlow<FavoritesUiState> = combine(
        getFavoritePhotosUseCase(),
        favoriteToggleManager.togglingPhotoIds,
    ) { photos, togglingIds ->
        FavoritesUiState(photos = photos, togglingPhotoIds = togglingIds)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FavoritesUiState(),
    )

    val userMessages: Flow<String> = favoriteToggleManager.messages

    fun toggleFavorite(photo: Photo) {
        favoriteToggleManager.toggle(photo, viewModelScope)
    }
}
