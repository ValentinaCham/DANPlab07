package com.example.danp_lab07.repository

import com.example.danp_lab07.data.Product
import com.example.danp_lab07.data.local.ProductDao
import com.example.danp_lab07.data.local.ProductEntity
import com.example.danp_lab07.data.remote.ProductRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductRepositoryImplTest {

    @Test
    fun saveProduct_persistsUnsyncedAndRequestsSync() = runBlocking {
        val dao = InMemoryProductDao()
        val scheduler = RecordingSyncScheduler()
        val repository = ProductRepositoryImpl(dao, FakeRemoteDataSource(), scheduler)

        repository.saveProduct(product(id = 7, name = "Offline"))

        val saved = dao.getProductById(7)
        assertEquals("Offline", saved?.name)
        assertFalse(saved!!.isSynced)
        assertFalse(saved.isDeleted)
        assertEquals(1, scheduler.enqueueCount)
    }

    @Test
    fun deleteProduct_keepsUnsyncedTombstoneButHidesItFromStream() = runBlocking {
        val dao = InMemoryProductDao()
        dao.insertProduct(product(id = 7).toEntityForTest(isSynced = true))
        val scheduler = RecordingSyncScheduler()
        val repository = ProductRepositoryImpl(dao, FakeRemoteDataSource(), scheduler)

        repository.deleteProduct(7)

        assertTrue(repository.getProductsStream().first().isEmpty())
        val tombstone = dao.getUnsyncedProducts().single()
        assertTrue(tombstone.isDeleted)
        assertFalse(tombstone.isSynced)
        assertEquals(1, scheduler.enqueueCount)
    }

    @Test
    fun syncPendingChanges_pushesUpsertsAndDeletesBeforeClearingPendingState() = runBlocking {
        val dao = InMemoryProductDao()
        dao.insertProduct(product(id = 1, name = "Create").toEntityForTest(isSynced = false))
        dao.insertProduct(
            product(id = 2, name = "Delete").toEntityForTest(
                isSynced = false,
                isDeleted = true,
            ),
        )
        val remote = FakeRemoteDataSource()
        val repository = ProductRepositoryImpl(dao, remote, RecordingSyncScheduler())

        repository.syncPendingChanges()

        assertEquals(listOf(1), remote.upserted.map { it.id })
        assertEquals(listOf(2), remote.deletedIds)
        assertTrue(dao.getProductById(1)!!.isSynced)
        assertNull(dao.getProductById(2))
        assertTrue(dao.getUnsyncedProducts().isEmpty())
    }

    @Test
    fun refreshProducts_mergesRemoteWithoutOverwritingPendingLocalChanges() = runBlocking {
        val dao = InMemoryProductDao()
        dao.insertProduct(product(id = 1, name = "Local pending").toEntityForTest(isSynced = false))
        dao.insertProduct(product(id = 2, name = "Old remote").toEntityForTest(isSynced = true))
        val remote = FakeRemoteDataSource(
            products = listOf(
                product(id = 1, name = "Remote conflict"),
                product(id = 2, name = "Remote update"),
                product(id = 3, name = "Remote new"),
            ),
        )
        val repository = ProductRepositoryImpl(dao, remote, RecordingSyncScheduler())

        repository.refreshProducts()

        assertEquals("Local pending", dao.getProductById(1)?.name)
        assertFalse(dao.getProductById(1)!!.isSynced)
        assertEquals("Remote update", dao.getProductById(2)?.name)
        assertTrue(dao.getProductById(2)!!.isSynced)
        assertEquals("Remote new", dao.getProductById(3)?.name)
        assertTrue(dao.getProductById(3)!!.isSynced)
    }
}

private class FakeRemoteDataSource(
    private val products: List<Product> = emptyList(),
) : ProductRemoteDataSource {
    val upserted = mutableListOf<Product>()
    val deletedIds = mutableListOf<Int>()

    override suspend fun getProducts(): List<Product> = products

    override suspend fun upsertProduct(product: Product) {
        upserted += product
    }

    override suspend fun deleteProduct(id: Int) {
        deletedIds += id
    }
}

private class RecordingSyncScheduler : ProductSyncScheduler {
    var enqueueCount = 0

    override fun enqueueSync() {
        enqueueCount++
    }
}

private class InMemoryProductDao : ProductDao {
    private val rows = linkedMapOf<Int, ProductEntity>()
    private val visibleProducts = MutableStateFlow<List<ProductEntity>>(emptyList())
    private var nextId = 1

    override fun getAllProducts(): Flow<List<ProductEntity>> = visibleProducts

    override suspend fun getProductById(id: Int): ProductEntity? = rows[id]

    override suspend fun insertProducts(products: List<ProductEntity>) {
        products.forEach { insertProduct(it) }
    }

    override suspend fun insertProduct(product: ProductEntity): Long {
        val id = if (product.id == 0) nextId++ else product.id
        rows[id] = product.copy(id = id)
        publish()
        return id.toLong()
    }

    override suspend fun markDeleted(id: Int) {
        rows[id]?.let { rows[id] = it.copy(isSynced = false, isDeleted = true) }
        publish()
    }

    override suspend fun deleteProductById(id: Int) {
        rows.remove(id)
        publish()
    }

    override suspend fun getUnsyncedProducts(): List<ProductEntity> =
        rows.values.filterNot { it.isSynced }

    override suspend fun getSyncedProducts(): List<ProductEntity> =
        rows.values.filter { it.isSynced }

    override suspend fun markProductSynced(id: Int) {
        rows[id]?.let { rows[id] = it.copy(isSynced = true) }
        publish()
    }

    private fun publish() {
        visibleProducts.value = rows.values.filterNot { it.isDeleted }
    }
}

private fun product(id: Int, name: String = "Product $id") = Product(
    id = id,
    name = name,
    price = 10.0,
    description = "Description",
    category = "Category",
    imageUri = "",
)

private fun Product.toEntityForTest(
    isSynced: Boolean,
    isDeleted: Boolean = false,
) = ProductEntity(
    id = id,
    name = name,
    price = price,
    description = description,
    category = category,
    imageUri = imageUri,
    isSynced = isSynced,
    isDeleted = isDeleted,
)
