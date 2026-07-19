package com.example.danp_lab07.worker

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.danp_lab07.repository.ImageRepository
import com.example.danp_lab07.repository.ProductRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: android.content.Context,
    @Assisted params: WorkerParameters,
    private val repository: ProductRepository,
    private val imageRepository: ImageRepository,
    private val connectivityManager: ConnectivityManager,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        if (!isNetworkAvailable()) {
            Log.d(TAG, "No validated network available; retrying sync")
            return Result.retry()
        }

        return try {
            Log.d(TAG, "Starting sync: push then pull")
            val tombstonedIds = repository.syncPendingChanges()
            if (tombstonedIds.isNotEmpty()) {
                Log.d(TAG, "Cleaning Storage for ${tombstonedIds.size} tombstones")
                tombstonedIds.forEach { id ->
                    runCatching { imageRepository.deleteAllForProduct(id) }
                        .onFailure { Log.w(TAG, "Storage cleanup failed for $id", it) }
                }
            }
            repository.refreshProducts()
            Log.d(TAG, "Sync finished successfully")
            Result.success()
        } catch (exception: Exception) {
            Log.e(TAG, "Sync failed, will retry", exception)
            Result.retry()
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    companion object {
        const val TAG = "SyncWorker"
        const val UNIQUE_NAME = "product_sync_periodic"
        const val IMMEDIATE_WORK_NAME = "product_sync_immediate"
    }
}
