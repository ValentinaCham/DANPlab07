package com.example.danp_lab07.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.danp_lab07.data.Product
import com.example.danp_lab07.domain.GetProductsUseCase
import com.example.danp_lab07.domain.PendingImage
import com.example.danp_lab07.domain.SaveProductUseCase
import com.example.danp_lab07.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val saveProductUseCase: SaveProductUseCase,
    private val repository: ProductRepository,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /** Discrete state used by the form screen to drive a progress indicator. */
    private val _uploadState = MutableStateFlow<ImageUploadState>(ImageUploadState.Idle)
    val uploadState: StateFlow<ImageUploadState> = _uploadState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ProductUiState> = _searchQuery
        .flatMapLatest { query ->
            getProductsUseCase(query)
        }
        .map { products ->
            ProductUiState.Success(products)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProductUiState.Loading
        )

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    /**
     * Saves a product, uploading any [pendingUris] to Storage first.
     *
     * Existing storage paths already associated with the product (passed via
     * [existingStoragePaths]) are preserved; [removedPaths] are deleted from
     * Storage after the save completes successfully.
     */
    fun saveProduct(
        product: Product,
        existingStoragePaths: List<String> = emptyList(),
        pendingUris: List<Uri> = emptyList(),
        removedPaths: List<String> = emptyList(),
    ) {
        viewModelScope.launch {
            val pending: List<PendingImage> = buildList(
                existingStoragePaths.size + pendingUris.size,
            ) {
                addAll(existingStoragePaths.map { PendingImage.Remote(it) })
                addAll(pendingUris.map { PendingImage.Local(it) })
            }.take(Product.MAX_IMAGES)

            _uploadState.value = if (pendingUris.isEmpty()) {
                ImageUploadState.Idle
            } else {
                ImageUploadState.Uploading(remaining = pendingUris.size)
            }

            runCatching {
                saveProductUseCase(
                    product = product,
                    pending = SaveProductUseCase.PendingSave(
                        pendingImages = pending,
                        removedStoragePaths = removedPaths,
                    ),
                )
            }.onSuccess {
                _uploadState.value = ImageUploadState.Success
            }.onFailure { e ->
                _uploadState.value = ImageUploadState.Error(
                    e.message ?: "Error al guardar el producto",
                )
            }
        }
    }

    fun consumeUploadState() {
        _uploadState.value = ImageUploadState.Idle
    }

    fun removeProduct(id: Int) {
        viewModelScope.launch {
            repository.deleteProduct(id)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            repository.refreshProducts()
        }
    }
}
