package com.wilkins.safezone

import android.app.Application
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Clase Application personalizada para configurar Coil
 * Esto soluciona el problema de carga de imágenes desde Supabase
 */
class SafeZoneApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        Log.d("SafeZoneApplication", "🚀 Application iniciada")
        Log.d("SafeZoneApplication", "✅ ImageLoader personalizado configurado")
    }

    override fun newImageLoader(): ImageLoader {
        Log.d("SafeZoneApplication", "🔧 Creando ImageLoader con configuración personalizada")

        // Cliente HTTP con timeouts más largos
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return ImageLoader.Builder(this)
            .okHttpClient(okHttpClient)
            // Caché en memoria
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) // 25% de la RAM disponible
                    .build()
            }
            // Caché en disco
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02) // 2% del almacenamiento
                    .build()
            }
            // Políticas de caché
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            // Transiciones
            .crossfade(true)
            // Logger para debugging (quitar en producción)
            .logger(DebugLogger())
            .build()
            .also {
                Log.d("SafeZoneApplication", "✅ ImageLoader creado exitosamente")
            }
    }
}