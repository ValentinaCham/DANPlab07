package com.example.danp_lab07.data.remote

import android.net.Uri

/**
 * Abstraction over Firebase Storage so the domain layer never depends on
 * Firebase directly. Implementations MUST be safe to call when the device is
 * offline — they should suspend until the upload/download task completes.
 */
interface ImageStorageDataSource {
    /**
     * Uploads a local image and returns its Storage path (NOT a download URL).
     * The caller is responsible for persisting that path in Room + Firestore.
     */
    suspend fun uploadImage(localUri: Uri, productId: Int, imageIndex: Int): String

    /**
     * Deletes a single image by Storage path. No-op if the path is blank.
     * @return true if the file existed and was deleted, false otherwise.
     */
    suspend fun deleteImage(storagePath: String): Boolean

    /**
     * Deletes every file under the product's image folder. Used when the
     * product itself is deleted so we never leak orphan blobs.
     */
    suspend fun deleteAllProductImages(productId: Int): Boolean
}
