package com.unsplash.photoexplorer.domain.model

data class PhotoDetail(
    val photo: Photo,
    val views: Int,
    val downloads: Int,
    val exif: Exif?,
    val location: Location?,
    val tags: List<String>,
)

data class Exif(
    val make: String?,
    val model: String?,
    val exposureTime: String?,
    val aperture: String?,
    val focalLength: String?,
    val iso: Int?,
)

data class Location(
    val name: String?,
    val city: String?,
    val country: String?,
    val latitude: Double?,
    val longitude: Double?,
)
