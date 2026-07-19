package com.example.danp_lab07.data.remote

import com.example.danp_lab07.data.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreProductDataSource @Inject constructor(
    firestore: FirebaseFirestore,
) : ProductRemoteDataSource {

    private val productsCollection = firestore.collection(COLLECTION_PRODUCTS)

    /**
     * Firestore schema: collection `products`, numeric document id, and fields
     * `id`, `name`, `price`, `description`, `category`, `imageUris` (array).
     * Legacy `imageUri` (string) is read as a fallback for older documents.
     */
    override suspend fun getProducts(): List<Product> {
        return productsCollection
            .get(Source.SERVER)
            .await()
            .documents
            .map { document ->
                Product(
                    id = document.id.toIntOrNull()
                        ?: document.getLong(FIELD_ID)?.toInt()
                        ?: throw IllegalStateException(
                            "Product document '${document.id}' has no numeric id",
                        ),
                    name = document.getString(FIELD_NAME).orEmpty(),
                    price = document.getDouble(FIELD_PRICE) ?: 0.0,
                    description = document.getString(FIELD_DESCRIPTION).orEmpty(),
                    category = document.getString(FIELD_CATEGORY).orEmpty(),
                    imageUris = readImageUris(document),
                )
            }
    }

    override suspend fun upsertProduct(product: Product) {
        require(product.id > 0) { "A product must have a local id before syncing" }
        productsCollection
            .document(product.id.toString())
            .set(product.toFirestoreMap())
            .await()
    }

    override suspend fun deleteProduct(id: Int) {
        productsCollection.document(id.toString()).delete().await()
    }

    private fun readImageUris(document: com.google.firebase.firestore.DocumentSnapshot): List<String> {
        // Preferred: array of paths.
        val array = document.get(FIELD_IMAGE_URIS) as? List<*>
        if (!array.isNullOrEmpty()) {
            return array.mapNotNull { it?.toString() }
        }
        // Fallback: legacy scalar string.
        val legacy = document.getString(FIELD_IMAGE_URI_LEGACY)
        return if (legacy.isNullOrBlank()) emptyList() else listOf(legacy)
    }

    private fun Product.toFirestoreMap(): Map<String, Any> = mapOf(
        FIELD_ID to id,
        FIELD_NAME to name,
        FIELD_PRICE to price,
        FIELD_DESCRIPTION to description,
        FIELD_CATEGORY to category,
        FIELD_IMAGE_URIS to imageUris,
    )

    companion object {
        const val COLLECTION_PRODUCTS = "products"
        const val FIELD_ID = "id"
        const val FIELD_NAME = "name"
        const val FIELD_PRICE = "price"
        const val FIELD_DESCRIPTION = "description"
        const val FIELD_CATEGORY = "category"
        const val FIELD_IMAGE_URIS = "imageUris"
        const val FIELD_IMAGE_URI_LEGACY = "imageUri"
    }
}
