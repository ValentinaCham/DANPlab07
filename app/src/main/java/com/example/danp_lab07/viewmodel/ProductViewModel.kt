package com.example.danp_lab07.viewmodel

import androidx.lifecycle.ViewModel
import com.example.danp_lab07.data.Product
import com.example.danp_lab07.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProductViewModel(private val repository: ProductRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductUiState>(ProductUiState.Loading)
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        _uiState.value = ProductUiState.Loading
        try {
            val allProducts = repository.getProducts()
            val query = _searchQuery.value
            val filtered = if (query.isEmpty()) {
                allProducts
            } else {
                allProducts.filter { 
                    it.name.contains(query, ignoreCase = true) || 
                    it.category.contains(query, ignoreCase = true)
                }
            }
            _uiState.value = ProductUiState.Success(filtered)
        } catch (e: Exception) {
            _uiState.value = ProductUiState.Error("Error al cargar productos")
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        loadProducts()
    }

    fun saveProduct(product: Product) {
        if (product.id == 0) {
            repository.addProduct(product)
        } else {
            repository.updateProduct(product)
        }
        loadProducts()
    }

    fun removeProduct(id: Int) {
        repository.deleteProduct(id)
        loadProducts()
    }
}
