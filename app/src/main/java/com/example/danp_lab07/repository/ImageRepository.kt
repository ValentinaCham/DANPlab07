package com.example.danp_lab07.repository

import android.net.Uri

/**
 * Public-facing API for product image storage. Callers (use-cases) only see
 * this interface — they don't know about Firebase.
 *
 * All operations suspend; failures should propagate so the UI can surface
 * them and the SyncWorker can retry.
 */
interface ImageRepository {
    /**
     * Uploads each local Uri sequentially. Returns the Storage paths in the
     * same order. Throws on any failure (caller decides whether to retry).
     */
    suspend fun uploadImages(localUris: List<Uri>, productId: Int): List<String>

    /** Removes a single image from Storage. Safe to call with blank paths. */
    suspend fun deleteImage(storagePath: String)

    /** Removes every image under the product's folder (called on delete). */
    suspend fun deleteAllForProduct(productId: Int)
}
