package com.unsplash.photoexplorer.data.mapper

import com.unsplash.photoexplorer.data.remote.dto.PhotoDto
import com.unsplash.photoexplorer.data.remote.dto.ProfileImageDto
import com.unsplash.photoexplorer.data.remote.dto.UserDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PhotoMapperTest {

    @Test
    fun `toPhoto uses altDescription when description is null`() {
        val dto = PhotoDto(description = null, altDescription = "alt text")

        val photo = dto.toPhoto(isFavorite = false)

        assertEquals("alt text", photo.description)
    }

    @Test
    fun `profileImageUrl returns medium first`() {
        val user = UserDto(
            profileImage = ProfileImageDto(small = "s", medium = "m", large = "l"),
        )

        assertEquals("m", user.profileImageUrl())
    }

    @Test
    fun `profileImageUrl falls back to small when medium is blank`() {
        val user = UserDto(
            profileImage = ProfileImageDto(small = "s", medium = "", large = "l"),
        )

        assertEquals("s", user.profileImageUrl())
    }

    @Test
    fun `profileImageUrl falls back to large when medium and small are blank`() {
        val user = UserDto(
            profileImage = ProfileImageDto(small = "", medium = "", large = "l"),
        )

        assertEquals("l", user.profileImageUrl())
    }

    @Test
    fun `profileImageUrl returns null when all are blank`() {
        val user = UserDto(
            profileImage = ProfileImageDto(small = "", medium = "", large = ""),
        )

        assertNull(user.profileImageUrl())
    }
}
