package com.unsplash.photoexplorer.data.repository

import com.unsplash.photoexplorer.data.local.dao.FavoritePhotoDao
import com.unsplash.photoexplorer.data.local.entity.FavoritePhotoEntity
import com.unsplash.photoexplorer.data.local.store.PhotoFileStore
import com.unsplash.photoexplorer.data.remote.api.UnsplashApi
import com.unsplash.photoexplorer.data.remote.dto.DownloadResponseDto
import com.unsplash.photoexplorer.data.remote.dto.PhotoDetailDto
import com.unsplash.photoexplorer.data.remote.dto.UrlsDto
import com.unsplash.photoexplorer.data.remote.dto.UserDto
import com.unsplash.photoexplorer.domain.model.Photo
import com.unsplash.photoexplorer.domain.model.PhotoUrls
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.IOException

class PhotoRepositoryImplTest {

    private val api = mockk<UnsplashApi>()
    private val dao = mockk<FavoritePhotoDao>()
    private val fileStore = mockk<PhotoFileStore>()
    private val repository = PhotoRepositoryImpl(api, dao, fileStore)

    private val testPhoto = Photo(
        id = "photo1",
        imageUrls = PhotoUrls("raw", "full", "regular", "small", "thumb"),
        username = "user",
        name = "User",
        profileImageUrl = null,
        width = 100,
        height = 200,
        description = "desc",
        isFavorite = false,
        localPath = null,
    )

    // region getPhotoDetail

    @Test
    fun `getPhotoDetail returns mapped detail with isFavorite false`() = runTest {
        val dto = PhotoDetailDto(
            id = "photo1",
            width = 100,
            height = 200,
            description = "desc",
            urls = UrlsDto("raw", "full", "regular", "small", "thumb"),
            user = UserDto(username = "user", name = "User"),
            views = 500,
            downloads = 100,
        )
        coEvery { api.getPhotoDetail("photo1") } returns dto

        val result = repository.getPhotoDetail("photo1")

        assertEquals("photo1", result.photo.id)
        assertEquals(500, result.views)
        assertFalse(result.photo.isFavorite)
    }

    @Test(expected = IOException::class)
    fun `getPhotoDetail propagates network error`() = runTest {
        coEvery { api.getPhotoDetail("photo1") } throws IOException("Network error")

        repository.getPhotoDetail("photo1")
    }

    // endregion

    // region toggleFavorite — 삭제 흐름

    @Test
    fun `toggleFavorite removes favorite when already exists`() = runTest {
        val entity = FavoritePhotoEntity(
            id = "photo1",
            raw = "raw", full = "full", regular = "regular", small = "small", thumb = "thumb",
            username = "user", name = "User", profileImageUrl = null,
            width = 100, height = 200, description = "desc",
            localFilePath = "/path/photo1.jpg",
            addedAt = 1000L,
        )
        coEvery { dao.getById("photo1") } returns entity
        coEvery { fileStore.delete("/path/photo1.jpg") } just runs
        coEvery { dao.deleteById("photo1") } just runs

        repository.toggleFavorite(testPhoto)

        coVerifyOrder {
            fileStore.delete("/path/photo1.jpg")
            dao.deleteById("photo1")
        }
    }

    // endregion

    // region toggleFavorite — 추가 흐름

    @Test
    fun `toggleFavorite adds favorite when not exists`() = runTest {
        coEvery { dao.getById("photo1") } returns null
        coEvery { api.trackDownload("photo1") } returns DownloadResponseDto("https://download.url")
        coEvery { fileStore.download("https://download.url", "photo1") } returns "/saved/photo1.jpg"
        coEvery { dao.insert(any()) } just runs

        repository.toggleFavorite(testPhoto)

        coVerifyOrder {
            api.trackDownload("photo1")
            fileStore.download("https://download.url", "photo1")
            dao.insert(any())
        }
    }

    @Test(expected = IOException::class)
    fun `toggleFavorite propagates download error`() = runTest {
        coEvery { dao.getById("photo1") } returns null
        coEvery { api.trackDownload("photo1") } returns DownloadResponseDto("https://download.url")
        coEvery { fileStore.download(any(), any()) } throws IOException("저장 공간이 부족합니다")

        repository.toggleFavorite(testPhoto)
    }

    // endregion

    // region observeFavoriteIds

    @Test
    fun `observeFavoriteIds returns set from dao`() = runTest {
        every { dao.observeFavoriteIds() } returns flowOf(listOf("id1", "id2"))

        val result = repository.observeFavoriteIds().first()

        assertEquals(setOf("id1", "id2"), result)
    }

    // endregion

    // region trackDownload

    @Test
    fun `trackDownload returns url from api`() = runTest {
        coEvery { api.trackDownload("photo1") } returns DownloadResponseDto("https://track.url")

        val result = repository.trackDownload("photo1")

        assertEquals("https://track.url", result)
    }

    // endregion
}
