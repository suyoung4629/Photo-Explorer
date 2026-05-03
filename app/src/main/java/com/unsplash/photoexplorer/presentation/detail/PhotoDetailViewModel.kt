package com.unsplash.photoexplorer.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.unsplash.photoexplorer.domain.model.PhotoDetail
import com.unsplash.photoexplorer.domain.usecase.GetPhotoDetailUseCase
import com.unsplash.photoexplorer.domain.usecase.ToggleFavoriteUseCase
import com.unsplash.photoexplorer.presentation.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PhotoDetailUiState {
    data object Loading : PhotoDetailUiState
    data class Success(
        val detail: PhotoDetail,
        val isToggling: Boolean = false,
    ) : PhotoDetailUiState
    data class Error(val message: String) : PhotoDetailUiState
}

@HiltViewModel
class PhotoDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPhotoDetailUseCase: GetPhotoDetailUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
) : ViewModel() {

    private val photoId: String = savedStateHandle.toRoute<Route.PhotoDetail>().photoId

    private val _uiState = MutableStateFlow<PhotoDetailUiState>(PhotoDetailUiState.Loading)
    val uiState: StateFlow<PhotoDetailUiState> = _uiState.asStateFlow()

    private val _userMessages = Channel<String>(Channel.BUFFERED)
    val userMessages: Flow<String> = _userMessages.receiveAsFlow()

    init {
        load()
    }

    fun retry() {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = PhotoDetailUiState.Loading
            try {
                val detail = getPhotoDetailUseCase(photoId)
                _uiState.value = PhotoDetailUiState.Success(detail = detail)
            } catch (e: Exception) {
                _uiState.value = PhotoDetailUiState.Error(
                    message = e.message ?: "사진 정보를 불러오지 못했습니다",
                )
            }
        }
    }

    fun toggleFavorite() {
        val current = _uiState.value as? PhotoDetailUiState.Success ?: return
        if (current.isToggling) return
        val photo = current.detail.photo
        if (photo.id.isBlank()) return

        viewModelScope.launch {
            _uiState.update { (it as? PhotoDetailUiState.Success)?.copy(isToggling = true) ?: it }
            val willBeFavorite = !photo.isFavorite
            try {
                toggleFavoriteUseCase(photo)
                _uiState.update {
                    val s = it as? PhotoDetailUiState.Success ?: return@update it
                    s.copy(
                        detail = s.detail.copy(
                            photo = s.detail.photo.copy(isFavorite = willBeFavorite),
                        ),
                        isToggling = false,
                    )
                }
                _userMessages.send(
                    if (willBeFavorite) "Favorite 목록에 저장 되었습니다." else "Favorite 목록에서 삭제 되었습니다."
                )
            } catch (e: Exception) {
                _uiState.update { (it as? PhotoDetailUiState.Success)?.copy(isToggling = false) ?: it }
                _userMessages.send(e.message ?: "저장 중 오류가 발생했습니다")
            }
        }
    }
}
