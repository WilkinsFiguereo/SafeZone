package com.wilkins.safezone.backend.network.User.Form

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import java.util.UUID

suspend fun uploadImageToSupabase(
    supabase: SupabaseClient,
    fileBytes: ByteArray,
    userId: String
): String {
    val bucket = supabase.storage.from("reports")

    val fileName = "${userId}/${UUID.randomUUID()}.jpg"

    bucket.upload(
        path = fileName,
        data = fileBytes,
        upsert = false
    )

    return fileName  // esto es lo que guardas en image_url
}
