package com.example.danp_lab07.domain

import android.net.Uri
import com.example.danp_lab07.data.Product
import com.example.danp_lab07.repository.ImageRepository
import javax.inject.Inject

/**
 * Each entry can be either:
 *  * a fresh local content Uri (needs to be uploaded to Storage), or
 *  * a Storage path string we already have (kept as-is, not re-uploaded).
 *
 * The form screen produces this list — Storage paths from the existing
 * Product mixed with newly picked Uris.
 */
sealed interface PendingImage {
    data class Local(val uri: Uri) : PendingImage
    data class Remote(val storagePath: String) : PendingImage
}

class UploadProductImagesUseCase @Inject constructor(
    private val imageRepository: ImageRepository,
) {
    /**
     * Resolves a list of pending entries into a final list of Storage paths
     * ready to persist in Room/Firestore. Throws on upload failure so the
     * ViewModel can surface it.
     */
    suspend operator fun invoke(
        productId: Int,
        images: List<PendingImage>,
    ): List<String> {
        val locals = images.mapIndexedNotNull { index, item ->
            if (item is PendingImage.Local) index to item.uri else null
        }
        val uploadedByIndex: Map<Int, String> =
            if (productId > 0 && locals.isNotEmpty()) {
                imageRepository.uploadImages(
                    localUris = locals.map { it.second },
                    productId = productId,
                ).mapIndexed { idx, path -> locals[idx].first to path }.toMap()
            } else emptyMap()

        return images.mapIndexed { index, item ->
            when (item) {
                is PendingImage.Local -> uploadedByIndex[index]
                    ?: error("Upload result missing for index $index")
                is PendingImage.Remote -> item.storagePath
            }
        }
    }

    companion object {
        /** Hard cap mirroring [Product.MAX_IMAGES]. Domain-level check. */
        val MAX: Int get() = Product.MAX_IMAGES
    }
}
