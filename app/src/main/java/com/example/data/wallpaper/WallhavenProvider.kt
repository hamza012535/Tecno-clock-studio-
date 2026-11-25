package com.example.data.wallpaper

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class WallhavenSearchResponse(
    val data: List<WallhavenWallpaperDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class WallhavenWallpaperDto(
    val id: String,
    val resolution: String = "",
    val path: String,
    val thumbs: WallhavenThumbsDto
)

@JsonClass(generateAdapter = true)
data class WallhavenThumbsDto(
    val large: String,
    val original: String = "",
    val small: String = ""
)

interface WallhavenApiService {
    @GET("search")
    suspend fun searchWallpapers(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("purity") purity: String = "100",
        @Query("categories") categories: String = "111",
        @Query("sorting") sorting: String = "relevance"
    ): WallhavenSearchResponse
}

/**
 * Keyless SFW Wallhaven provider using https://wallhaven.cc/api/v1/search.
 * Images are loaded directly from Wallhaven CDN via Coil without proxying or re-hosting.
 */
class WallhavenProvider : WallpaperProvider {

    override val providerName: String = "Wallhaven"

    private val api: WallhavenApiService by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
        Retrofit.Builder()
            .baseUrl("https://wallhaven.cc/api/v1/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(WallhavenApiService::class.java)
    }

    override suspend fun search(query: String, page: Int): Result<List<WallpaperItem>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val effectiveQuery = query.trim().ifEmpty { "amoled dark minimal" }
                val response = api.searchWallpapers(
                    query = effectiveQuery,
                    page = page,
                    purity = "100",
                    sorting = "relevance"
                )
                response.data.map { dto ->
                    WallpaperItem(
                        id = "wh_${dto.id}",
                        thumbUrl = dto.thumbs.large,
                        fullUrl = dto.path,
                        resolution = dto.resolution.ifBlank { "HD" },
                        source = "Wallhaven"
                    )
                }
            }
        }

    override suspend fun random(width: Int, height: Int): Result<List<WallpaperItem>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = api.searchWallpapers(
                    query = "dark minimal",
                    page = 1,
                    purity = "100",
                    sorting = "random"
                )
                response.data.map { dto ->
                    WallpaperItem(
                        id = "wh_${dto.id}",
                        thumbUrl = dto.thumbs.large,
                        fullUrl = dto.path,
                        resolution = dto.resolution.ifBlank { "${width}x${height}" },
                        source = "Wallhaven"
                    )
                }
            }
        }
}
