package com.example.danp_lab07.data.remote

import com.example.danp_lab07.data.Product

interface ProductRemoteDataSource {
    suspend fun getProducts(): List<Product>
    suspend fun upsertProduct(product: Product)
    suspend fun deleteProduct(id: Int)
}
