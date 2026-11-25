package com.example.data.wallpaper

data class WallpaperItem(
    val id: String,
    val thumbUrl: String,
    val fullUrl: String,
    val resolution: String,
    val source: String,
    val author: String? = null
)

interface WallpaperProvider {
    val providerName: String
    suspend fun search(query: String, page: Int = 1): Result<List<WallpaperItem>>
    suspend fun random(width: Int = 1080, height: Int = 2400): Result<List<WallpaperItem>>
}
