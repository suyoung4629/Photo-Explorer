package com.unsplash.photoexplorer.presentation.list

import com.unsplash.photoexplorer.domain.usecase.GetPhotoListUseCase
import com.unsplash.photoexplorer.domain.usecase.ObserveFavoriteIdsUseCase
import com.unsplash.photoexplorer.presentation.common.FavoriteToggleManager
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.orbitmvi.orbit.test.test

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val getPhotoListUseCase = mockk<GetPhotoListUseCase>()
    private val observeFavoriteIdsUseCase = mockk<ObserveFavoriteIdsUseCase>()
    private val favoriteToggleManager = mockk<FavoriteToggleManager>(relaxed = true)

    private fun createViewModel(): PhotoListViewModel {
        every { getPhotoListUseCase() } returns flowOf(androidx.paging.PagingData.empty())
        every { observeFavoriteIdsUseCase() } returns flowOf(emptySet())
        every { favoriteToggleManager.togglingPhotoIds } returns MutableStateFlow(emptySet())
        every { favoriteToggleManager.messages } returns flowOf()

        return PhotoListViewModel(getPhotoListUseCase, observeFavoriteIdsUseCase, favoriteToggleManager)
    }

    @Test
    fun `photos flow is not null`() = runTest {
        val viewModel = createViewModel()

        assertNotNull(viewModel.photos)
    }

    @Test
    fun `togglingPhotoIds exposes manager state`() = runTest {
        val togglingFlow = MutableStateFlow(setOf("photo1"))
        every { getPhotoListUseCase() } returns flowOf(androidx.paging.PagingData.empty())
        every { observeFavoriteIdsUseCase() } returns flowOf(emptySet())
        every { favoriteToggleManager.togglingPhotoIds } returns togglingFlow
        every { favoriteToggleManager.messages } returns flowOf()

        val viewModel = PhotoListViewModel(getPhotoListUseCase, observeFavoriteIdsUseCase, favoriteToggleManager)

        viewModel.test(this) {
            runOnCreate()
            expectState { copy(togglingPhotoIds = setOf("photo1")) }
            cancelAndIgnoreRemainingItems()
        }
    }
}
