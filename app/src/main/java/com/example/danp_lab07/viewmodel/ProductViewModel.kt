package com.example.danp_lab07.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.danp_lab07.data.Product
import com.example.danp_lab07.domain.GetProductsUseCase
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
    private val repository: ProductRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

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

    fun saveProduct(product: Product) {
        viewModelScope.launch {
            saveProductUseCase(product)
        }
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
