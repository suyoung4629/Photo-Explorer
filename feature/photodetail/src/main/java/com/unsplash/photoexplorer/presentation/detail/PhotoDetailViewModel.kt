package com.unsplash.photoexplorer.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unsplash.photoexplorer.domain.model.PhotoDetail
import com.unsplash.photoexplorer.domain.usecase.GetPhotoDetailUseCase
import com.unsplash.photoexplorer.domain.usecase.ObserveFavoriteIdsUseCase
import com.unsplash.photoexplorer.presentation.common.FavoriteToggleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

sealed interface PhotoDetailUiState {
    data object Loading : PhotoDetailUiState
    data class Success(
        val detail: PhotoDetail,
        val isToggling: Boolean = false,
    ) : PhotoDetailUiState
    data class Error(val message: String) : PhotoDetailUiState
}

sealed interface PhotoDetailSideEffect {
    data class ShowMessage(val message: String) : PhotoDetailSideEffect
}

@HiltViewModel
class PhotoDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPhotoDetailUseCase: GetPhotoDetailUseCase,
    private val favoriteToggleManager: FavoriteToggleManager,
    private val observeFavoriteIdsUseCase: ObserveFavoriteIdsUseCase,
) : ContainerHost<PhotoDetailUiState, PhotoDetailSideEffect>, ViewModel() {

    private val photoId: String = checkNotNull(savedStateHandle.get<String>("photoId"))

    override val container = container<PhotoDetailUiState, PhotoDetailSideEffect>(
        PhotoDetailUiState.Loading,
    ) {
        load()
        observeFavorites()
        observeMessages()
    }

    private fun load() = intent {
        reduce { PhotoDetailUiState.Loading }
        try {
            val detail = getPhotoDetailUseCase(photoId)
            reduce { PhotoDetailUiState.Success(detail = detail) }
        } catch (e: Exception) {
            reduce {
                PhotoDetailUiState.Error(
                    message = e.message ?: "사진 정보를 불러오지 못했습니다",
                )
            }
        }
    }

    private fun observeFavorites() = intent {
        combine(
            observeFavoriteIdsUseCase(),
            favoriteToggleManager.togglingPhotoIds,
        ) { favoriteIds, togglingIds ->
            Pair(favoriteIds, togglingIds)
        }.collect { (favoriteIds, togglingIds) ->
            reduce {
                when (val s = state) {
                    is PhotoDetailUiState.Success -> s.copy(
                        detail = s.detail.copy(
                            photo = s.detail.photo.copy(
                                isFavorite = s.detail.photo.id in favoriteIds,
                            ),
                        ),
                        isToggling = s.detail.photo.id in togglingIds,
                    )
                    else -> state
                }
            }
        }
    }

    private fun observeMessages() = intent {
        favoriteToggleManager.messages.collect { message ->
            postSideEffect(PhotoDetailSideEffect.ShowMessage(message))
        }
    }

    fun retry() = load()

    fun toggleFavorite() = intent {
        val current = state as? PhotoDetailUiState.Success ?: return@intent
        favoriteToggleManager.toggle(current.detail.photo, viewModelScope)
    }
}
