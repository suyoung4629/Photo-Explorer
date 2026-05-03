package com.unsplash.photoexplorer.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.unsplash.photoexplorer.domain.model.Photo
import com.unsplash.photoexplorer.domain.usecase.GetPhotoListUseCase
import com.unsplash.photoexplorer.domain.usecase.ObserveFavoriteIdsUseCase
import com.unsplash.photoexplorer.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PhotoListUiState(
    val togglingPhotoIds: Set<String> = emptySet(),
)

@HiltViewModel
class PhotoListViewModel @Inject constructor(
    getPhotoListUseCase: GetPhotoListUseCase,
    observeFavoriteIdsUseCase: ObserveFavoriteIdsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
) : ViewModel() {

    /**
     * 화면에 표시할 사진 목록.
     *
     * Pager의 결과를 [viewModelScope]에 cachedIn하여 좋아요 상태가 바뀔 때마다
     * 페이지를 다시 가져오지 않도록 한다. 그 위에 [observeFavoriteIdsUseCase]의
     * 즐겨찾기 ID 집합을 [combine]하여, 각 [Photo.isFavorite]를 실시간으로 갱신한다.
     *
     * 결과적으로 사용자가 다른 화면에서 즐겨찾기를 토글해도 이 목록의 하트 상태가
     * 자동으로 동기화된다.
     */
    val photos: Flow<PagingData<Photo>> = combine(
        getPhotoListUseCase().cachedIn(viewModelScope),
        observeFavoriteIdsUseCase(),
    ) { pagingData, favoriteIds ->
        pagingData.map { photo -> photo.copy(isFavorite = photo.id in favoriteIds) }
    }

    /**
     * 화면 UI 상태
     * PhotoListUiState : 현재는 토글 진행 중인 사진 ID만 포함. (다운로드/삭제 시 인디케이터 표시 용)
     */
    private val _uiState = MutableStateFlow(PhotoListUiState())
    val uiState: StateFlow<PhotoListUiState> = _uiState.asStateFlow()

    /**
     * 1회성 사용자 메시지(Snackbar용).
     */
    private val _userMessages = Channel<String>(Channel.BUFFERED)
    val userMessages: Flow<String> = _userMessages.receiveAsFlow()

    /**
     * 사진의 즐겨찾기 상태를 토글한다.
     */
    fun toggleFavorite(photo: Photo) {
        if (photo.id.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(togglingPhotoIds = it.togglingPhotoIds + photo.id) }
            val wasAdding = !photo.isFavorite
            try {
                toggleFavoriteUseCase(photo)
                _userMessages.send(
                    if (wasAdding) "Favorite 목록에 저장 되었습니다." else "Favorite 목록에서 삭제 되었습니다."
                )
            } catch (e: Exception) {
                _userMessages.send(e.message ?: "저장 중 오류가 발생했습니다")
            } finally {
                _uiState.update { it.copy(togglingPhotoIds = it.togglingPhotoIds - photo.id) }
            }
        }
    }
}
