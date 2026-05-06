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
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

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
    fun `initial uiState has empty photos`() {
        val viewModel = createViewModel()

        assertEquals(FavoritesUiState(), viewModel.uiState.value)
    }

    @Test
    fun `uiState emits favorite photos`() = runTest {
        val viewModel = createViewModel()

        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        val state = viewModel.uiState.value
        assertEquals(2, state.photos.size)
        assertEquals("fav1", state.photos[0].id)
        assertEquals("fav2", state.photos[1].id)

        job.cancel()
    }

    @Test
    fun `uiState includes togglingPhotoIds`() = runTest {
        val viewModel = createViewModel(togglingIds = setOf("fav1"))

        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        assertTrue("fav1" in viewModel.uiState.value.togglingPhotoIds)

        job.cancel()
    }

    @Test
    fun `uiState emits empty list when no favorites`() = runTest {
        val viewModel = createViewModel(photos = emptyList())

        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        assertTrue(viewModel.uiState.value.photos.isEmpty())

        job.cancel()
    }

}
