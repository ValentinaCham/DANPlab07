package com.example.danp_lab07.viewmodel

import com.example.danp_lab07.data.Product

sealed class ProductUiState {
    object Loading : ProductUiState()
    data class Success(val products: List<Product>) : ProductUiState()
    data class Error(val message: String) : ProductUiState()
}

/**
 * Discrete states for the image upload flow used by the form screen.
 * `Idle` is the default; `Uploading` carries the number of files left.
 */
sealed class ImageUploadState {
    object Idle : ImageUploadState()
    data class Uploading(val remaining: Int) : ImageUploadState()
    object Success : ImageUploadState()
    data class Error(val message: String) : ImageUploadState()
}
