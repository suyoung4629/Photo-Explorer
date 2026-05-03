package com.unsplash.photoexplorer.data.mapper

import com.unsplash.photoexplorer.data.local.entity.FavoritePhotoEntity
import com.unsplash.photoexplorer.data.remote.dto.PhotoDto
import com.unsplash.photoexplorer.data.remote.dto.UrlsDto
import com.unsplash.photoexplorer.domain.model.Photo
import com.unsplash.photoexplorer.domain.model.PhotoUrls

fun PhotoDto.toPhoto(isFavorite: Boolean): Photo = Photo(
    id = id,
    imageUrls = urls.toPhotoUrls(),
    username = user.username,
    width = width,
    height = height,
    description = description ?: altDescription,
    isFavorite = isFavorite,
    localPath = null,
)

fun UrlsDto.toPhotoUrls(): PhotoUrls = PhotoUrls(
    raw = raw,
    full = full,
    regular = regular,
    small = small,
    thumb = thumb,
)

fun FavoritePhotoEntity.toPhoto(): Photo = Photo(
    id = id,
    imageUrls = PhotoUrls(raw = raw, full = full, regular = regular, small = small, thumb = thumb),
    username = username,
    width = width,
    height = height,
    description = description,
    isFavorite = true,
    localPath = localFilePath,
)

fun Photo.toFavoriteEntity(localFilePath: String, addedAt: Long): FavoritePhotoEntity =
    FavoritePhotoEntity(
        id = id,
        raw = imageUrls.raw,
        full = imageUrls.full,
        regular = imageUrls.regular,
        small = imageUrls.small,
        thumb = imageUrls.thumb,
        username = username,
        width = width,
        height = height,
        description = description,
        localFilePath = localFilePath,
        addedAt = addedAt,
    )
