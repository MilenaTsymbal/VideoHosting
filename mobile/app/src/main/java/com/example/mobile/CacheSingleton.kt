package com.example.mobile.util

import android.content.Context
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import java.io.File

object CacheSingleton {
    @Volatile
    private var cache: SimpleCache? = null

    fun getInstance(context: Context): SimpleCache {
        return cache ?: synchronized(this) {
            cache ?: run {
                val cacheDir = File(context.cacheDir, "media")
                val evictor = LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024)
                val databaseProvider = ExoDatabaseProvider(context)
                SimpleCache(cacheDir, evictor, databaseProvider).also { cache = it }
            }
        }
    }
}