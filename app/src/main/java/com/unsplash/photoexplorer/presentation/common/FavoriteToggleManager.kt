package com.unsplash.photoexplorer.presentation.common

import com.unsplash.photoexplorer.domain.model.Photo
import com.unsplash.photoexplorer.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteToggleManager  @Inject constructor(
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
) {

    /**
     * 현재 즐겨찾기 토글이 진행 중인 인 사진 ID 집합.
     * UI에서 로딩 인디케이터 표시 및 중복 요청 방지에 사용된다.
     */
    private val _togglingPhotoIds = MutableStateFlow<Set<String>>(emptySet())
    val togglingPhotoIds: StateFlow<Set<String>> = _togglingPhotoIds.asStateFlow()

    /**
     * 토글 결과를 알리는 1회성 메시지 채널.
     * 각 화면의 Snackbar에서 collect하여 사용자에게 표시한다.
     */
    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    fun toggle(photo: Photo, scope: CoroutineScope) {
        if (photo.id.isBlank()) return
        if (photo.id in _togglingPhotoIds.value) return
        scope.launch {
            _togglingPhotoIds.update { it + photo.id }
            val willBeFavorite = !photo.isFavorite
            try {
                toggleFavoriteUseCase(photo)
                _messages.send(
                    if (willBeFavorite) "Favorite 목록에 저장 되었습니다." else "Favorite 목록에서 삭제 되었습니다."
                )
            } catch (e: Exception) {
                _messages.send(e.message ?: "저장 중 오류가 발생했습니다")
            } finally {
                _togglingPhotoIds.update { it - photo.id }
            }
        }
    }
}
