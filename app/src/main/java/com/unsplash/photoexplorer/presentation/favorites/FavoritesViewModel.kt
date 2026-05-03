package com.unsplash.photoexplorer.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unsplash.photoexplorer.domain.model.Photo
import com.unsplash.photoexplorer.domain.usecase.GetFavoritePhotosUseCase
import com.unsplash.photoexplorer.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val photos: List<Photo> = emptyList(),
    val togglingPhotoIds: Set<String> = emptySet(),
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    getFavoritePhotosUseCase: GetFavoritePhotosUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
) : ViewModel() {

    /**
     * 토글 진행 중인 사진 ID 집합.
     */
    private val _togglingPhotoIds = MutableStateFlow<Set<String>>(emptySet())

    /**
     * 즐겨찾기 사진 목록 + 토글 진행 상태.
     * DB([GetFavoritePhotosUseCase]) Flow가 변경되면 자동 갱신.
     */
    val uiState: StateFlow<FavoritesUiState> = combine(
        getFavoritePhotosUseCase(),
        _togglingPhotoIds,
    ) { photos, ids ->
        FavoritesUiState(photos = photos, togglingPhotoIds = ids)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FavoritesUiState(),
    )

    /**
     * 1회성 사용자 메시지(Snackbar용).
     */
    private val _userMessages = Channel<String>(Channel.BUFFERED)
    val userMessages: Flow<String> = _userMessages.receiveAsFlow()

    /**
     * 즐겨찾기 토글. Favorites 화면에서는 항상 OFF 동작이지만,
     * 로직을 PhotoListViewModel과 일치시켜 향후 변경(예: 같은 화면에서 다시 추가)에도 대응 가능하게 둔다.
     */
    fun toggleFavorite(photo: Photo) {
        if (photo.id.isBlank()) return
        if (photo.id in _togglingPhotoIds.value) return
        viewModelScope.launch {
            _togglingPhotoIds.update { it + photo.id }
            val willBeFavorite = !photo.isFavorite
            try {
                toggleFavoriteUseCase(photo)
                _userMessages.send(
                    if (willBeFavorite) "Favorite 목록에 저장 되었습니다." else "Favorite 목록에서 삭제 되었습니다."
                )
            } catch (e: Exception) {
                _userMessages.send(e.message ?: "저장 중 오류가 발생했습니다")
            } finally {
                _togglingPhotoIds.update { it - photo.id }
            }
        }
    }
}
