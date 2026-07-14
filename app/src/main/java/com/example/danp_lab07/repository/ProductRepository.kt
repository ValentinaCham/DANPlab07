package com.example.danp_lab07.repository

import com.example.danp_lab07.data.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getProductsStream(): Flow<List<Product>>
    suspend fun getProductById(id: Int): Product?
    suspend fun saveProduct(product: Product)
    suspend fun deleteProduct(id: Int)
    suspend fun refreshProducts() // To sync with remote
}
