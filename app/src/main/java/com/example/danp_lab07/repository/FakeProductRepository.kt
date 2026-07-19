package com.example.danp_lab07.repository

import com.example.danp_lab07.data.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class FakeProductRepository @Inject constructor() : ProductRepository {
    override fun getProductsStream(): Flow<List<Product>> = flowOf(emptyList())

    override suspend fun getProductById(id: Int): Product? = null

    override suspend fun saveProduct(product: Product) = Unit

    override suspend fun deleteProduct(id: Int) = Unit

    override suspend fun refreshProducts() = Unit

    override suspend fun syncPendingChanges(): List<Int> = emptyList()
}
