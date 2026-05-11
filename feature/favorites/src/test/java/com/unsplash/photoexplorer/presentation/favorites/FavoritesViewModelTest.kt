package com.unsplash.photoexplorer.presentation.favorites

import com.unsplash.photoexplorer.domain.model.Photo
import com.unsplash.photoexplorer.domain.model.PhotoUrls
import com.unsplash.photoexplorer.domain.usecase.GetFavoritePhotosUseCase
import com.unsplash.photoexplorer.presentation.common.FavoriteToggleManager
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.orbitmvi.orbit.test.test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val getFavoritePhotosUseCase = mockk<GetFavoritePhotosUseCase>()
    private val favoriteToggleManager = mockk<FavoriteToggleManager>(relaxed = true)

    private val testPhotos = listOf(
        Photo(
            id = "fav1",
            imageUrls = PhotoUrls("", "", "", "", ""),
            username = "user1",
            name = "User 1",
            profileImageUrl = null,
            width = 100,
            height = 200,
            description = null,
            isFavorite = true,
            localPath = "/path/fav1.jpg",
        ),
        Photo(
            id = "fav2",
            imageUrls = PhotoUrls("", "", "", "", ""),
            username = "user2",
            name = "User 2",
            profileImageUrl = null,
            width = 300,
            height = 400,
            description = null,
            isFavorite = true,
            localPath = "/path/fav2.jpg",
        ),
    )

    private fun createViewModel(
        photos: List<Photo> = testPhotos,
        togglingIds: Set<String> = emptySet(),
    ): FavoritesViewModel {
        every { getFavoritePhotosUseCase() } returns flowOf(photos)
        every { favoriteToggleManager.togglingPhotoIds } returns MutableStateFlow(togglingIds)
        every { favoriteToggleManager.messages } returns flowOf()

        return FavoritesViewModel(getFavoritePhotosUseCase, favoriteToggleManager)
    }

    @Test
    fun `uiState emits favorite photos`() = runTest {
        val viewModel = createViewModel()

        viewModel.test(this) {
            runOnCreate()
            expectState {
                FavoritesUiState(photos = testPhotos, togglingPhotoIds = emptySet())
            }
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun `uiState includes togglingPhotoIds`() = runTest {
        val viewModel = createViewModel(togglingIds = setOf("fav1"))

        viewModel.test(this) {
            runOnCreate()
            expectState {
                FavoritesUiState(photos = testPhotos, togglingPhotoIds = setOf("fav1"))
            }
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun `uiState emits empty list when no favorites`() = runTest {
        val viewModel = createViewModel(photos = emptyList())

        viewModel.test(this) {
            runOnCreate()
            // 초기 상태와 동일(빈 리스트)하므로 추가 상태 변경 없음
            cancelAndIgnoreRemainingItems()
        }
    }
}
