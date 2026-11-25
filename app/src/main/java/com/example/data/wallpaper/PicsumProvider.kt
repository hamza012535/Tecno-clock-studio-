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
import kotlin.random.Random

@JsonClass(generateAdapter = true)
data class PicsumPhotoDto(
    val id: String,
    val author: String = "",
    val width: Int = 1080,
    val height: Int = 2400,
    val url: String = "",
    @Json(name = "download_url") val downloadUrl: String = ""
)

interface PicsumApiService {
    @GET("v2/list")
    suspend fun listPhotos(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 18
    ): List<PicsumPhotoDto>
}

/**
 * Keyless Lorem Picsum provider returning exact Tecno-screen-proportioned wallpapers (1080x2400).
 */
class PicsumProvider : WallpaperProvider {

    override val providerName: String = "Lorem Picsum"

    private val api: PicsumApiService by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .build()
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
        Retrofit.Builder()
            .baseUrl("https://picsum.photos/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(PicsumApiService::class.java)
    }

    override suspend fun search(query: String, page: Int): Result<List<WallpaperItem>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val photos = api.listPhotos(page = page, limit = 18)
                photos.map { dto ->
                    WallpaperItem(
                        id = "picsum_${dto.id}",
                        thumbUrl = "https://picsum.photos/id/${dto.id}/360/640",
                        fullUrl = "https://picsum.photos/id/${dto.id}/1080/2400",
                        resolution = "1080x2400",
                        source = "Lorem Picsum",
                        author = dto.author
                    )
                }
            }
        }

    override suspend fun random(width: Int, height: Int): Result<List<WallpaperItem>> =
        withContext(Dispatchers.IO) {
            val apiResult = runCatching {
                val randomPage = Random.nextInt(1, 25)
                val photos = api.listPhotos(page = randomPage, limit = 18).shuffled()
                photos.map { dto ->
                    WallpaperItem(
                        id = "picsum_${dto.id}_${randomPage}",
                        thumbUrl = "https://picsum.photos/id/${dto.id}/360/640",
                        fullUrl = "https://picsum.photos/id/${dto.id}/$width/$height",
                        resolution = "${width}x${height}",
                        source = "Lorem Picsum",
                        author = dto.author
                    )
                }
            }
            if (apiResult.isSuccess && !apiResult.getOrNull().isNullOrEmpty()) {
                apiResult
            } else {
                // Direct keyless seed URLs as resilient fallback
                val baseSeed = (System.currentTimeMillis() / 1000L).toInt()
                val items = (1..12).map { index ->
                    val seed = baseSeed + index
                    WallpaperItem(
                        id = "picsum_rand_$seed",
                        thumbUrl = "https://picsum.photos/360/640?random=$seed",
                        fullUrl = "https://picsum.photos/$width/$height?random=$seed",
                        resolution = "${width}x${height}",
                        source = "Lorem Picsum"
                    )
                }
                Result.success(items)
            }
        }
}
