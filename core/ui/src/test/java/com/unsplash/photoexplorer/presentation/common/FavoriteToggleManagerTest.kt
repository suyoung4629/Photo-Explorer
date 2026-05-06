package com.unsplash.photoexplorer.presentation.common

import app.cash.turbine.test
import com.unsplash.photoexplorer.domain.model.Photo
import com.unsplash.photoexplorer.domain.model.PhotoUrls
import com.unsplash.photoexplorer.domain.usecase.ToggleFavoriteUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class FavoriteToggleManagerTest {

    private val toggleFavoriteUseCase = mockk<ToggleFavoriteUseCase>()
    private val manager = FavoriteToggleManager(toggleFavoriteUseCase)

    private val testPhoto = Photo(
        id = "photo1",
        imageUrls = PhotoUrls("", "", "", "", ""),
        username = "user",
        name = null,
        profileImageUrl = null,
        width = 100,
        height = 200,
        description = null,
        isFavorite = false,
        localPath = null,
    )

    @Test
    fun `toggle adds photoId to togglingPhotoIds during operation`() = runTest {
        // Mock 내부에서 중간 상태 검증 (coAnswers = 호출 시점에 실행되는 블록)
        coEvery { toggleFavoriteUseCase(testPhoto) } coAnswers {
            assertTrue(testPhoto.id in manager.togglingPhotoIds.value)
        }

        manager.toggle(testPhoto, this)
        advanceUntilIdle()

        // 완료 후에는 제거됨
        assertFalse(testPhoto.id in manager.togglingPhotoIds.value)
    }

    @Test
    fun `toggle sends success message for adding favorite`() = runTest {
        coEvery { toggleFavoriteUseCase(testPhoto) } just runs

        manager.messages.test {
            manager.toggle(testPhoto, this@runTest)
            advanceUntilIdle()

            assertEquals("Favorite 목록에 저장 되었습니다.", awaitItem())
        }
    }

    @Test
    fun `toggle sends success message for removing favorite`() = runTest {
        val favoritePhoto = testPhoto.copy(isFavorite = true)
        coEvery { toggleFavoriteUseCase(favoritePhoto) } just runs

        manager.messages.test {
            manager.toggle(favoritePhoto, this@runTest)
            advanceUntilIdle()

            assertEquals("Favorite 목록에서 삭제 되었습니다.", awaitItem())
        }
    }

    @Test
    fun `toggle sends error message on failure`() = runTest {
        coEvery { toggleFavoriteUseCase(testPhoto) } throws IOException("저장 공간이 부족합니다")

        manager.messages.test {
            manager.toggle(testPhoto, this@runTest)
            advanceUntilIdle()

            assertEquals("저장 공간이 부족합니다", awaitItem())
        }
    }

    @Test
    fun `toggle ignores blank photoId`() = runTest {
        val blankIdPhoto = testPhoto.copy(id = "")

        manager.toggle(blankIdPhoto, this)
        advanceUntilIdle()

        coVerify(exactly = 0) { toggleFavoriteUseCase(any()) }
    }

    @Test
    fun `toggle ignores duplicate request for same photoId`() = runTest {
        // Mock 실행 중에 중복 요청 시도
        coEvery { toggleFavoriteUseCase(testPhoto) } coAnswers {
            manager.toggle(testPhoto, this@runTest) // 진행 중이므로 무시됨
        }

        manager.toggle(testPhoto, this)
        advanceUntilIdle()

        coVerify(exactly = 1) { toggleFavoriteUseCase(testPhoto) }
    }

    @Test
    fun `toggle cleans up togglingPhotoIds even on error`() = runTest {
        coEvery { toggleFavoriteUseCase(testPhoto) } throws RuntimeException("error")

        manager.toggle(testPhoto, this)
        advanceUntilIdle()

        assertFalse(testPhoto.id in manager.togglingPhotoIds.value)
    }
}
