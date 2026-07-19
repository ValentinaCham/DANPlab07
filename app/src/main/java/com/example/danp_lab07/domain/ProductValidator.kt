package com.example.danp_lab07.domain

import com.example.danp_lab07.data.Product

data class ProductValidationResult(
    val parsedPrice: Double? = null,
    val nameError: String? = null,
    val priceError: String? = null,
    val descriptionError: String? = null,
    val categoryError: String? = null,
    val imagesError: String? = null,
) {
    val isValid: Boolean
        get() = nameError == null &&
            priceError == null &&
            descriptionError == null &&
            categoryError == null &&
            imagesError == null
}

object ProductValidator {
    fun validate(
        name: String,
        price: String,
        description: String,
        category: String,
        imageCount: Int = 0,
    ): ProductValidationResult {
        val parsedPrice = price.trim().toDoubleOrNull()
        return ProductValidationResult(
            parsedPrice = parsedPrice,
            nameError = if (name.isBlank()) "El nombre es obligatorio" else null,
            priceError = when {
                parsedPrice == null -> "Ingresa un precio válido"
                parsedPrice <= 0 -> "El precio debe ser mayor que cero"
                else -> null
            },
            descriptionError = if (description.isBlank()) {
                "La descripción es obligatoria"
            } else {
                null
            },
            categoryError = if (category.isBlank()) "La categoría es obligatoria" else null,
            imagesError = if (imageCount > Product.MAX_IMAGES) {
                "Máximo ${Product.MAX_IMAGES} imágenes por producto"
            } else {
                null
            },
        )
    }
}
