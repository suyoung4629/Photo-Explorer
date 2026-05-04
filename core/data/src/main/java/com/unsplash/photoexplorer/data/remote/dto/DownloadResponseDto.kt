package com.unsplash.photoexplorer.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DownloadResponseDto(
    @SerialName("url") val url: String = "",
)
