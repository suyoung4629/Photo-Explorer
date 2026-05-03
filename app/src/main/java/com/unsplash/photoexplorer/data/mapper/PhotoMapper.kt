package com.unsplash.photoexplorer.data.mapper

import com.unsplash.photoexplorer.data.local.entity.FavoritePhotoEntity
import com.unsplash.photoexplorer.data.remote.dto.PhotoDto
import com.unsplash.photoexplorer.data.remote.dto.UrlsDto
import com.unsplash.photoexplorer.data.remote.dto.UserDto
import com.unsplash.photoexplorer.domain.model.Photo
import com.unsplash.photoexplorer.domain.model.PhotoUrls

fun PhotoDto.toPhoto(isFavorite: Boolean): Photo = Photo(
    id = id,
    imageUrls = urls.toPhotoUrls(),
    username = user.username,
    name = user.name,
    profileImageUrl = user.profileImageUrl(),
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

fun UserDto.profileImageUrl(): String? =
    profileImage.medium.takeIf { it.isNotBlank() }
        ?: profileImage.small.takeIf { it.isNotBlank() }
        ?: profileImage.large.takeIf { it.isNotBlank() }

fun FavoritePhotoEntity.toPhoto(): Photo = Photo(
    id = id,
    imageUrls = PhotoUrls(raw = raw, full = full, regular = regular, small = small, thumb = thumb),
    username = username,
    name = name,
    profileImageUrl = profileImageUrl,
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
        name = name,
        profileImageUrl = profileImageUrl,
        width = width,
        height = height,
        description = description,
        localFilePath = localFilePath,
        addedAt = addedAt,
    )
