package com.example.danp_lab07.domain

import com.example.danp_lab07.data.Product
import com.example.danp_lab07.repository.ProductRepository
import javax.inject.Inject

class SaveProductUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(product: Product) {
        repository.saveProduct(product)
    }
}
