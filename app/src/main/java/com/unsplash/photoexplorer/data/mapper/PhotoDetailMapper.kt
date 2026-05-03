package com.unsplash.photoexplorer.data.mapper

import com.unsplash.photoexplorer.data.remote.dto.ExifDto
import com.unsplash.photoexplorer.data.remote.dto.LocationDto
import com.unsplash.photoexplorer.data.remote.dto.PhotoDetailDto
import com.unsplash.photoexplorer.domain.model.Exif
import com.unsplash.photoexplorer.domain.model.Location
import com.unsplash.photoexplorer.domain.model.Photo
import com.unsplash.photoexplorer.domain.model.PhotoDetail

fun PhotoDetailDto.toPhotoDetail(isFavorite: Boolean): PhotoDetail = PhotoDetail(
    photo = Photo(
        id = id,
        imageUrls = urls.toPhotoUrls(),
        username = user.username,
        profileImageUrl = user.profileImageUrl(),
        width = width,
        height = height,
        description = description ?: altDescription,
        isFavorite = isFavorite,
        localPath = null,
    ),
    views = views,
    downloads = downloads,
    exif = exif?.toExif(),
    location = location?.toLocation(),
    tags = tags.map { it.title },
)

private fun ExifDto.toExif(): Exif = Exif(
    make = make,
    model = model,
    exposureTime = exposureTime,
    aperture = aperture,
    focalLength = focalLength,
    iso = iso,
)

private fun LocationDto.toLocation(): Location = Location(
    name = name,
    city = city,
    country = country,
    latitude = position?.latitude,
    longitude = position?.longitude,
)
