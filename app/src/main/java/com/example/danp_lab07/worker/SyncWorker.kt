package com.example.danp_lab07.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.danp_lab07.data.local.ProductDao
import com.example.danp_lab07.data.local.toDomain
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import android.util.Log

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val productDao: ProductDao
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            Log.d("SyncWorker", "Starting sync...")
            val unsyncedProducts = productDao.getUnsyncedProducts()
            
            unsyncedProducts.forEach { entity ->
                // Simulate remote save
                // val result = apiService.saveProduct(entity.toDomain())
                // if (result.isSuccessful) {
                //     productDao.insertProduct(entity.copy(isSynced = true))
                // }
                Log.d("SyncWorker", "Syncing product: ${entity.name}")
                productDao.insertProduct(entity.copy(isSynced = true))
            }
            
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Error syncing", e)
            Result.retry()
        }
    }
}
