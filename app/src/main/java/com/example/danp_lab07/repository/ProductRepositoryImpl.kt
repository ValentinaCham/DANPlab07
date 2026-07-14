package com.example.danp_lab07.repository

import com.example.danp_lab07.data.Product
import com.example.danp_lab07.data.local.ProductDao
import com.example.danp_lab07.data.local.toDomain
import com.example.danp_lab07.data.local.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao
) : ProductRepository {

    override fun getProductsStream(): Flow<List<Product>> {
        return productDao.getAllProducts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getProductById(id: Int): Product? {
        return productDao.getProductById(id)?.toDomain()
    }

    override suspend fun saveProduct(product: Product) {
        // Offline-First: Save locally first
        productDao.insertProduct(product.toEntity(isSynced = false))
        
        // Trigger background sync or try immediate sync here
        // For this lab, we'll assume a background worker handles the isSynced = false items
    }

    override suspend fun deleteProduct(id: Int) {
        productDao.deleteProductById(id)
    }

    override suspend fun refreshProducts() {
        // Simulate fetching from Remote API
        // val remoteProducts = apiService.getProducts()
        // productDao.insertProducts(remoteProducts.map { it.toEntity() })
    }
}
