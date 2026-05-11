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
import kotlinx.coroutines.flow.combine
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

data class PhotoListUiState(
    val togglingPhotoIds: Set<String> = emptySet(),
)

sealed interface PhotoListSideEffect {
    data class ShowMessage(val message: String) : PhotoListSideEffect
}

@HiltViewModel
class PhotoListViewModel @Inject constructor(
    getPhotoListUseCase: GetPhotoListUseCase,
    observeFavoriteIdsUseCase: ObserveFavoriteIdsUseCase,
    private val favoriteToggleManager: FavoriteToggleManager,
) : ContainerHost<PhotoListUiState, PhotoListSideEffect>, ViewModel() {

    override val container = container<PhotoListUiState, PhotoListSideEffect>(PhotoListUiState()) {
        observeTogglingIds()
        observeMessages()
    }

    /**
     * PagingData는 자체 상태를 관리하는 스트림이므로 Orbit State 외부에 별도 Flow로 유지한다.
     */
    val photos: Flow<PagingData<Photo>> = combine(
        getPhotoListUseCase().cachedIn(viewModelScope),
        observeFavoriteIdsUseCase(),
    ) { pagingData, favoriteIds ->
        pagingData.map { photo -> photo.copy(isFavorite = photo.id in favoriteIds) }
    }

    private fun observeTogglingIds() = intent {
        favoriteToggleManager.togglingPhotoIds.collect { ids ->
            reduce { state.copy(togglingPhotoIds = ids) }
        }
    }

    private fun observeMessages() = intent {
        favoriteToggleManager.messages.collect { message ->
            postSideEffect(PhotoListSideEffect.ShowMessage(message))
        }
    }

    fun toggleFavorite(photo: Photo) = intent {
        favoriteToggleManager.toggle(photo, viewModelScope)
    }
}
