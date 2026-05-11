package com.unsplash.photoexplorer.presentation.detail

import androidx.lifecycle.SavedStateHandle
import com.unsplash.photoexplorer.domain.model.Photo
import com.unsplash.photoexplorer.domain.model.PhotoDetail
import com.unsplash.photoexplorer.domain.model.PhotoUrls
import com.unsplash.photoexplorer.domain.usecase.GetPhotoDetailUseCase
import com.unsplash.photoexplorer.domain.usecase.ObserveFavoriteIdsUseCase
import com.unsplash.photoexplorer.presentation.common.FavoriteToggleManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getPhotoDetailUseCase = mockk<GetPhotoDetailUseCase>()
    private val favoriteToggleManager = mockk<FavoriteToggleManager>(relaxed = true)
    private val observeFavoriteIdsUseCase = mockk<ObserveFavoriteIdsUseCase>()
    private val favoriteIdsFlow = MutableStateFlow<Set<String>>(emptySet())

    private val testDetail = PhotoDetail(
        photo = Photo(
            id = "photo1",
            imageUrls = PhotoUrls("", "", "", "", ""),
            username = "user",
            name = "User",
            profileImageUrl = null,
            width = 100,
            height = 200,
            description = "desc",
            isFavorite = false,
            localPath = null,
        ),
        views = 500,
        downloads = 100,
        exif = null,
        location = null,
        tags = emptyList(),
    )

    private fun createViewModel(): PhotoDetailViewModel {
        every { observeFavoriteIdsUseCase() } returns favoriteIdsFlow
        every { favoriteToggleManager.togglingPhotoIds } returns MutableStateFlow(emptySet())
        every { favoriteToggleManager.messages } returns flowOf()

        return PhotoDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("photoId" to "photo1")),
            getPhotoDetailUseCase = getPhotoDetailUseCase,
            favoriteToggleManager = favoriteToggleManager,
            observeFavoriteIdsUseCase = observeFavoriteIdsUseCase,
        )
    }

    @Test
    fun `initial state is Loading before subscription`() = runTest {
        coEvery { getPhotoDetailUseCase("photo1") } returns testDetail

        val viewModel = createViewModel()

        // stateIn의 initialValue — 구독 전이므로 Loading
        assertEquals(PhotoDetailUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `uiState transitions to Success after load`() = runTest {
        coEvery { getPhotoDetailUseCase("photo1") } returns testDetail
        val viewModel = createViewModel()

        // WhileSubscribed를 활성화하기 위해 구독 시작
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        val state = viewModel.uiState.value as PhotoDetailUiState.Success
        assertEquals("photo1", state.detail.photo.id)
        assertEquals(500, state.detail.views)

        job.cancel()
    }

    @Test
    fun `uiState transitions to Error on failure`() = runTest {
        coEvery { getPhotoDetailUseCase("photo1") } throws IOException("Network error")
        val viewModel = createViewModel()

        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        val state = viewModel.uiState.value as PhotoDetailUiState.Error
        assertEquals("Network error", state.message)

        job.cancel()
    }

    @Test
    fun `retry reloads photo detail after error`() = runTest {
        coEvery { getPhotoDetailUseCase("photo1") } throws IOException("fail") andThen testDetail
        val viewModel = createViewModel()

        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        assertTrue(viewModel.uiState.value is PhotoDetailUiState.Error)

        viewModel.onIntent(PhotoDetailIntent.Retry)

        val state = viewModel.uiState.value as PhotoDetailUiState.Success
        assertEquals("photo1", state.detail.photo.id)

        job.cancel()
    }

    @Test
    fun `isFavorite updates when favoriteIds flow changes`() = runTest {
        coEvery { getPhotoDetailUseCase("photo1") } returns testDetail
        val viewModel = createViewModel()

        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        assertFalse((viewModel.uiState.value as PhotoDetailUiState.Success).detail.photo.isFavorite)

        favoriteIdsFlow.value = setOf("photo1")

        assertTrue((viewModel.uiState.value as PhotoDetailUiState.Success).detail.photo.isFavorite)

        job.cancel()
    }
}
