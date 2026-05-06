package com.unsplash.photoexplorer.data.mapper

import com.unsplash.photoexplorer.data.remote.dto.LocationDto
import com.unsplash.photoexplorer.data.remote.dto.PhotoDetailDto
import com.unsplash.photoexplorer.data.remote.dto.ProfileImageDto
import com.unsplash.photoexplorer.data.remote.dto.UrlsDto
import com.unsplash.photoexplorer.data.remote.dto.UserDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PhotoDetailMapperTest {

    private val baseDto = PhotoDetailDto(
        id = "detail1",
        width = 5000,
        height = 4000,
        description = "Main description",
        altDescription = "Alt description",
        urls = UrlsDto("raw", "full", "regular", "small", "thumb"),
        user = UserDto(
            username = "photographer",
            name = "Photo Grapher",
            profileImage = ProfileImageDto("s", "m", "l"),
        ),
        views = 12345,
        downloads = 678,
        exif = null,
        location = null,
        tags = emptyList(),
    )

    @Test
    fun `toPhotoDetail uses altDescription when description is null`() {
        val dto = baseDto.copy(description = null)

        val detail = dto.toPhotoDetail(isFavorite = false)

        assertEquals("Alt description", detail.photo.description)
    }

    @Test
    fun `toPhotoDetail handles null exif and location`() {
        val detail = baseDto.toPhotoDetail(isFavorite = false)

        assertNull(detail.exif)
        assertNull(detail.location)
    }

    @Test
    fun `toPhotoDetail handles location without position`() {
        val dto = baseDto.copy(
            location = LocationDto(
                name = "Unknown",
                city = null,
                country = null,
                position = null,
            ),
        )

        val detail = dto.toPhotoDetail(isFavorite = false)
        val location = detail.location!!

        assertEquals("Unknown", location.name)
        assertNull(location.latitude)
        assertNull(location.longitude)
    }
}
