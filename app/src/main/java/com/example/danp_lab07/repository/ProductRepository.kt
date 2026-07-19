package com.example.danp_lab07.repository

import com.example.danp_lab07.data.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getProductsStream(): Flow<List<Product>>
    suspend fun getProductById(id: Int): Product?
    suspend fun saveProduct(product: Product)
    suspend fun deleteProduct(id: Int)

    /**
     * Pull fresh data from the remote source and merge into Room. Remote
     * is the source of truth on conflicts unless the local row is pending sync.
     */
    suspend fun refreshProducts()

    /**
     * Push every pending change (created/edited/deleted) to the remote
     * source. Returns the list of product ids that were tombstoned (i.e.
     * hard-deleted from Room during this sync) so the caller can run
     * compensating actions like dropping their Storage folder.
     */
    suspend fun syncPendingChanges(): List<Int>
}
