package com.unsplash.photoexplorer.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PhotoDetailDto(
    @SerialName("id") val id: String = "",
    @SerialName("width") val width: Int = 0,
    @SerialName("height") val height: Int = 0,
    @SerialName("description") val description: String? = null,
    @SerialName("alt_description") val altDescription: String? = null,
    @SerialName("urls") val urls: UrlsDto = UrlsDto(),
    @SerialName("user") val user: UserDto = UserDto(),
    @SerialName("views") val views: Int = 0,
    @SerialName("downloads") val downloads: Int = 0,
    @SerialName("exif") val exif: ExifDto? = null,
    @SerialName("location") val location: LocationDto? = null,
    @SerialName("tags") val tags: List<TagDto> = emptyList(),
)

@Serializable
data class ExifDto(
    @SerialName("make") val make: String? = null,
    @SerialName("model") val model: String? = null,
    @SerialName("exposure_time") val exposureTime: String? = null,
    @SerialName("aperture") val aperture: String? = null,
    @SerialName("focal_length") val focalLength: String? = null,
    @SerialName("iso") val iso: Int? = null,
)

@Serializable
data class LocationDto(
    @SerialName("name") val name: String? = null,
    @SerialName("city") val city: String? = null,
    @SerialName("country") val country: String? = null,
    @SerialName("position") val position: PositionDto? = null,
)

@Serializable
data class PositionDto(
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null,
)

@Serializable
data class TagDto(
    @SerialName("title") val title: String = "",
)
