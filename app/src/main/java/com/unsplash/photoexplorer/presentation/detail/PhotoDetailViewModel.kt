package com.unsplash.photoexplorer.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.unsplash.photoexplorer.domain.model.PhotoDetail
import com.unsplash.photoexplorer.domain.usecase.GetPhotoDetailUseCase
import com.unsplash.photoexplorer.domain.usecase.ObserveFavoriteIdsUseCase
import com.unsplash.photoexplorer.domain.usecase.ToggleFavoriteUseCase
import com.unsplash.photoexplorer.presentation.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
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
    observeFavoriteIdsUseCase: ObserveFavoriteIdsUseCase,
) : ViewModel() {

    private val photoId: String = savedStateHandle.toRoute<Route.PhotoDetail>().photoId

    private sealed interface LoadState {
        data object Loading : LoadState
        data class Loaded(val detail: PhotoDetail) : LoadState
        data class Error(val message: String) : LoadState
    }

    private val _loadState = MutableStateFlow<LoadState>(LoadState.Loading)
    private val _isToggling = MutableStateFlow(false)

    val uiState: StateFlow<PhotoDetailUiState> = combine(
        _loadState,
        observeFavoriteIdsUseCase(),
        _isToggling,
    ) { loadState, favoriteIds, isToggling ->
        when (loadState) {
            is LoadState.Loading -> PhotoDetailUiState.Loading
            is LoadState.Error -> PhotoDetailUiState.Error(loadState.message)
            is LoadState.Loaded -> {
                val detail = loadState.detail
                PhotoDetailUiState.Success(
                    detail = detail.copy(
                        photo = detail.photo.copy(isFavorite = detail.photo.id in favoriteIds),
                    ),
                    isToggling = isToggling,
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PhotoDetailUiState.Loading,
    )

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
            _loadState.value = LoadState.Loading
            try {
                val detail = getPhotoDetailUseCase(photoId)
                _loadState.value = LoadState.Loaded(detail = detail)
            } catch (e: Exception) {
                _loadState.value = LoadState.Error(
                    message = e.message ?: "사진 정보를 불러오지 못했습니다",
                )
            }
        }
    }

    fun toggleFavorite() {
        val current = uiState.value as? PhotoDetailUiState.Success ?: return
        if (_isToggling.value) return
        val photo = current.detail.photo
        if (photo.id.isBlank()) return

        viewModelScope.launch {
            _isToggling.value = true
            val willBeFavorite = !photo.isFavorite
            try {
                toggleFavoriteUseCase(photo)
                _userMessages.send(
                    if (willBeFavorite) "Favorite 목록에 저장 되었습니다." else "Favorite 목록에서 삭제 되었습니다."
                )
            } catch (e: Exception) {
                _userMessages.send(e.message ?: "저장 중 오류가 발생했습니다")
            } finally {
                _isToggling.value = false
            }
        }
    }
}
