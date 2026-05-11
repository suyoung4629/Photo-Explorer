package com.unsplash.photoexplorer.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unsplash.photoexplorer.domain.model.Photo
import com.unsplash.photoexplorer.domain.usecase.GetFavoritePhotosUseCase
import com.unsplash.photoexplorer.presentation.common.FavoriteToggleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

data class FavoritesUiState(
    val photos: List<Photo> = emptyList(),
    val togglingPhotoIds: Set<String> = emptySet(),
)

sealed interface FavoritesSideEffect {
    data class ShowMessage(val message: String) : FavoritesSideEffect
}

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val getFavoritePhotosUseCase: GetFavoritePhotosUseCase,
    private val favoriteToggleManager: FavoriteToggleManager,
) : ContainerHost<FavoritesUiState, FavoritesSideEffect>, ViewModel() {

    override val container = container<FavoritesUiState, FavoritesSideEffect>(FavoritesUiState()) {
        observeState()
        observeMessages()
    }

    private fun observeState() = intent {
        combine(
            getFavoritePhotosUseCase(),
            favoriteToggleManager.togglingPhotoIds,
        ) { photos, togglingIds ->
            FavoritesUiState(photos = photos, togglingPhotoIds = togglingIds)
        }.collect { newState ->
            reduce { newState }
        }
    }

    private fun observeMessages() = intent {
        favoriteToggleManager.messages.collect { message ->
            postSideEffect(FavoritesSideEffect.ShowMessage(message))
        }
    }

    fun toggleFavorite(photo: Photo) = intent {
        favoriteToggleManager.toggle(photo, viewModelScope)
    }
}
