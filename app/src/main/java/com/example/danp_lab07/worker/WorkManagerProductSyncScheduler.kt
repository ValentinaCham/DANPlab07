package com.example.danp_lab07.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.danp_lab07.repository.ProductSyncScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerProductSyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : ProductSyncScheduler {

    override fun enqueueSync() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            SyncWorker.IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request,
        )
    }
}
