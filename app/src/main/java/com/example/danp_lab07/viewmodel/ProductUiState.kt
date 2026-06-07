package com.example.danp_lab07.viewmodel

import com.example.danp_lab07.data.Product

sealed class ProductUiState {
    object Loading : ProductUiState()
    data class Success(val products: List<Product>) : ProductUiState()
    data class Error(val message: String) : ProductUiState()
}
