package com.example.danp_lab07.domain

import com.example.danp_lab07.data.Product
import com.example.danp_lab07.data.local.ProductDao
import com.example.danp_lab07.data.local.toEntity
import com.example.danp_lab07.repository.ImageRepository
import com.example.danp_lab07.repository.ProductRepository
import com.example.danp_lab07.repository.ProductSyncScheduler
import javax.inject.Inject
import kotlinx.coroutines.coroutineScope

/**
 * Orchestrates the create/update flow including uploads.
 *
 * Algorithm:
 *  1. For brand-new products (id == 0) we save once with empty `imageUris`
 *     to obtain the Room-generated id.
 *  2. Then we upload any pending local Uris under that product id.
 *  3. Finally we re-save the product carrying the resulting Storage paths.
 *  4. For edits, we skip step 1 and only upload + re-save the changed paths.
 *
 * Offline-first: the local Room row is the source of truth at all times.
 * If the device is offline, we still save the row (paths will be empty
 * until connectivity returns) and the SyncWorker retries the Firestore push.
 */
class SaveProductUseCase @Inject constructor(
    private val repository: ProductRepository,
    private val uploadUseCase: UploadProductImagesUseCase,
    private val imageRepository: ImageRepository,
    private val productDao: ProductDao,
    private val syncScheduler: ProductSyncScheduler,
) {

    /**
     * Per-call hints gathered by the UI: brand-new local picks and any
     * Storage paths the user explicitly removed during this edit.
     */
    data class PendingSave(
        val pendingImages: List<PendingImage> = emptyList(),
        val removedStoragePaths: List<String> = emptyList(),
    )

    suspend operator fun invoke(
        product: Product,
        pending: PendingSave = PendingSave(),
    ): Product {
        // 1. Ensure a stable id exists. For new products we save with empty
        //    imageUris first so the autoincrement id is allocated.
        val withId: Product = if (product.id == 0) {
            val newId = productDao.insertProduct(
                product.copy(imageUris = emptyList()).toEntity(
                    isSynced = false, isDeleted = false,
                ),
            )
            check(newId > 0) { "Room could not persist the product" }
            product.copy(id = newId.toInt())
        } else {
            repository.saveProduct(product)
            product
        }

        // 2. Combine existing Storage paths with newly picked local Uris,
        //    capped at MAX_IMAGES.
        val combined: List<PendingImage> = buildList(
            withId.imageUris.size + pending.pendingImages.size,
        ) {
            addAll(withId.imageUris.map { PendingImage.Remote(it) })
            addAll(pending.pendingImages)
        }.take(Product.MAX_IMAGES)

        // 3. Resolve final Storage paths (upload any PendingImage.Local).
        val finalPaths = uploadUseCase(withId.id, combined)

        // 4. Persist the final state.
        val mergedProduct = withId.copy(imageUris = finalPaths)
        repository.saveProduct(mergedProduct)
        syncScheduler.enqueueSync()

        // 5. Best-effort: remove any images the user explicitly dropped in
        //    this edit. Failures here don't roll back the Room save.
        if (pending.removedStoragePaths.isNotEmpty()) {
            coroutineScope {
                pending.removedStoragePaths.forEach { path ->
                    runCatching { imageRepository.deleteImage(path) }
                }
            }
        }

        return mergedProduct
    }
}
