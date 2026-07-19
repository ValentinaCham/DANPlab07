package com.example.danp_lab07.repository

import android.net.Uri
import com.example.danp_lab07.data.remote.ImageStorageDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageRepositoryImpl @Inject constructor(
    private val storage: ImageStorageDataSource,
) : ImageRepository {

    override suspend fun uploadImages(localUris: List<Uri>, productId: Int): List<String> {
        if (localUris.isEmpty()) return emptyList()
        return localUris.mapIndexed { index, uri ->
            storage.uploadImage(uri, productId, index)
        }
    }

    override suspend fun deleteImage(storagePath: String) {
        if (storagePath.isNotBlank()) storage.deleteImage(storagePath)
    }

    override suspend fun deleteAllForProduct(productId: Int) {
        runCatching { storage.deleteAllProductImages(productId) }
            // best-effort; we never want a Storage failure to leak into the
            // product-delete UI flow
    }
}
