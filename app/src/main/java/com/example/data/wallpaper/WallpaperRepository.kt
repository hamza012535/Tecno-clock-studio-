package com.example.data.wallpaper

data class WallpaperSearchOutcome(
    val items: List<WallpaperItem>,
    val activeProviderName: String,
    val usedFallback: Boolean = false,
    val note: String? = null
)

class WallpaperRepository(
    private val primaryProvider: WallpaperProvider = WallhavenProvider(),
    private val secondaryProvider: WallpaperProvider = PicsumProvider()
) {

    suspend fun search(query: String, page: Int = 1): Result<WallpaperSearchOutcome> {
        val primaryResult = primaryProvider.search(query, page)
        primaryResult.getOrNull()?.let { items ->
            if (items.isNotEmpty()) {
                return Result.success(
                    WallpaperSearchOutcome(
                        items = items,
                        activeProviderName = primaryProvider.providerName,
                        usedFallback = false
                    )
                )
            }
        }

        // Automatic fallback to Lorem Picsum if Wallhaven fails or returns empty
        val fallbackResult = secondaryProvider.random(width = 1080, height = 2400)
        return fallbackResult.map { items ->
            WallpaperSearchOutcome(
                items = items,
                activeProviderName = secondaryProvider.providerName,
                usedFallback = true,
                note = primaryResult.exceptionOrNull()?.let {
                    "Wallhaven unavailable; showing ${secondaryProvider.providerName} wallpapers."
                } ?: "No Wallhaven matches for '$query'; showing ${secondaryProvider.providerName}."
            )
        }
    }

    suspend fun randomWallpapers(width: Int = 1080, height: Int = 2400): Result<WallpaperSearchOutcome> {
        return secondaryProvider.random(width, height).map { items ->
            WallpaperSearchOutcome(
                items = items,
                activeProviderName = secondaryProvider.providerName,
                usedFallback = false
            )
        }
    }
}
