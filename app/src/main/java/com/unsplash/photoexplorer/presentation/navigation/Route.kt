package com.unsplash.photoexplorer.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface Route {

    @Serializable
    data object PhotoList : Route

    @Serializable
    data class PhotoDetail(val photoId: String) : Route

    @Serializable
    data object Favorites : Route
}
