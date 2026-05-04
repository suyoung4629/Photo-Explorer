package com.unsplash.photoexplorer.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.unsplash.photoexplorer.domain.model.Photo
import com.unsplash.photoexplorer.domain.usecase.GetPhotoListUseCase
import com.unsplash.photoexplorer.domain.usecase.ObserveFavoriteIdsUseCase
import com.unsplash.photoexplorer.presentation.common.FavoriteToggleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class PhotoListViewModel @Inject constructor(
    getPhotoListUseCase: GetPhotoListUseCase,
    observeFavoriteIdsUseCase: ObserveFavoriteIdsUseCase,
    private val favoriteToggleManager: FavoriteToggleManager,
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

    val togglingPhotoIds: StateFlow<Set<String>> = favoriteToggleManager.togglingPhotoIds

    val userMessages: Flow<String> = favoriteToggleManager.messages

    fun toggleFavorite(photo: Photo) {
        favoriteToggleManager.toggle(photo, viewModelScope)
    }
}
