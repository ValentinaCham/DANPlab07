package com.example.danp_lab07.repository

import com.example.danp_lab07.data.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class FakeProductRepository @Inject constructor() : ProductRepository {
    override fun getProductsStream(): Flow<List<Product>> = flowOf(
        listOf(Product(1, "Fake Product", 0.0, "Fake Description", "Fake Category", ""))
    )

    override suspend fun getProductById(id: Int): Product? = null

    override suspend fun saveProduct(product: Product) {}

    override suspend fun deleteProduct(id: Int) {}

    override suspend fun refreshProducts() {}
}
