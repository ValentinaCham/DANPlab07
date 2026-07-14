package com.example.danp_lab07.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.danp_lab07.data.Product

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val price: Double,
    val description: String,
    val category: String,
    val imageUri: String,
    val isSynced: Boolean = true // Flag for offline-first sync logic
)

fun ProductEntity.toDomain(): Product {
    return Product(
        id = id,
        name = name,
        price = price,
        description = description,
        category = category,
        imageUri = imageUri
    )
}

fun Product.toEntity(isSynced: Boolean = true): ProductEntity {
    return ProductEntity(
        id = id,
        name = name,
        price = price,
        description = description,
        category = category,
        imageUri = imageUri,
        isSynced = isSynced
    )
}
