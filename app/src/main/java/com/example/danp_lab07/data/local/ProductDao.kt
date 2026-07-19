package com.example.danp_lab07.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE isDeleted = 0 ORDER BY id")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Int): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Query("UPDATE products SET isDeleted = 1, isSynced = 0 WHERE id = :id")
    suspend fun markDeleted(id: Int)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: Int)

    @Query("SELECT * FROM products WHERE isSynced = 0")
    suspend fun getUnsyncedProducts(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE isSynced = 1")
    suspend fun getSyncedProducts(): List<ProductEntity>

    @Query("UPDATE products SET isSynced = 1 WHERE id = :id")
    suspend fun markProductSynced(id: Int)

    @Transaction
    suspend fun mergeRemoteProducts(products: List<ProductEntity>) {
        val pendingIds = getUnsyncedProducts().mapTo(mutableSetOf()) { it.id }
        val remoteProducts = products.filterNot { it.id in pendingIds }
        insertProducts(remoteProducts)

        val remoteIds = products.mapTo(mutableSetOf()) { it.id }
        getSyncedProducts()
            .filterNot { it.id in remoteIds }
            .forEach { deleteProductById(it.id) }
    }
}
