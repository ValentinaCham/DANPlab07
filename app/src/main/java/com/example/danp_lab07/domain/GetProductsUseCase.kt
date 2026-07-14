package com.example.danp_lab07.domain

import com.example.danp_lab07.data.Product
import com.example.danp_lab07.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetProductsUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(query: String = ""): Flow<List<Product>> {
        return repository.getProductsStream().map { products ->
            if (query.isEmpty()) {
                products
            } else {
                products.filter {
                    it.name.contains(query, ignoreCase = true) ||
                            it.category.contains(query, ignoreCase = true)
                }
            }
        }
    }
}
