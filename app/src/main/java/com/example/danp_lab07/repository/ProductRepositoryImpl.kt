package com.example.danp_lab07.repository

import com.example.danp_lab07.data.Product
import com.example.danp_lab07.data.local.ProductDao
import com.example.danp_lab07.data.local.toDomain
import com.example.danp_lab07.data.local.toEntity
import com.example.danp_lab07.data.remote.ProductRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao,
    private val remoteDataSource: ProductRemoteDataSource,
    private val syncScheduler: ProductSyncScheduler,
) : ProductRepository {

    override fun getProductsStream(): Flow<List<Product>> =
        productDao.getAllProducts().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getProductById(id: Int): Product? =
        productDao.getProductById(id)
            ?.takeUnless { it.isDeleted }
            ?.toDomain()

    override suspend fun saveProduct(product: Product) {
        val localId = productDao.insertProduct(
            product.toEntity(
                isSynced = false,
                isDeleted = false,
            ),
        )
        check(localId > 0) { "Room could not persist the product" }
        syncScheduler.enqueueSync()
    }

    override suspend fun deleteProduct(id: Int) {
        productDao.getProductById(id) ?: return
        productDao.markDeleted(id)
        syncScheduler.enqueueSync()
    }

    /**
     * Syncs every unsynced row and reports the product ids that got
     * tombstoned (deleted from Room) so the caller can perform compensating
     * work, e.g. dropping their images from Firebase Storage.
     */
    override suspend fun syncPendingChanges(): List<Int> {
        val tombstonedIds = mutableListOf<Int>()
        productDao.getUnsyncedProducts().forEach { entity ->
            if (entity.isDeleted) {
                remoteDataSource.deleteProduct(entity.id)
                tombstonedIds += entity.id
                productDao.deleteProductById(entity.id)
            } else {
                remoteDataSource.upsertProduct(entity.toDomain())
                productDao.markProductSynced(entity.id)
            }
        }
        return tombstonedIds
    }

    override suspend fun refreshProducts() {
        val remoteProducts = remoteDataSource.getProducts()
            .map { it.toEntity(isSynced = true, isDeleted = false) }

        productDao.mergeRemoteProducts(remoteProducts)
    }
}
