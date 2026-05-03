package com.unsplash.photoexplorer.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PhotoDto(
    @SerialName("id") val id: String = "",
    @SerialName("width") val width: Int = 0,
    @SerialName("height") val height: Int = 0,
    @SerialName("description") val description: String? = null,
    @SerialName("alt_description") val altDescription: String? = null,
    @SerialName("urls") val urls: UrlsDto = UrlsDto(),
    @SerialName("user") val user: UserDto = UserDto(),
)

@Serializable
data class UrlsDto(
    @SerialName("raw") val raw: String = "",
    @SerialName("full") val full: String = "",
    @SerialName("regular") val regular: String = "",
    @SerialName("small") val small: String = "",
    @SerialName("thumb") val thumb: String = "",
)

@Serializable
data class UserDto(
    @SerialName("username") val username: String = "",
    @SerialName("name") val name: String? = null,
    @SerialName("profile_image") val profileImage: ProfileImageDto = ProfileImageDto(),
)

@Serializable
data class ProfileImageDto(
    @SerialName("small") val small: String = "",
    @SerialName("medium") val medium: String = "",
    @SerialName("large") val large: String = "",
)
