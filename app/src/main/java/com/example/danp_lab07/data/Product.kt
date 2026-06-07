package com.example.danp_lab07.data

data class Product(
    val id: Int = 0,
    val name: String,
    val price: Double,
    val description: String,
    val category: String,
    val imageUri: String
)
