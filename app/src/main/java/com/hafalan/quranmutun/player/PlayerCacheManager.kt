package com.hafalan.quranmutun.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

@OptIn(UnstableApi::class)
object PlayerCacheManager {
    private const val MAX_CACHE_BYTES = 150L * 1024L * 1024L // 150 MB

    @Volatile
    private var cache: SimpleCache? = null

    fun getCache(context: Context): SimpleCache {
        return cache ?: synchronized(this) {
            cache ?: run {
                val cacheDir = File(context.applicationContext.cacheDir, "audio_cache")
                val dbProvider = StandaloneDatabaseProvider(context.applicationContext)
                SimpleCache(cacheDir, LeastRecentlyUsedCacheEvictor(MAX_CACHE_BYTES), dbProvider)
                    .also { cache = it }
            }
        }
    }
}
