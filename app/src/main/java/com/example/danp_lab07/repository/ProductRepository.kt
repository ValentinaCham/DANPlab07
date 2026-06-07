package com.example.danp_lab07.repository

import com.example.danp_lab07.data.Product

class ProductRepository {
    private val _products = mutableListOf(
        Product(1, "Laptop", 3500.0, "Laptop gamer de alta gama", "Tecnología", "https://media.es.wired.com/photos/6643c25c4f46a4d19c7ab47c/master/pass/HP-Spectre-x360-14-(2024)-Abstract-Background-SOURCE-Best-Buy.jpg"),
        Product(2, "Mouse", 80.0, "Mouse ergonómico inalámbrico", "Tecnología", "https://upload.wikimedia.org/wikipedia/commons/2/22/3-Tasten-Maus_Microsoft.jpg"),
        Product(3, "Teclado", 150.0, "Teclado mecánico RGB", "Tecnología", "https://pe-media.hptiendaenlinea.com/magefan_blog/Teclado_gamer.jpg")
    )
    private var currentId = 4

    fun getProducts(): List<Product> = _products.toList()

    fun getProductById(id: Int): Product? = _products.find { it.id == id }

    fun addProduct(product: Product) {
        _products.add(product.copy(id = currentId++))
    }

    fun updateProduct(updatedProduct: Product) {
        val index = _products.indexOfFirst { it.id == updatedProduct.id }
        if (index != -1) {
            _products[index] = updatedProduct
        }
    }

    fun deleteProduct(id: Int) {
        _products.removeAll { it.id == id }
    }
}
